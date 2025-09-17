package com.example.bubtrack.domain.ai.audio

import kotlin.math.cos
import kotlin.math.sin

class FFT(private val n: Int) {
    private val cosTable = DoubleArray(n / 2)
    private val sinTable = DoubleArray(n / 2)

    init {
        for (i in 0 until n / 2) {
            cosTable[i] = cos(2.0 * Math.PI * i / n)
            sinTable[i] = sin(2.0 * Math.PI * i / n)
        }
    }

    fun transform(realIn: DoubleArray, imagIn: DoubleArray) {
        val n = realIn.size
        if (n != this.n) throw IllegalArgumentException("FFT size mismatch: expected $n, got ${this.n}")

        val real = realIn.copyOf()
        val imag = imagIn.copyOf()

        // Bit reversal
        var j = 0
        for (i in 1 until n - 1) {
            var bit = n shr 1
            while (j >= bit) {
                j -= bit
                bit = bit shr 1
            }
            j += bit

            if (i < j) {
                val tr = real[i]
                val ti = imag[i]
                real[i] = real[j]
                imag[i] = imag[j]
                real[j] = tr
                imag[j] = ti
            }
        }

        // Cooley-Tukey FFT
        var size = 2
        while (size <= n) {
            val halfsize = size / 2
            val tablestep = n / size

            var idx = 0
            while (idx < n) {
                var k = 0
                for (jstep in 0 until halfsize) {
                    val i1 = idx + jstep
                    val i2 = idx + jstep + halfsize

                    // Bounds checking
                    if (i1 >= n || i2 >= n || k >= cosTable.size || k >= sinTable.size) {
                        break
                    }

                    val tpre = real[i2] * cosTable[k] + imag[i2] * sinTable[k]
                    val tpim = -real[i2] * sinTable[k] + imag[i2] * cosTable[k]

                    real[i2] = real[i1] - tpre
                    imag[i2] = imag[i1] - tpim
                    real[i1] += tpre
                    imag[i1] += tpim

                    k += tablestep
                    // Additional bounds check for k
                    if (k >= cosTable.size) k = k % cosTable.size
                }
                idx += size
            }
            size *= 2
        }

        // Copy results back with bounds checking
        for (m in 0 until minOf(real.size, realIn.size)) {
            realIn[m] = real[m]
            imagIn[m] = imag[m]
        }
    }
}