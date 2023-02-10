package io.github.devngho.kisopenapi.requests.response

import io.github.devngho.kisopenapi.requests.util.ResultCodeSerializer
import io.github.devngho.kisopenapi.requests.util.YNSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LiveResponse(val header: LiveResponseHeader?, val body: LiveResponseBody?, val output: LiveResponseBodyOutput?)

@Serializable
data class LiveResponseHeader(
    @SerialName("tr_id") val tradeId: String?,
    @SerialName("tr_key") val tradeKey: String?,
    @SerialName("encrypt") @Serializable(with = YNSerializer::class) val isEncrypted: Boolean?
)

@Serializable
data class LiveResponseBody(
    @SerialName("rt_cd") @Serializable(with = ResultCodeSerializer::class) val isOk: Boolean?,
    @SerialName("msg_cd") val code: String?,
    @SerialName("msg1") val msg: String?,
    @SerialName("output") val output: LiveResponseBodyOutput?
)

@Serializable
data class LiveResponseBodyOutput(
    @SerialName("iv") val iv: String?,
    @SerialName("key") val key: String?,
)