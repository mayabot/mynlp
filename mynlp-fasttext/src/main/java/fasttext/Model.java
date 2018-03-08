package fasttext;

import com.carrotsearch.hppc.IntArrayList;
import com.google.common.primitives.Floats;
import fasttext.matrix.Matrix;
import fasttext.pq.QMatrix;
import fasttext.utils.FloatIntPair;
import fasttext.utils.Utils;
import fasttext.utils.loss_name;
import fasttext.utils.model_name;

import java.util.*;
import fasttext.matrix.Vector;

import static com.google.common.base.Preconditions.checkArgument;
import static fasttext.utils.Utils.shuffle;

public class Model {

	static final int SIGMOID_TABLE_SIZE = 512;
	static final int MAX_SIGMOID = 8;
	static final int LOG_TABLE_SIZE = 512;

	static final int NEGATIVE_TABLE_SIZE = 10000000;

	public class Node {
		int parent;
		int left;
		int right;
		long count;
		boolean binary;
	}

	private Matrix wi_; // input
	private Matrix wo_; // output
	private Args args_;
	private Vector hidden_;
	private Vector output_;
	private Vector grad_;
	private int hsz_; // dim
	@SuppressWarnings("unused")
	private int isz_; // input vocabSize
	private int osz_; // output vocabSize
	private float loss_;
	private long nexamples_;
	private float[] t_sigmoid;
	private float[] t_log;

	// used for negative sampling:

	private IntArrayList negatives;
	private int negpos;

	// used for hierarchical softmax:

	private List<List<Integer>> paths;
	private List<List<Boolean>> codes;
	private List<Node> tree;

	public transient Random rng;
	public boolean quant_;

	public Model(Matrix wi, Matrix wo, Args args, int seed) {
		hidden_ = new Vector(args.dim);
		output_ = new Vector(wo.rows());
		grad_ = new Vector(args.dim);
		rng = new Random((long) seed);

		wi_ = wi;
		wo_ = wo;
		args_ = args;
		isz_ = wi.rows();
		osz_ = wo.rows();
		hsz_ = args.dim;
		negpos = 0;
		loss_ = 0.0f;
		nexamples_ = 1l;
		initSigmoid();
		initLog();
	}

	public void setQuantizePointer(QMatrix qwi, QMatrix qwo, boolean qout) {
		//this.qwi = qwi;

	}

	public float binaryLogistic(int target, boolean label, float lr) {
		float score = sigmoid(wo_.dotRow(hidden_, target));
		float alpha = lr * ((label ? 1.0f : 0.0f) - score);
		//grad_.addRow(wo_, target, alpha);
		grad_.add(wo_.rowView(target),alpha);

		wo_.addRow(hidden_, target, alpha);
		if (label) {
			return -log(score);
		} else {
			return -log(1.0f - score);
		}
	}

	public float negativeSampling(int target, float lr) {
		float loss = 0.0f;
		grad_.zero();
		for (int n = 0; n <= args_.neg; n++) {
			if (n == 0) {
				loss += binaryLogistic(target, true, lr);
			} else {
				loss += binaryLogistic(getNegative(target), false, lr);
			}
		}
		return loss;
	}

	public float hierarchicalSoftmax(int target, float lr) {
		float loss = 0.0f;
		grad_.zero();
		final List<Boolean> binaryCode = codes.get(target);
		final List<Integer> pathToRoot = paths.get(target);
		for (int i = 0; i < pathToRoot.size(); i++) {
			loss += binaryLogistic(pathToRoot.get(i), binaryCode.get(i), lr);
		}
		return loss;
	}

	public void computeOutputSoftmax(Vector hidden, Vector output) {
		output.mul(wo_, hidden);
		float max = output.get(0), z = 0.0f;
		for (int i = 1; i < osz_; i++) {
			max = Math.max(output.get(i), max);
		}
		for (int i = 0; i < osz_; i++) {
			output.set(i, (float) Math.exp(output.get(i) - max));
			z += output.get(i);
		}
		for (int i = 0; i < osz_; i++) {
			output.set(i, output.get(i) / z);
		}
	}

