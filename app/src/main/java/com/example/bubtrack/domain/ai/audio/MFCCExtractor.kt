package com.example.bubtrack.domain.ai.audio

import kotlin.math.*

class MFCCExtractor(
    private val sampleRate: Int = 22050,
    private val nMfcc: Int = 40,
    private val nFft: Int = 512,
    private val hopLength: Int = 256,
    private val nMel: Int = 40
) {
    fun extract(signalShort: ShortArray): FloatArray {
        if (signalShort.isEmpty()) return FloatArray(nMfcc) { 0f }

        // Convert to double [-1..1]
        val signal = DoubleArray(signalShort.size) { i -> signalShort[i] / 32768.0 }

        // Framing & windowing (Hamming)
        val frames = mutableListOf<DoubleArray>()
        var pos = 0
        while (pos + nFft <= signal.size) {
            val frame = DoubleArray(nFft)
            for (i in 0 until nFft) {
                frame[i] = signal[pos + i] * hamming(i, nFft)
            }
            frames.add(frame)
            pos += hopLength
        }

        // Handle case where signal is shorter than nFft
        if (frames.isEmpty()) {
            val padded = DoubleArray(nFft)
            for (i in 0 until min(signal.size, nFft)) {
                padded[i] = signal[i] * hamming(i, nFft)
            }
            frames.add(padded)
        }

        // FFT -> power spectrogram
        val fft = FFT(nFft)
        val specList = frames.map { frame ->
            val re = frame.copyOf()
            val im = DoubleArray(nFft) { 0.0 }
            fft.transform(re, im)

            // FIX: Properly handle FFT output size
            val specSize = nFft / 2 + 1
            DoubleArray(specSize) { k ->
                if (k < re.size && k < im.size) {
                    val real = re[k]
                    val imag = im[k]
                    (real * real + imag * imag) / nFft
                } else {
                    0.0
                }
            }
        }

        // Mel filterbank
        val filterbank = melFilterBank(nMel, nFft, sampleRate)
        val melSpectrogram = specList.map { spec ->
            DoubleArray(nMel) { m ->
                var sum = 0.0
                val filt = filterbank[m]
                // FIX: Ensure we don't access out of bounds
                val len = min(filt.size, spec.size)
                for (k in 0 until len) {
                    if (k < spec.size && k < filt.size) {
                        sum += spec[k] * filt[k]
                    }
                }
                max(sum, 1e-10)
            }
        }

        // Log and DCT
        val mfccList = melSpectrogram.map { mel ->
            val logMel = DoubleArray(mel.size) { i -> ln(mel[i]) }
            dct(logMel, nMfcc)
        }

        // Mean across time axis
        val avg = DoubleArray(nMfcc) { 0.0 }
        if (mfccList.isNotEmpty()) {
            for (c in mfccList) {
                for (i in 0 until min(c.size, avg.size)) {
                    avg[i] += c[i]
                }
            }
            for (i in avg.indices) {
                avg[i] = avg[i] / mfccList.size.toDouble()
            }
        }

        return FloatArray(nMfcc) { i ->
            if (i < avg.size) avg[i].toFloat() else 0f
        }
    }

    private fun hamming(n: Int, N: Int): Double =
        if (N <= 1) 1.0 else 0.54 - 0.46 * cos(2.0 * Math.PI * n / (N - 1))

    private fun dct(input: DoubleArray, numCep: Int): DoubleArray {
        val N = input.size
        val out = DoubleArray(numCep) { 0.0 }
        for (k in 0 until numCep) {
            var sum = 0.0
            for (n in 0 until N) {
                sum += input[n] * cos(Math.PI * k * (2 * n + 1) / (2.0 * N))
            }
            out[k] = sum
        }
        return out
    }

    private fun melFilterBank(nMel: Int, nFft: Int, sr: Int): Array<DoubleArray> {
        val lowMel = hzToMel(0.0)
        val highMel = hzToMel(sr / 2.0)
        val melPoints = DoubleArray(nMel + 2) { i ->
            lowMel + (highMel - lowMel) * i / (nMel + 1)
        }
        val hzPoints = melPoints.map { melToHz(it) }
        val bins = hzPoints.map {
            floor((nFft + 1) * it / sr).toInt().coerceIn(0, nFft / 2)
        }

        val fbank = Array(nMel) { DoubleArray(nFft / 2 + 1) { 0.0 } }

        for (m in 1..nMel) {
            val f_m_minus = bins[m - 1]
            val f_m = bins[m]
            val f_m_plus = bins[m + 1]

            val denom1 = (f_m - f_m_minus).takeIf { it != 0 } ?: 1
            val denom2 = (f_m_plus - f_m).takeIf { it != 0 } ?: 1

            // FIX: Add bounds checking
            for (k in f_m_minus until f_m) {
                if (k >= 0 && k < fbank[m - 1].size) {
                    fbank[m - 1][k] = (k - f_m_minus).toDouble() / denom1
                }
            }
            for (k in f_m until f_m_plus) {
                if (k >= 0 && k < fbank[m - 1].size) {
                    fbank[m - 1][k] = (f_m_plus - k).toDouble() / denom2
                }
            }
        }
        return fbank
    }

    private fun hzToMel(hz: Double) = 2595.0 * ln(1.0 + hz / 700.0)
    private fun melToHz(mel: Double) = 700.0 * (exp(mel / 2595.0) - 1.0)
}