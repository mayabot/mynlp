package fasttext;


import fasttext.matrix.Matrix;
import fasttext.pq.QMatrix;
import fasttext.utils.CLangDataInputStream;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static fasttext.utils.ModelName.sup;

public class FastTextIO {

    /**
     * Load binary model file. 这个二进制版本是C语言版本的模型
     * @param modelPath
     * @return
     * @throws Exception
     */
    public static FastText readClangModel(String modelPath) throws Exception {


        File modeFile = new File(modelPath);

        if (!(modeFile.exists() && modeFile.isFile() && modeFile.canRead())) {
            throw new IOException("Model file cannot be opened for loading!");
        }

        try (
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(modeFile), 1024 * 64);
                CLangDataInputStream dis = new CLangDataInputStream(bis)) {

            //check model
            int magic = dis.readInt();
            int version = dis.readInt();

            if (magic != FastText.FASTTEXT_FILEFORMAT_MAGIC_INT32) {
                throw new RuntimeException("Model file has wrong file format!");
            }

            if (version > FastText.FASTTEXT_VERSION) {
                throw new RuntimeException("Model file has wrong file format! version is " + version);
            }

            //Args
            Args args_ = new Args();
            args_.load(dis);

            System.out.println(args_.toString());

            //
            if (version == 11 && args_.model == sup) {
                // backward compatibility: old supervised models do not use char ngrams.
                args_.maxn = 0;
            }

            Dictionary dictionary = new Dictionary(args_);
            dictionary.load(dis);


            Matrix input = new Matrix();
            QMatrix qinput = new QMatrix();

            boolean quant_input = dis.readBoolean();
            if (quant_input) {
                qinput.load(dis);
            } else {
                input.load(dis);
            }

            if (!quant_input && dictionary.isPruned()) {
                throw new RuntimeException("Invalid model file.\n"
                        + "Please download the updated model from www.fasttext.cc.\n"
                        + "See issue #332 on Github for more information.\n");
            }

            Matrix output = new Matrix();
            QMatrix qoutput = new QMatrix();

            args_.qout = dis.readBoolean();
            if (quant_input && args_.qout) {
                qoutput.load(dis);
            } else {
                output.load(dis);
            }


            Model model = new Model(input, output, args_, 0);
            model.quant_ = quant_input;
            model.setQuantizePointer(qinput, qoutput, args_.qout);

            if (args_.model == sup) {
                model.setTargetCounts(dictionary.getCounts(EntryType.label));
            } else {
                model.setTargetCounts(dictionary.getCounts(EntryType.word));
            }


            if (!quant_input) {
                return new FastText(dictionary, input, output, model, args_);
            } else {
                return new FastText(dictionary, qinput, qoutput, model, args_);
            }
        }
    }
}
