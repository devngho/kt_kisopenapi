package io.github.devngho.kisopenapi.requests.util

import io.ktor.utils.io.core.*

/**
 * Copied from krypto library(at krypto/src/commonMain/kotlin/com/soywiz/krypto/internal/KryptoTools.kt of https://github.com/soywiz-archive/krypto/tree/master).
 * Removed unused functions/classes in this file.
 *
 * -- Original License --
 *
 * MIT License
 *
 * Copyright (c) 2017 Carlos Ballesteros Velasco
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
internal fun Int.ext8(offset: Int) = (this ushr offset) and 0xFF

internal fun arraycopy(src: ByteArray, srcPos: Int, dst: ByteArray, dstPos: Int, count: Int) =
    src.copyInto(dst, dstPos, srcPos, srcPos + count)

@Suppress("UNUSED_CHANGED_VALUE", "SpellCheckingInspection", "Unused")
/**
 * Copied from krypto library(at krypto/src/commonMain/kotlin/com/soywiz/krypto/AES.kt of https://github.com/soywiz-archive/krypto/tree/master).
 * Renamed to KryptoAES and changed access modifier to private, removed unused functions/classes in this file.
 * Added some suppress annotations.
 *
 * -- Original License --
 *
 * MIT License
 *
 * Copyright (c) 2017 Carlos Ballesteros Velasco
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * Based on CryptoJS v3.1.2
 * code.google.com/p/crypto-js
 * (c) 2009-2013 by Jeff Mott. All rights reserved.
 * code.google.com/p/crypto-js/wiki/License
 */
private class KryptoAES(val keyWords: IntArray) {
    private val keySize = keyWords.size
    private val numRounds = keySize + 6
    private val ksRows = (numRounds + 1) * 4
    private val keySchedule = IntArray(ksRows).apply {
        for (ksRow in 0 until size) {
            this[ksRow] = if (ksRow < keySize) {
                keyWords[ksRow]
            } else {
                var t = this[ksRow - 1]
                if (0 == (ksRow % keySize)) {
                    t = (t shl 8) or (t ushr 24)
                    t =
                        (SBOX[t.ext8(24)] shl 24) or (SBOX[t.ext8(16)] shl 16) or (SBOX[t.ext8(8)] shl 8) or SBOX[t and 0xff]
                    t = t xor (RCON[(ksRow / keySize) or 0] shl 24)
                } else if (keySize > 6 && ksRow % keySize == 4) {
                    t =
                        (SBOX[t.ext8(24)] shl 24) or (SBOX[t.ext8(16)] shl 16) or (SBOX[t.ext8(8)] shl 8) or SBOX[t and 0xff]
                }
                this[ksRow - keySize] xor t
            }
        }
    }
    private val invKeySchedule = IntArray(ksRows).apply {
        for (invKsRow in 0 until size) {
            val ksRow = ksRows - invKsRow
            val t = if ((invKsRow % 4) != 0) keySchedule[ksRow] else keySchedule[ksRow - 4]
            this[invKsRow] =
                if (invKsRow < 4 || ksRow <= 4) t else INV_SUB_MIX_0[SBOX[t.ext8(24)]] xor INV_SUB_MIX_1[SBOX[t.ext8(16)]] xor INV_SUB_MIX_2[SBOX[t.ext8(
                    8
                )]] xor INV_SUB_MIX_3[SBOX[t and 0xff]]
        }
    }

    constructor(key: ByteArray) : this(key.toIntArray())

    fun decryptBlock(M: IntArray, offset: Int) {
        var t = M[offset + 1]
        M[offset + 1] = M[offset + 3]
        M[offset + 3] = t
        this.doCryptBlock(
            M,
            offset,
            this.invKeySchedule,
            INV_SUB_MIX_0,
            INV_SUB_MIX_1,
            INV_SUB_MIX_2,
            INV_SUB_MIX_3,
            INV_SBOX
        )
        t = M[offset + 1]
        M[offset + 1] = M[offset + 3]
        M[offset + 3] = t
    }

