package com.mayabot.nlp.fasttext.utils

import java.util.*

class LongArrayList (
        expectedElements: Int = 4,
        private val resizer: ArraySizingStrategy = BoundedProportionalArraySizingStrategy()
) {

    /**
     * Internal array for storing the list. The array may be larger than the current size
     * ([.size]).
     */
    var buffer: LongArray = EMPTY_ARRAY

    /**
     * Current number of elements stored in [.buffer].
     */
    private var elementsCount = 0

    /**
     * New instance with sane defaults.
     */
    init {
        ensureCapacity(expectedElements)
    }

    inline fun forEach(action: (num: Long) -> Unit) {
        val buffer = this.buffer
        var i = 0
        val size = this.size()
        while (i < size) {
            action(buffer[i])
            i++
        }
    }

    fun add(e1: Long) {
        ensureBufferSpace(1)
        buffer[elementsCount++] = e1
    }

    operator fun get(index: Int): Long {

        assert(index >= 0 && index < size()){
            "Index " + index + " out of bounds [" + 0 + ", " + size() + ")."
        }

        return buffer[index]
    }

    operator fun set(index: Int, e1: Long): Long {
        assert(index >= 0 && index < size()) {
            "Index " + index + " out of bounds [" + 0 + ", " + size() + ")."
        }

        val v = buffer[index]
        buffer[index] = e1
        return v
    }

    operator fun contains(e1: Long): Boolean {
        return indexOf(e1) >= 0
    }

    fun indexOf(e1: Long): Int {
        for (i in 0 until elementsCount) {
            if (buffer[i] == e1) {
                return i
            }
        }
        return -1
    }

    val isEmpty: Boolean get() = elementsCount == 0

    /**
     * Ensure this container can hold at least the given number of elements
     * without resizing its buffers.
     *
     * @param expectedElements The total number of elements, inclusive.
     */
    fun ensureCapacity(expectedElements: Int) {
        val bufferLen = buffer.size
        if (expectedElements > bufferLen) {
            ensureBufferSpace(expectedElements - size())
        }
    }

    /**
     * Ensures the internal buffer has enough free slots to store
     * `expectedAdditions`. Increases internal buffer size if needed.
     */
    private fun ensureBufferSpace(expectedAdditions: Int) {
        val bufferLen =  buffer.size

        if (elementsCount + expectedAdditions > bufferLen) {

            val newSize = resizer.grow(bufferLen, elementsCount, expectedAdditions)

            assert(newSize >= elementsCount + expectedAdditions) {
                ("Resizer failed to" + " return sensible new size: " + newSize + " <= " + (elementsCount + expectedAdditions))
            }

            buffer = buffer.copyOf(newSize)
        }
    }

    /**
     * Truncate or expand the list to the new size. If the list is truncated, the
     * buffer will not be reallocated (use [.trimToSize] if you need a
     * truncated buffer), but the truncated values will be reset to the default
     * value (zero). If the list is expanded, the elements beyond the current size
     * are initialized with JVM-defaults (zero or `null` values).
     */
    fun resize(newSize: Int) {
        if (newSize <= buffer.size) {
            if (newSize < elementsCount) {
                Arrays.fill(buffer, newSize, elementsCount, 0)
            } else {
                Arrays.fill(buffer, elementsCount, newSize, 0)
            }
        } else {
            ensureCapacity(newSize)
        }

        elementsCount = newSize
    }

    fun size(): Int {
        return elementsCount
    }

    /**
     * Trim the internal buffer to the current size.
     */
    fun trimToSize() {
        if (size() != buffer!!.size) {
            buffer = toArray()
        }
    }

    /**
     * Sets the number of stored elements to zero. Releases and initializes the
     * internal storage array to default values. To clear the list without
     * cleaning the buffer, simply set the [.elementsCount] field to zero.
     */
    fun clear() {
        Arrays.fill(buffer, 0, elementsCount, 0)
        elementsCount = 0
    }

    /**
     * Sets the number of stored elements to zero and releases the internal
     * storage array.
     */
    fun release() {
        buffer = EMPTY_ARRAY
        elementsCount = 0
    }

    fun toArray(): LongArray {
        return buffer.copyOf(elementsCount)
    }

    companion object {
        /**
         * An immutable empty buffer (array).
         */
        val EMPTY_ARRAY = LongArray(0)
    }

}