	public void computeOutputSoftmax() {
		computeOutputSoftmax(hidden_, output_);
	}

	public float softmax(int target, float lr) {
		grad_.zero();
		computeOutputSoftmax();
		for (int i = 0; i < osz_; i++) {
			float label = (i == target) ? 1.0f : 0.0f;
			float alpha = lr * (label - output_.get(i));
			grad_.addRow(wo_, i, alpha);
			wo_.addRow(hidden_, i, alpha);
		}
		return -log(output_.get(target));
	}

	public void computeHidden(final IntArrayList input, Vector hidden) {
		checkArgument(hidden.size() == hsz_);
		hidden.zero();

		int[] buffer = input.buffer;
		for (int i = 0,size=input.size(); i < size; i++) {
			int it = buffer[i];
			hidden.addRow(wi_, it);
		}
		hidden.mul(1.0f / input.size());
	}

	private Comparator<FloatIntPair> comparePairs = (o1, o2) -> Floats.compare(o2.first,o1.first);

	public void predict(final IntArrayList input, int k, List<FloatIntPair> heap, Vector hidden,
			Vector output) {
		checkArgument(k > 0);
//		if (heap instanceof ArrayList) {
//			((ArrayList<FloatIntPair>) heap).ensureCapacity(k + 1);
//		}
		computeHidden(input, hidden);
		if (args_.loss == loss_name.hs) {
			dfs(k, 2 * osz_ - 2, 0.0f, heap, hidden);
		} else {
			findKBest(k, heap, hidden, output);
		}
		Collections.sort(heap, comparePairs);
	}

	public void predict(final IntArrayList input, int k, List<FloatIntPair> heap) {
		predict(input, k, heap, hidden_, output_);
	}

	public void findKBest(int k, List<FloatIntPair> heap, Vector hidden, Vector output) {
		computeOutputSoftmax(hidden, output);
		for (int i = 0; i < osz_; i++) {
			if (heap.size() == k && log(output.get(i)) < heap.get(heap.size() - 1).first) {
				continue;
			}
			heap.add(new FloatIntPair(log(output.get(i)), i));
			Collections.sort(heap, comparePairs);
			if (heap.size() > k) {
				Collections.sort(heap, comparePairs);
				heap.remove(heap.size() - 1); // pop last
			}
		}
	}

	public void dfs(int k, int node, float score, List<FloatIntPair> heap, Vector hidden) {
		if (heap.size() == k && score < heap.get(heap.size() - 1).first) {
			return;
		}

		if (tree.get(node).left == -1 && tree.get(node).right == -1) {
			heap.add(new FloatIntPair(score, node));
			Collections.sort(heap, comparePairs);
			if (heap.size() > k) {
				Collections.sort(heap, comparePairs);
				heap.remove(heap.size() - 1); // pop last
			}
			return;
		}

		float f = sigmoid(wo_.dotRow(hidden, node - osz_));
		dfs(k, tree.get(node).left, score + log(1.0f - f), heap, hidden);
		dfs(k, tree.get(node).right, score + log(f), heap, hidden);
	}

	public void update(final IntArrayList input, int target, float lr) {
		checkArgument(target >= 0);
		checkArgument(target < osz_);
		if (input.size() == 0) {
			return;
		}
		computeHidden(input, hidden_);

		if (args_.loss == loss_name.ns) {
			loss_ += negativeSampling(target, lr);
		} else if (args_.loss == loss_name.hs) {
			loss_ += hierarchicalSoftmax(target, lr);
		} else {
			loss_ += softmax(target, lr);
		}
		nexamples_ += 1;

		if (args_.model == model_name.sup) {
			grad_.mul(1.0f / input.size());
		}

		int[] buffer = input.buffer;
		for (int i = 0,size=input.size(); i < size; i++) {
			int it = buffer[i];
			wi_.addRow(grad_, it, 1.0f);
		}
	}

