@file:Suppress("SpellCheckingInspection")

package io.github.devngho.kisopenapi.requests.data

import kotlinx.serialization.SerialName

interface AccountInfo {
    @SerialName("CANO")
    val accountNumber: String?

    @SerialName("ACNT_PRDT_CD")
    val accountProductCode: String?
}