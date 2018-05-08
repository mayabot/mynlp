package fasttext


import com.google.common.base.Preconditions
import com.mayabot.mynlp.fasttext.*
import com.mayabot.mynlp.fasttext.Vector
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.util.*

/**
 * 一共可以分为多少块。99个，2个一份，应该为50份
 */
fun pages(total: Int, size: Int): Int = (total + size - 1) / size
fun pages(total: Long, size: Int): Int = ((total + size.toLong() - 1) / size.toLong()).toInt()

class QMatrix(val m: Int = 0, val n: Int = 0, val dsub: Int = 2, var qnorm: Boolean = false) {

    var qnorm_: Boolean = false

    var codesize_: Int = m * pages(n, dsub)

    var pq_ = ProductQuantizer(n, dsub)
    var codes_ = ShortArray(codesize_)

    var npq_: ProductQuantizer = ProductQuantizer(1,1)
    var norm_codes_: ShortArray = ShortArray(0)

    init {
        if (qnorm) {
            norm_codes_ = ShortArray(m)
            npq_ = ProductQuantizer(1, 1)
        }
    }

    fun quantize(matrix: MutableFloatMatrix) {
        Preconditions.checkArgument(m == matrix.rows())
        Preconditions.checkArgument(n == matrix.cols())

        if (qnorm_) {
            val norms = FloatArray(m)
            for (i in 0 until m) {
                norms[i] = matrix[i].norm2()
            }

            for (i in 0 until m) {
                matrix[i] /= norms[i]
            }
            quantizeNorm(norms)
        }

        pq_.train(matrix)
        pq_.compute_codes(matrix, codes_)
    }

    fun addToVector(x: MutableVector, t: Int) {
        var norm = 1.0f
        if (qnorm_) {
            norm = npq_.centroidTable.centroidData[npq_.get_centroids(0, norm_codes_[t])]
        }
        pq_.addCode(x, codes_, t, norm)
    }

    fun dotRow(vec: Vector, i: Int): Float {
        Preconditions.checkArgument(i >= 0 && i < m && vec.length() === n)

        var norm = 1f
        if (qnorm_) {
            norm = npq_.centroidTable.centroidData[npq_.get_centroids(0, norm_codes_[i])]
        }

        return pq_.mulCode(vec, codes_, i, norm)
    }

    private fun quantizeNorm(norms: FloatArray) {
        assert(qnorm_)
        assert(norms.size == m)

        val norms = FloatMatrix.readOnlyFloatArrayMatrix(m,1,norms)
        npq_.train(norms)
        npq_.compute_codes(norms, norm_codes_)
    }

    //    public void save(CLangDataOutputStream out) throws IOException{
    //        out.writeBoolean(qnorm_);
    //        out.writeLong(m_);
    //        out.writeLong(n_);
    //        out.writeInt(codesize_);
    //
    //        out.writeShortArray(codes_);
    //
    //        pq_.save(out);
    //        if (qnorm_) {
    //            out.writeShortArray(norm_codes_);
    //
    //            npq_.save(out);
    //        }
    //
    //    }

    fun save(channel: FileChannel) {
        with(channel) {
            writeBoolean(qnorm_)
            writeLong(m.toLong())
            writeLong(n.toLong())
            writeInt(codesize_)
            writeShortArray(codes_)

            pq_.save(channel)
            if (qnorm_) {
                writeShortArray(norm_codes_)
                npq_.save(channel)
            }
        }
    }

    companion object {
        @Throws(IOException::class)
        fun load(buffer: AutoDataInput): QMatrix{
            val qnorm_ = buffer.readUnsignedByte().toInt() != 0
            val m = buffer.readLong().toInt()
            val n = buffer.readLong().toInt()
            val codeSize = buffer.readInt()

            val codes = ShortArray(codeSize)
            buffer.readShortArray(codes)

            val qmatrix = QMatrix(m=m,n=n,dsub=2,qnorm = qnorm_ )
            qmatrix.codesize_ = codeSize
            qmatrix.codes_ = codes

            val pq_ = ProductQuantizer.loadFromBuffer(buffer)

            qmatrix.pq_ = pq_
            qmatrix.qnorm = qnorm_

            if (qnorm_) {
                val normCodes = ShortArray(m)
                buffer.readShortArray(normCodes)
                qmatrix.norm_codes_ = normCodes


                val npq = ProductQuantizer.loadFromBuffer(buffer)
                qmatrix.npq_ = npq
            }

            return qmatrix
        }
    }

}


