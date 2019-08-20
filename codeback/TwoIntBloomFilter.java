package com.mayabot.nlp.common;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.math.LongMath;
import com.google.common.primitives.Ints;
import com.google.common.primitives.UnsignedBytes;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Random;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class TwoIntBloomFilter {

    public static void main(String[] args) {
        TwoIntBloomFilter bloomFilter = TwoIntBloomFilter.create(3000000);

        bloomFilter.put(1, 2);
        bloomFilter.put(1, 3);

        Random random = new Random(0);

        //473=>262

        for (int i = 0; i < 3000000; i++) {
            int a = random.nextInt(1000);
            int b = random.nextInt(1000);

//            if (i < 10) {
//                System.out.println(a + "=>" +b);
//            }

            bloomFilter.put(a, b);
        }

        for (int i = 0; i < 10000000; i++) {
            bloomFilter.mightContain(1, 3);
            bloomFilter.mightContain(12, 3);
        }

        long t1 = System.currentTimeMillis();
        for (int i = 0; i < 10000000; i++) {
            bloomFilter.mightContain(1, 3);
            bloomFilter.mightContain(12, 3);
        }
        long t2 = System.currentTimeMillis();

        System.out.println(t2 - t1);

        System.out.println(bloomFilter.mightContain(2, 3));
        System.out.println(bloomFilter.mightContain(1, 3));
        System.out.println(bloomFilter.mightContain(473, 262));
        System.out.println(bloomFilter.mightContain(4731, 262));
        System.out.println(bloomFilter.mightContain(47131, 262));


    }

    /**
     * The bit set of the BloomFilter (not necessarily power of 2!)
     */
    private final BitArray bits;

    /**
     * Number of hashes per element
     */
    private final int numHashFunctions;


    /**
     * Creates a BloomFilter.
     */
    private TwoIntBloomFilter(BitArray bits, int numHashFunctions) {
        checkArgument(numHashFunctions > 0, "numHashFunctions (%s) must be > 0", numHashFunctions);
        checkArgument(
                numHashFunctions <= 255, "numHashFunctions (%s) must be <= 255", numHashFunctions);
        this.bits = checkNotNull(bits);
        this.numHashFunctions = numHashFunctions;
    }

    /**
     * Returns {@code true} if the element <i>might</i> have been put in this Bloom filter,
     * {@code false} if this is <i>definitely</i> not the case.
     */
    public boolean mightContain(int a, int b) {
        long bitSize = bits.bitSize();
        int hash1 = hashInt(a);
        int hash2 = hashInt(b);
//        int hash1 = a;
//        int hash2 = b;

        for (int i = 1; i <= numHashFunctions; ++i) {
            int combinedHash = hash1 + i * hash2;
            if (combinedHash < 0) {
                combinedHash = ~combinedHash;
            }

            if (!bits.get((long) combinedHash % bitSize)) {
                return false;
            }
        }

        return true;
    }


    public boolean put(int a, int b) {
        long bitSize = bits.bitSize();
        //long hash64 = Hashing.murmur3_128().hashObject(object, funnel).asLong();
        int hash1 = hashInt(a);
        int hash2 = hashInt(b);
//        int hash1 = a;
//        int hash2 = b;
        boolean bitsChanged = false;

        for (int i = 1; i <= numHashFunctions; ++i) {
            int combinedHash = hash1 + i * hash2;
            if (combinedHash < 0) {
                combinedHash = ~combinedHash;
            }

            bitsChanged |= bits.set((long) combinedHash % bitSize);
        }

        return bitsChanged;
    }

    private int seed = 0;

    private static final int C1 = 0xcc9e2d51;
    private static final int C2 = 0x1b873593;

    public int hashInt(int input) {
        int k1 = mixK1(input);
        int h1 = mixH1(seed, k1);

        return fmix(h1, Ints.BYTES);
    }

    private static int mixK1(int k1) {
        k1 *= C1;
        k1 = Integer.rotateLeft(k1, 15);
        k1 *= C2;
        return k1;
    }

    private static int mixH1(int h1, int k1) {
        h1 ^= k1;
        h1 = Integer.rotateLeft(h1, 13);
        h1 = h1 * 5 + 0xe6546b64;
        return h1;
    }

    // Finalization mix - force all bits of a hash block to avalanche
    private static int fmix(int h1, int length) {
        h1 ^= length;
        h1 ^= h1 >>> 16;
        h1 *= 0x85ebca6b;
        h1 ^= h1 >>> 13;
        h1 *= 0xc2b2ae35;
        h1 ^= h1 >>> 16;
        return h1;
    }


    public double expectedFpp() {
        // You down with FPP? (Yeah you know me!) Who's down with FPP? (Every last homie!)
        return Math.pow((double) bits.bitCount() / bitSize(), numHashFunctions);
    }

    /**
     * Returns the number of bits in the underlying bit array.
     */
    @VisibleForTesting
    long bitSize() {
        return bits.bitSize();
    }


    public static TwoIntBloomFilter create(int expectedInsertions) {
        return create((long) expectedInsertions, 0.03);
    }

    public static TwoIntBloomFilter create(int expectedInsertions, double fpp) {
        return create((long) expectedInsertions, fpp);
    }

    @VisibleForTesting
    public static TwoIntBloomFilter create(long expectedInsertions, double fpp) {
        checkArgument(
                expectedInsertions >= 0, "Expected insertions (%s) must be >= 0", expectedInsertions);
        checkArgument(fpp > 0.0, "False positive probability (%s) must be > 0.0", fpp);
        checkArgument(fpp < 1.0, "False positive probability (%s) must be < 1.0", fpp);

        if (expectedInsertions == 0) {
            expectedInsertions = 1;
        }
        /*
         * TODO(user): Put a warning in the javadoc about tiny fpp values,
         * since the resulting size is proportional to -log(p), but there is not
         * much of a point after all, e.g. optimalM(1000, 0.0000000000000001) = 76680
         * which is less than 10kb. Who cares!
         */
        long numBits = optimalNumOfBits(expectedInsertions, fpp);
        int numHashFunctions = optimalNumOfHashFunctions(expectedInsertions, numBits);
        try {
            return new TwoIntBloomFilter(new BitArray(numBits), numHashFunctions);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Could not create BloomFilter of " + numBits + " bits", e);
        }
    }

    /*
     * Cheat sheet:
     *
     * m: total bits
     * n: expected insertions
     * b: m/n, bits per insertion
     * p: expected false positive probability
     *
     * 1) Optimal k = b * ln2
     * 2) p = (1 - e ^ (-kn/m))^k
     * 3) For optimal k: p = 2 ^ (-k) ~= 0.6185^b
     * 4) For optimal k: m = -nlnp / ((ln2) ^ 2)
     */

    /**
     * Computes the optimal k (number of hashes per element inserted in Bloom filter), given the
     * expected insertions and total number of bits in the Bloom filter.
     * <p>
     * See http://en.wikipedia.org/wiki/File:Bloom_filter_fp_probability.svg for the formula.
     *
     * @param n expected insertions (must be positive)
     * @param m total number of bits in Bloom filter (must be positive)
     */
    @VisibleForTesting
    static int optimalNumOfHashFunctions(long n, long m) {
        // (m / n) * log(2), but avoid truncation due to division!
        return Math.max(1, (int) Math.round((double) m / n * Math.log(2)));
    }

    /**
     * Computes m (total bits of Bloom filter) which is expected to achieve, for the specified
     * expected insertions, the required false positive probability.
     * <p>
     * See http://en.wikipedia.org/wiki/Bloom_filter#Probability_of_false_positives for the formula.
     *
     * @param n expected insertions (must be positive)
     * @param p false positive rate (must be 0 < p < 1)
     */
    @VisibleForTesting
    static long optimalNumOfBits(long n, double p) {
        if (p == 0) {
            p = Double.MIN_VALUE;
        }
        return (long) (-n * Math.log(p) / (Math.log(2) * Math.log(2)));
    }


    public void writeTo(DataOutput dout) throws IOException {
        /*
         * Serial form:
         * 1 signed byte for the strategy
         * 1 unsigned byte for the number of hash functions
         * 1 big endian int, the number of longs in our bitset
         * N big endian longs of our bitset
         */
        dout.writeByte(UnsignedBytes.checkedCast(numHashFunctions)); // note: checked at the c'tor
        dout.writeInt(bits.data.length);
        for (long value : bits.data) {
            dout.writeLong(value);
        }
    }

    public static TwoIntBloomFilter readFrom(DataInput din) throws IOException {
        checkNotNull(din, "InputStream");
        int strategyOrdinal = -1;
        int numHashFunctions = -1;
        int dataLength = -1;
        try {
//            DataInputStream din = new DataInputStream(in);
            // currently this assumes there is no negative ordinal; will have to be updated if we
            // add non-stateless strategies (for which we've reserved negative ordinals; see
            // Strategy.ordinal()).
            numHashFunctions = UnsignedBytes.toInt(din.readByte());
            dataLength = din.readInt();

            long[] data = new long[dataLength];
            for (int i = 0; i < data.length; i++) {
                data[i] = din.readLong();
            }
            return new TwoIntBloomFilter(new BitArray(data), numHashFunctions);
        } catch (RuntimeException e) {
            IOException ioException = new IOException(
                    "Unable to deserialize BloomFilter from InputStream."
                            + " strategyOrdinal: " + strategyOrdinal
                            + " numHashFunctions: " + numHashFunctions
                            + " dataLength: " + dataLength);
            ioException.initCause(e);
            throw ioException;
        }
    }


    static final class BitArray {
        final long[] data;
        long bitCount;

        BitArray(long bits) {
            this(new long[Ints.checkedCast(LongMath.divide(bits, 64, RoundingMode.CEILING))]);
        }

        // Used by serialization
        BitArray(long[] data) {
            checkArgument(data.length > 0, "data length is zero!");
            this.data = data;
            long bitCount = 0;
            for (long value : data) {
                bitCount += Long.bitCount(value);
            }
            this.bitCount = bitCount;
        }

        /**
         * Returns true if the bit changed value.
         */
        boolean set(long index) {
            if (!get(index)) {
                data[(int) (index >>> 6)] |= (1L << index);
                bitCount++;
                return true;
            }
            return false;
        }

        boolean get(long index) {
            return (data[(int) (index >>> 6)] & (1L << index)) != 0;
        }

        /**
         * Number of bits
         */
        long bitSize() {
            return (long) data.length * Long.SIZE;
        }

        /**
         * Number of set bits (1s)
         */
        long bitCount() {
            return bitCount;
        }

        BitArray copy() {
            return new BitArray(data.clone());
        }

        /**
         * Combines the two BitArrays using bitwise OR.
         */
        void putAll(BitArray array) {
            checkArgument(
                    data.length == array.data.length,
                    "BitArrays must be of equal length (%s != %s)",
                    data.length,
                    array.data.length);
            bitCount = 0;
            for (int i = 0; i < data.length; i++) {
                data[i] |= array.data[i];
                bitCount += Long.bitCount(data[i]);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof BitArray) {
                BitArray bitArray = (BitArray) o;
                return Arrays.equals(data, bitArray.data);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(data);
        }
    }
}
