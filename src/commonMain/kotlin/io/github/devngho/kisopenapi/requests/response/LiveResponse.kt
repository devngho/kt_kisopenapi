package io.github.devngho.kisopenapi.requests.response

import io.github.devngho.kisopenapi.requests.util.ResultCodeSerializer
import io.github.devngho.kisopenapi.requests.util.YNSerializer
import io.github.devngho.kisopenapi.requests.util.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

@Serializable
data class LiveResponse(
    val header: LiveResponseHeader?,
    val body: LiveResponseBody?,
    val output: LiveResponseBodyOutput?
)

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

@Serializable
data class LiveCallBody(
    val header: LiveCallHeader,
    val body: LiveCallBodyBody
) {
    companion object {
        fun buildCallBody(token: String, consumerType: String, trId: String, trKey: String, trType: String) =
            LiveCallBody(
                LiveCallHeader(
                    token,
                    consumerType,
                    trType,
                    "utf-8"
                ),
                LiveCallBodyBody(
                    LiveCallBodyInput(
                        trId,
                        trKey
                    )
                )
            ).let { json.encodeToString(it) }
    }
}

@Serializable
data class LiveCallHeader(
    @SerialName("approval_key") val approvalKey: String,
    @SerialName("custtype") val consumerType: String,
    @SerialName("tr_type") val trType: String,
    @SerialName("content-type") val contentType: String
)

@Serializable
data class LiveCallBodyBody(
    val input: LiveCallBodyInput
)

@Serializable
data class LiveCallBodyInput(
    @SerialName("tr_id") val trId: String,
    @SerialName("tr_key") val trKey: String
)