    @Suppress("SameParameterValue", "LocalVariableName")
    private fun doCryptBlock(
        M: IntArray,
        offset: Int,
        keySchedule: IntArray,
        SUB_MIX_0: IntArray,
        SUB_MIX_1: IntArray,
        SUB_MIX_2: IntArray,
        SUB_MIX_3: IntArray,
        SBOX: IntArray
    ) {
        var s0 = M[offset + 0] xor keySchedule[0]
        var s1 = M[offset + 1] xor keySchedule[1]
        var s2 = M[offset + 2] xor keySchedule[2]
        var s3 = M[offset + 3] xor keySchedule[3]
        var ksRow = 4

        for (round in 1 until numRounds) {
            val t0 =
                SUB_MIX_0[s0.ext8(24)] xor SUB_MIX_1[s1.ext8(16)] xor SUB_MIX_2[s2.ext8(8)] xor SUB_MIX_3[s3.ext8(0)] xor keySchedule[ksRow++]
            val t1 =
                SUB_MIX_0[s1.ext8(24)] xor SUB_MIX_1[s2.ext8(16)] xor SUB_MIX_2[s3.ext8(8)] xor SUB_MIX_3[s0.ext8(0)] xor keySchedule[ksRow++]
            val t2 =
                SUB_MIX_0[s2.ext8(24)] xor SUB_MIX_1[s3.ext8(16)] xor SUB_MIX_2[s0.ext8(8)] xor SUB_MIX_3[s1.ext8(0)] xor keySchedule[ksRow++]
            val t3 =
                SUB_MIX_0[s3.ext8(24)] xor SUB_MIX_1[s0.ext8(16)] xor SUB_MIX_2[s1.ext8(8)] xor SUB_MIX_3[s2.ext8(0)] xor keySchedule[ksRow++]
            s0 = t0; s1 = t1; s2 = t2; s3 = t3
        }

        val t0 =
            ((SBOX[s0.ext8(24)] shl 24) or (SBOX[s1.ext8(16)] shl 16) or (SBOX[s2.ext8(8)] shl 8) or SBOX[s3.ext8(0)]) xor keySchedule[ksRow++]
        val t1 =
            ((SBOX[s1.ext8(24)] shl 24) or (SBOX[s2.ext8(16)] shl 16) or (SBOX[s3.ext8(8)] shl 8) or SBOX[s0.ext8(0)]) xor keySchedule[ksRow++]
        val t2 =
            ((SBOX[s2.ext8(24)] shl 24) or (SBOX[s3.ext8(16)] shl 16) or (SBOX[s0.ext8(8)] shl 8) or SBOX[s1.ext8(0)]) xor keySchedule[ksRow++]
        val t3 =
            ((SBOX[s3.ext8(24)] shl 24) or (SBOX[s0.ext8(16)] shl 16) or (SBOX[s1.ext8(8)] shl 8) or SBOX[s2.ext8(0)]) xor keySchedule[ksRow++]
        M[offset + 0] = t0; M[offset + 1] = t1; M[offset + 2] = t2; M[offset + 3] = t3
    }