/**
 * 乘积量化
 */
class ProductQuantizer(val dim: Int, val dsub: Int) {

    /**
     * dim/dsub 有几个子空间
     */
    var nsubq_: Int = pages(dim, dsub)

    /**
     * 最后一个子空间的维度
     */
    val lastdsub_: Int = if (dim % dsub == 0) dsub else dim % dsub

    private val random = Random(1234L)

    val centroidTable = CentroidTable(dim, ksub_, dsub)

    companion object {
        val nbits_ = 8
        val ksub_ = 1 shl nbits_
        val max_points_per_cluster_ = 256
        val max_points_ = max_points_per_cluster_ * ksub_

        fun loadFromBuffer(buffer: AutoDataInput): ProductQuantizer {
            val dim = buffer.readInt()
            val nsubq = buffer.readInt()
            val dsub = buffer.readInt()
            val lastdsub = buffer.readInt()

            val result = ProductQuantizer(dim, dsub)

            buffer.readFloatArray(result.centroidTable.centroidData)
            return result
        }
    }


    fun save(chan: FileChannel) {
        chan.write(ByteBuffer.allocate(4 * 4 + centroidTable.centroidData.size * 4).apply {
            putInt(dim)
            putInt(nsubq_)
            putInt(dsub)
            putInt(lastdsub_)
            writeFloatArray(centroidTable.centroidData)
        }.apply { flip() })
    }

    fun train(data: FloatMatrix) {
        // n 行数
        val np = Math.min(data.rows(), max_points_)

        val perm = IntArray(data.rows())
        iota(perm)

        var d = dsub
        val xslice = FloatMatrix.floatArrayMatrix(np,dsub)
        val xsliceData = xslice.data


        for (m in 0 until nsubq_) {
            if (m == nsubq_ - 1) {
                d = lastdsub_
            }
            if (np != data.rows()) {
                shuffle(perm, random)
            }

            // xlslice 抽样了第m个分片，d维度的数据
            var xp = 0
            for (j in 0 until np) {
                val row = perm[j]
                for (p in m * dsub until m * dsub + d) {
                    xsliceData[xp++] = data[row, p]
                }
            }

            centroidTable[m].kmeans(xslice)
        }
    }

    /**
     * @param data
     * @param codes
     * @param m     这个n是原始数据的行数
     */
    fun compute_codes(data: FloatMatrix, codes: ShortArray) {
        for (i in 0 until data.rows()) {
            val c = i * nsubq_

            val dataRow = data[i]
            for (m in 0 until nsubq_) {
                val mCentroid = centroidTable[m]
                val k = mCentroid.assignCentroid(dataRow, m * dsub).toInt()
                codes[c + m] = k.toShort()
            }
        }

    }

    fun get_centroids(m: Int, i: Short): Int {
        return if (m == nsubq_ - 1) {
            m * ksub_ * dsub + i * lastdsub_
        } else (m * ksub_ + i) * dsub
    }

    /**
     *
     * @param x
     * @param codes_
     * @param t 原始数据的行数
     * @param alpha
     */
    fun addCode(x: MutableVector, codes_: ShortArray, t: Int, alpha: Float) {
        var d = dsub
        val codeOffset = nsubq_ * t
        val centroidData = centroidTable.centroidData
        for (m in 0 until nsubq_) {
            val c = get_centroids(m, codes_[codeOffset + m])
            if (m == nsubq_ - 1) {
                d = lastdsub_
            }
            for (n in 0 until d) {
                try {

                    x[m * dsub + n] += alpha * centroidData[c + n]
                } catch (e: Exception) {
                    e.printStackTrace()

                }
            }
        }
    }

    fun mulCode(x: Vector, codes_: ShortArray, t: Int, alpha: Float): Float {
        var res = 0f
        var d = dsub
        val codeOffset = nsubq_ * t
        val centroidData = centroidTable.centroidData
        for (m in 0 until nsubq_) {
            val c = get_centroids(m, codes_[codeOffset + m])
            if (m == nsubq_ - 1) {
                d = lastdsub_
            }
            for (n in 0 until d) {
                res += x[m * dsub + n] * centroidData[c + n]
            }
        }
        return res * alpha
    }


}

/**
 * 质心Group.
 * 管理了M个分区的质心群
 *
 * @param dim  原始数据的维度
 * @param ksub 每个区，质心的数量，一般为2的次方。比如256
 */
