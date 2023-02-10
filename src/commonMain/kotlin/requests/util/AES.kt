package io.github.devngho.kisopenapi.requests.util

import com.soywiz.krypto.*

fun decodeAES(key: String, iv: String, cipherText: ByteArray, cipherType: CipherMode = CipherMode.CBC) = AES(key.encodeToByteArray())[cipherType, CipherPadding.PKCS7Padding,iv.encodeToByteArray()].decrypt(cipherText)