    companion object {
        private val SBOX = IntArray(256)
        private val INV_SBOX = IntArray(256)
        private val SUB_MIX_0 = IntArray(256)
        private val SUB_MIX_1 = IntArray(256)
        private val SUB_MIX_2 = IntArray(256)
        private val SUB_MIX_3 = IntArray(256)
        private val INV_SUB_MIX_0 = IntArray(256)
        private val INV_SUB_MIX_1 = IntArray(256)
        private val INV_SUB_MIX_2 = IntArray(256)
        private val INV_SUB_MIX_3 = IntArray(256)
        private val RCON = intArrayOf(0x00, 0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80, 0x1b, 0x36)

        private const val BLOCK_SIZE = 16

        init {
            val d = IntArray(256) {
                if (it >= 128) (it shl 1) xor 0x11b else (it shl 1)
            }

            var x = 0
            var xi = 0
            for (i in 0 until 256) {
                var sx = xi xor (xi shl 1) xor (xi shl 2) xor (xi shl 3) xor (xi shl 4)
                sx = (sx ushr 8) xor (sx and 0xff) xor 0x63
                SBOX[x] = sx
                INV_SBOX[sx] = x
                val x2 = d[x]
                val x4 = d[x2]
                val x8 = d[x4]
                var t: Int
                t = (d[sx] * 0x101) xor (sx * 0x1010100)
                SUB_MIX_0[x] = (t shl 24) or (t ushr 8)
                SUB_MIX_1[x] = (t shl 16) or (t ushr 16)
                SUB_MIX_2[x] = (t shl 8) or (t ushr 24)
                SUB_MIX_3[x] = (t shl 0)
                t = (x8 * 0x1010101) xor (x4 * 0x10001) xor (x2 * 0x101) xor (x * 0x1010100)
                INV_SUB_MIX_0[sx] = (t shl 24) or (t ushr 8)
                INV_SUB_MIX_1[sx] = (t shl 16) or (t ushr 16)
                INV_SUB_MIX_2[sx] = (t shl 8) or (t ushr 24)
                INV_SUB_MIX_3[sx] = (t shl 0)

                if (x == 0) {
                    x = 1; xi = 1
                } else {
                    x = x2 xor d[d[d[x8 xor x2]]]
                    xi = xi xor d[d[xi]]
                }
            }
        }

        private fun ByteArray.toIntArray(): IntArray {
            val out = IntArray(size / 4)
            var m = 0
            for (n in 0 until out.size) {
                val v3 = this[m++].toInt() and 0xFF
                val v2 = this[m++].toInt() and 0xFF
                val v1 = this[m++].toInt() and 0xFF
                val v0 = this[m++].toInt() and 0xFF
                out[n] = (v0 shl 0) or (v1 shl 8) or (v2 shl 16) or (v3 shl 24)
            }
            return out
        }

        private fun IntArray.toByteArray(): ByteArray {
            val out = ByteArray(size * 4)
            var m = 0
            for (n in 0 until size) {
                val v = this[n]
                out[m++] = ((v shr 24) and 0xFF).toByte()
                out[m++] = ((v shr 16) and 0xFF).toByte()
                out[m++] = ((v shr 8) and 0xFF).toByte()
                out[m++] = ((v shr 0) and 0xFF).toByte()
            }
            return out
        }

        private fun getIV(srcIV: ByteArray?): ByteArray {
            val dstIV = ByteArray(16)
            srcIV?.apply {
                val min = if (size < dstIV.size) size else dstIV.size
                arraycopy(srcIV, 0, dstIV, 0, min)
            }
            return dstIV
        }

        fun decryptAesCbc(data: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
            val aes = KryptoAES(key)
            val dataWords = data.toIntArray()
            val wordsLength = dataWords.size
            val ivWords = getIV(iv).toIntArray()

            var s0 = ivWords[0]
            var s1 = ivWords[1]
            var s2 = ivWords[2]
            var s3 = ivWords[3]

            for (n in 0 until wordsLength step 4) {
                val t0 = dataWords[n + 0]
                val t1 = dataWords[n + 1]
                val t2 = dataWords[n + 2]
                val t3 = dataWords[n + 3]

                aes.decryptBlock(dataWords, n)

                dataWords[n + 0] = dataWords[n + 0] xor s0
                dataWords[n + 1] = dataWords[n + 1] xor s1
                dataWords[n + 2] = dataWords[n + 2] xor s2
                dataWords[n + 3] = dataWords[n + 3] xor s3

                s0 = t0
                s1 = t1
                s2 = t2
                s3 = t3
            }
            return dataWords.toByteArray()
        }
    }
}

object AES {
    /**
     * AES 복호화를 수행해 반환합니다.
     *
     * @param cipherText 암호화된 텍스트
     * @param key 키
     * @param iv 초기화 벡터
     */
    fun decodeAES(key: String, iv: String, cipherText: ByteArray): String =
        KryptoAES.decryptAesCbc(cipherText, key.toByteArray(), iv.toByteArray())
            .decodeToString()
}