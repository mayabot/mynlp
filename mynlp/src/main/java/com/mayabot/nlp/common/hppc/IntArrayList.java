package com.mayabot.nlp.common.hppc;

import com.mayabot.nlp.common.ArraySizingStrategy;
import com.mayabot.nlp.common.BoundedProportionalArraySizingStrategy;

import java.util.Arrays;

/**
 * An array-backed list of ints.
 */

public class IntArrayList {

    public final static int DEFAULT_EXPECTED_ELEMENTS = 4;

    /**
     * An immutable empty buffer (array).
     */
    public final static
    int[] EMPTY_ARRAY = new int[0];


    /**
     * Internal array for storing the list. The array may be larger than the current size
     * ({@link #size()}).
     */
    public int[] buffer = EMPTY_ARRAY;

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
    public IntArrayList() {
        this(DEFAULT_EXPECTED_ELEMENTS);
    }

    /**
     * New instance with sane defaults.
     *
     * @param expectedElements The expected number of elements guaranteed not to cause buffer
     *                         expansion (inclusive).
     */
    public IntArrayList(int expectedElements) {
        this(expectedElements, new BoundedProportionalArraySizingStrategy());
    }


    /**
     * New instance with sane defaults.
     *
     * @param expectedElements The expected number of elements guaranteed not to cause buffer
     *                         expansion (inclusive).
     * @param resizer          Underlying buffer sizing strategy.
     */
    public IntArrayList(int expectedElements, ArraySizingStrategy resizer) {
        assert resizer != null;
        this.resizer = resizer;
        ensureCapacity(expectedElements);
    }


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


    public int get(int index) {
        assert (index >= 0 && index < size()) : "Index " + index + " out of bounds [" + 0 + ", " + size() + ").";

        return buffer[index];
    }


    public int set(int index, int e1) {
        assert (index >= 0 && index < size()) : "Index " + index + " out of bounds [" + 0 + ", " + size() + ").";

        final int v = buffer[index];
        buffer[index] = e1;
        return v;
    }


    public int remove(int index) {
        assert (index >= 0 && index < size()) : "Index " + index + " out of bounds [" + 0 + ", " + size() + ").";

        final int v = buffer[index];
        if (index + 1 < elementsCount) {
            System.arraycopy(buffer, index + 1, buffer, index, elementsCount - index - 1);
        }
        elementsCount--;
        buffer[elementsCount] = 0;
        return v;
    }


    public boolean contains(int e1) {
        return indexOf(e1) >= 0;
    }


    public int indexOf(int e1) {
        for (int i = 0; i < elementsCount; i++) {
            if (((buffer[i]) == (e1))) {
                return i;
            }
        }

        return -1;
    }

    public int lastIndexOf(int e1) {
        for (int i = elementsCount - 1; i >= 0; i--) {
            if (((buffer[i]) == (e1))) {
                return i;
            }
        }

        return -1;
    }


    public boolean isEmpty() {
        return elementsCount == 0;
    }

    /**
     * Ensure this container can hold at least the given number of elements
     * without resizing its buffers.
     *
     * @param expectedElements The total number of elements, inclusive.
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

    /**
     * Truncate or expand the list to the new size. If the list is truncated, the
     * buffer will not be reallocated (use {@link #trimToSize()} if you need a
     * truncated buffer), but the truncated values will be reset to the default
     * value (zero). If the list is expanded, the elements beyond the current size
     * are initialized with JVM-defaults (zero or <code>null</code> values).
     */
    public void resize(int newSize) {
        if (newSize <= buffer.length) {
            if (newSize < elementsCount) {
                Arrays.fill(buffer, newSize, elementsCount, 0);
            } else {
                Arrays.fill(buffer, elementsCount, newSize, 0);
            }
        } else {
            ensureCapacity(newSize);
        }
        this.elementsCount = newSize;
    }

    public int size() {
        return elementsCount;
    }

    /**
     * Trim the internal buffer to the current size.
     */
    public void trimToSize() {
        if (size() != this.buffer.length) {
            this.buffer = toArray();
        }
    }

    /**
     * Sets the number of stored elements to zero. Releases and initializes the
     * internal storage array to default values. To clear the list without
     * cleaning the buffer, simply set the {@link #elementsCount} field to zero.
     */
    public void clear() {
        Arrays.fill(buffer, 0, elementsCount, 0);
        this.elementsCount = 0;
    }

    /**
     * Sets the number of stored elements to zero and releases the internal
     * storage array.
     */
    public void release() {
        this.buffer = EMPTY_ARRAY;
        this.elementsCount = 0;
    }

    /**
     * <p>The returned array is sized to match exactly
     * the number of elements of the stack.</p>
     */
    public int[] toArray() {
        return Arrays.copyOf(buffer, elementsCount);
    }

    /**
     * Clone this object. The returned clone will reuse the same hash function and
     * array resizing strategy.
     */
    @Override
    public IntArrayList clone() {
        try {
            /*  */
            final IntArrayList cloned = (IntArrayList) super.clone();
            cloned.buffer = buffer.clone();
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public int hashCode() {
        int h = 1, max = elementsCount;
        for (int i = 0; i < max; i++) {
            h = 31 * h + mix32(this.buffer[i]);
        }
        return h;
    }

    /**
     * Returns <code>true</code> only if the other object is an instance of
     * the same class and with the same elements.
     */
    @Override
    public boolean equals(Object obj) {
        return obj != null &&
                getClass() == obj.getClass() &&
                equalElements(getClass().cast(obj));
    }

    /**
     * MH3's plain finalization step.
     */
    private static int mix32(int k) {
        k = (k ^ (k >>> 16)) * 0x85ebca6b;
        k = (k ^ (k >>> 13)) * 0xc2b2ae35;
        return k ^ (k >>> 16);
    }

    protected boolean equalElements(IntArrayList other) {
        int max = size();
        if (other.size() != max) {
            return false;
        }

        for (int i = 0; i < max; i++) {
            if (!((other.get(i)) == (get(i)))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Create a list from a variable number of arguments or an array of <code>int</code>.
     * The elements are copied from the argument to the internal buffer.
     */
    /*  */
    public static IntArrayList from(int... elements) {
        final IntArrayList list = new IntArrayList(elements.length);
        list.add(elements);
        return list;
    }
}