class CentroidTable(
        dim: Int,

        /**
         * 每个区，质心的数量，一般为2的次方。比如256
         */
        internal var ksub: Int,
        /**
         * dsub 子空间的维度
         */
        internal var dsub: Int) {

    private val random = Random(1234L)

    val eps_ = 1e-7f

    var centroidData: FloatArray = FloatArray(dim * ksub)

    /**
     * dim/dsub 有几个子空间
     */
    val nsubq: Int = pages(dim,dsub)

    /**
     * 最后一个子空间的维度
     */
    val lastdsub: Int = if(dim%dsub==0) dsub else dim % dsub

    operator fun get(m: Int): MCentroid {
        return MCentroid(m)
    }

//    /**
//     * 训练第M个分区的数据
//     */
//    fun kmeans(m: Int, xslice: FloatMatrix) {
//        val mcen = this[m]
//        checkArgument(xslice.cols() == mcen.d)
//        mcen.kmeans(xslice)
//    }

    /**
     * 第M个区间的质心
     */
    inner class MCentroid(val m: Int) {

        private val start: Int =  m * ksub * dsub

        val d: Int = if (m == nsubq - 1) {
            lastdsub
        } else {
            dsub
        }


        fun kmeans(xslice: FloatMatrix) {
            val n = xslice.rows()
            val perm = IntArray(n)
            iota(perm)
            shuffle(perm, random)

            //随机选取256个点，作为初始化质心
            for (i in 0 until ksub) {
                // memcpy (&c[i * d], x + perm[i] * d, d * sizeof(real));
                //System.arraycopy(xslice, perm[i] * d, centroidData, start + i * d, d)
                val r = xslice[perm[i]]
                var s = start + i*d
                for (ii in 0 until d) {
                    centroidData[s++] = r[ii]
                }
            }

            val codes = ShortArray(n)
            for (q in 0 until niter_) {

                //记住每个向量和哪些之心最近
                for (i in 0 until n) {
                    codes[i] = assignCentroid(xslice[i],0)
                }

                //每个质心,坐标为和之有关的均值
                val nelts = IntArray(ksub)

                //质心=0
                for (i in start until ksub * d) {
                    centroidData[i] = 0f
                }

                //每个质心求和
                for (i in 0 until n) {
                    val k = codes[i].toInt()

                    var t=0
                    var r = xslice[i]
                    var j = start + k * d
                    val max = start + k * d + d
                    while (j < max) {
                        centroidData[j] += r[t++]
                        j++
                    }
                    //求和
                    nelts[k]++
                }

                //平均数
                var c = start
                for (k in 0 until ksub) {
                    val z = nelts[k]
                    if (z != 0) {
                        for (j in 0 until d) {
                            centroidData[c++] /= z.toFloat()
                        }
                    } else {
                        c += d
                    }
                }

                //如果质心没有绑定到最近的,随机分配一个
                for (k in 0 until ksub) {
                    if (nelts[k] == 0) {
                        var m = 0
                        while (random.nextFloat() * (n - ksub) >= nelts[m] - 1) {
                            m = (m + 1) % ksub
                        }
                        System.arraycopy(centroidData, start + m * d, centroidData, start + k * d, d)
                        for (j in 0 until d) {
                            val sign = j % 2 * 2 - 1
                            centroidData[start + k * d + j] += sign * eps_
                            centroidData[start + m * d + j] -= sign * eps_
                        }
                        nelts[k] = nelts[m] / 2
                        nelts[m] -= nelts[k]
                    }
                }
            }
        }

        /**
         * 返回地i个质心在数据data中的位置
         *
         * @param i
         * @return
         */
        private fun index(i: Int): Int {
            return start + i * d
        }

        /**
         * 计算出给定的向量和这些质心之间，那个最近
         *
         * @param data
         * @param offset
         * @return 质心点的下标
         */
        fun assignCentroid(data: Vector,offset:Int): Short {
            var dis = distL2(data,offset,0)
            var code: Short = 0
            for (j in 1 until ksub) {
                val disij = distL2(data,offset,j)
                if (disij < dis) {
                    code = j.toShort()
                    dis = disij
                }
            }
            return code
        }


        fun distL2(dataRow: Vector,offset: Int, iZ: Int): Float {
            var dist = 0f
            var j = index(iZ)
            for (i in offset until offset+d) {
                val tmp = dataRow[i] - centroidData[j]
                dist += tmp * tmp
                j++
            }
            return dist
        }

    }

    companion object {
        val niter_ = 25
    }

}