package io.github.devngho.kisopenapi.requests.util

import com.soywiz.krypto.AES
import com.soywiz.krypto.CipherPadding
import io.ktor.utils.io.core.*

object AES {
    /**
     * AES 복호화를 수행해 반환합니다.
     *
     * @param cipherText 암호화된 텍스트
     * @param key 키
     * @param iv 초기화 벡터
     */
    fun decodeAES(key: String, iv: String, cipherText: ByteArray): String =
        AES.decryptAesCbc(cipherText, key.toByteArray(), iv.toByteArray(), padding = CipherPadding.NoPadding)
            .decodeToString()
}