	public void setTargetCounts(final long[] counts) {
		checkArgument(counts.length == osz_);
		if (args_.loss == loss_name.ns) {
			initTableNegatives(counts);
		}else if (args_.loss == loss_name.hs) {
			buildTree(counts);
		}

	}

	public void initTableNegatives(final long[] counts) {
		negatives = new IntArrayList(counts.length);
		float z = 0.0f;
		final int size = counts.length;
		for (int i = 0; i < size; i++) {
			z += Utils.sqrt(counts[i]);
		}
		for (int i = 0; i < size; i++) {
			float c = Utils.sqrt(counts[i]);
			for (int j = 0; j < c * NEGATIVE_TABLE_SIZE / z; j++) {
				negatives.add(i);
			}
		}
		shuffle(negatives, rng);
	}

	public int getNegative(int target) {
		int negative;
		do {
			negative = negatives.get(negpos);
			negpos = (negpos + 1) % negatives.size();
		} while (target == negative);
		return negative;
	}

	public void buildTree(final long[] counts) {
		paths = new ArrayList<List<Integer>>(osz_);
		codes = new ArrayList<List<Boolean>>(osz_);
		tree = new ArrayList<Node>(2 * osz_ - 1);

		for (int i = 0; i < 2 * osz_ - 1; i++) {
			Node node = new Node();
			node.parent = -1;
			node.left = -1;
			node.right = -1;
			node.count = 1000000000000000L;// 1e15f;
			node.binary = false;
			tree.add(i, node);
		}
		for (int i = 0; i < osz_; i++) {
			tree.get(i).count = counts[i];
		}
		int leaf = osz_ - 1;
		int node = osz_;
		for (int i = osz_; i < 2 * osz_ - 1; i++) {
			int[] mini = new int[2];
			for (int j = 0; j < 2; j++) {
				if (leaf >= 0 && tree.get(leaf).count < tree.get(node).count) {
					mini[j] = leaf--;
				} else {
					mini[j] = node++;
				}
			}
			tree.get(i).left = mini[0];
			tree.get(i).right = mini[1];
			tree.get(i).count = tree.get(mini[0]).count + tree.get(mini[1]).count;
			tree.get(mini[0]).parent = i;
			tree.get(mini[1]).parent = i;
			tree.get(mini[1]).binary = true;
		}
		for (int i = 0; i < osz_; i++) {
			List<Integer> path = new ArrayList<Integer>();
			List<Boolean> code = new ArrayList<Boolean>();
			int j = i;
			while (tree.get(j).parent != -1) {
				path.add(tree.get(j).parent - osz_);
				code.add(tree.get(j).binary);
				j = tree.get(j).parent;
			}
			paths.add(path);
			codes.add(code);
		}
	}

	public float getLoss() {
		return loss_ / nexamples_;
	}

	private void initSigmoid() {
		t_sigmoid = new float[SIGMOID_TABLE_SIZE + 1];
		for (int i = 0; i < SIGMOID_TABLE_SIZE + 1; i++) {
			float x = (float) (i * 2 * MAX_SIGMOID) / SIGMOID_TABLE_SIZE - MAX_SIGMOID;
			t_sigmoid[i] = (float) (1.0f / (1.0f + Math.exp(-x)));
		}
	}

	private void initLog() {
		t_log = new float[LOG_TABLE_SIZE + 1];
		for (int i = 0; i < LOG_TABLE_SIZE + 1; i++) {
			float x = (float) (((float) (i) + 1e-5f) / LOG_TABLE_SIZE);
			t_log[i] = (float) Math.log(x);
		}
	}

	public float log(float x) {
		if (x > 1.0f) {
			return 0.0f;
		}
		int i = (int) (x * LOG_TABLE_SIZE);
		return t_log[i];
	}

	public float sigmoid(float x) {
		if (x < -MAX_SIGMOID) {
			return 0.0f;
		} else if (x > MAX_SIGMOID) {
			return 1.0f;
		} else {
			int i = (int) ((x + MAX_SIGMOID) * SIGMOID_TABLE_SIZE / MAX_SIGMOID / 2);
			return t_sigmoid[i];
		}
	}
}
