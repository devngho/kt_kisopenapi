package io.github.devngho.kisopenapi.requests.util

import com.soywiz.krypto.AES
import com.soywiz.krypto.CipherPadding
import io.ktor.utils.io.core.*

object AES {
    fun decodeAES(key: String, iv: String, cipherText: ByteArray): String =
        AES.decryptAesCbc(cipherText, key.toByteArray(), iv.toByteArray(), padding = CipherPadding.NoPadding)
            .decodeToString()
}