package com.mayabot.nlp.common;

import java.util.Arrays;

/**
 * @author jimichan jimichan@gmail.com
 */
public class IntArrayBuilder {

    /**
     * An immutable empty buffer (array).
     */
    public final static
    int []

            EMPTY_ARRAY =
            new int [0];

    /**
     * Internal array for storing the list. The array may be larger than the current size
     * ({@link #size()}).
     */
    public
    int []

            buffer = EMPTY_ARRAY;

    /**
     * Current number of elements stored in {@link #buffer}.
     */
    public int elementsCount;

    /**
     * Buffer resizing strategy.
     */
    protected final ArraySizingStrategy resizer;

    /**
     * New instance with sane defaults.
     */
    public IntArrayBuilder() {
        this(8);
    }

    /**
     * New instance with sane defaults.
     *
     * @param expectedElements
     *          The expected number of elements guaranteed not to cause buffer
     *          expansion (inclusive).
     */
    public IntArrayBuilder(int expectedElements) {
        this(expectedElements, BoundedProportionalArraySizingStrategy.DEFAULT_MIN_GROW_COUNT,
                BoundedProportionalArraySizingStrategy.DEFAULT_MAX_GROW_COUNT, 1.5f);
    }


    /**
     * New instance with sane defaults.
     *
     * @param expectedElements
     *          The expected number of elements guaranteed not to cause buffer
     *          expansion (inclusive).
     *
     */
    public IntArrayBuilder(int expectedElements, int minGrow, int maxGrow, float ratio) {
        this.resizer = new BoundedProportionalArraySizingStrategy(minGrow,maxGrow,ratio);
        ensureCapacity(expectedElements);
    }


    /**
     * {@inheritDoc}
     */
    
    public void add(int e1) {
        ensureBufferSpace(1);
        buffer[elementsCount++] = e1;
    }

    /**
     * Appends two elements at the end of the list. To add more than two elements,
     * use <code>add</code> (vararg-version) or access the buffer directly (tight
     * loop).
     */
    public void add(int e1, int e2) {
        ensureBufferSpace(2);
        buffer[elementsCount++] = e1;
        buffer[elementsCount++] = e2;
    }

    /**
     * Add all elements from a range of given array to the list.
     */
    public void add(int[] elements, int start, int length) {
        assert length >= 0 : "Length must be >= 0";

        ensureBufferSpace(length);
        System.arraycopy(elements, start, buffer, elementsCount, length);
        elementsCount += length;
    }

    /**
     * Vararg-signature method for adding elements at the end of the list.
     * <p>
     * <b>This method is handy, but costly if used in tight loops (anonymous array
     * passing)</b>
     * </p>
     */
    /*  */
    public final void add(int... elements) {
        add(elements, 0, elements.length);
    }


    public IntArray get() {
        return new IntArray(buffer, elementsCount);
    }

    public IntArray getCompactIntArray() {
        if(buffer.length - elementsCount > 128
                && buffer.length*1.0f/elementsCount > 1.15f ){
            trimToSize();
        }
        return new IntArray(buffer, elementsCount);
    }

    /**
     * Ensure this container can hold at least the given number of elements
     * without resizing its buffers.
     *
     * @param expectedElements
     *          The total number of elements, inclusive.
     */
    public void ensureCapacity(int expectedElements) {
        final int bufferLen = (buffer == null ? 0 : buffer.length);
        if (expectedElements > bufferLen) {
            ensureBufferSpace(expectedElements - size());
        }
    }

    /**
     * Ensures the internal buffer has enough free slots to store
     * <code>expectedAdditions</code>. Increases internal buffer size if needed.
     */
    protected void ensureBufferSpace(int expectedAdditions) {
        final int bufferLen = (buffer == null ? 0 : buffer.length);
        if (elementsCount + expectedAdditions > bufferLen) {
            final int newSize = resizer.grow(bufferLen, elementsCount, expectedAdditions);
            assert newSize >= elementsCount + expectedAdditions : "Resizer failed to" + " return sensible new size: "
                    + newSize + " <= " + (elementsCount + expectedAdditions);

            this.buffer = Arrays.copyOf(buffer, newSize);
        }
    }

    public int size() {
        return elementsCount;
    }

    /**
     * Trim the internal buffer to the current size.
     */
    public void trimToSize() {
        if (size() != this.buffer.length) {
            this.buffer =  toArray();
        }
    }


    /**
     * Sets the number of stored elements to zero and releases the internal
     * storage array.
     */
    public void release() {
        this.buffer =  EMPTY_ARRAY;
        this.elementsCount = 0;
    }

    /**
     * {@inheritDoc}
     *
     * <p>The returned array is sized to match exactly
     * the number of elements of the stack.</p>
     */
    public int [] toArray()

    {
        return Arrays.copyOf(buffer, elementsCount);
    }


}
