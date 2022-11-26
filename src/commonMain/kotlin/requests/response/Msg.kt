package io.github.devngho.kisopenapi.requests.response

import kotlinx.serialization.SerialName

interface Msg {
    @SerialName("tr_id") var tradeId: String?
    @SerialName("gt_uid") var globalTradeID: String?
    @SerialName("msg_cd") val code: String?
    @SerialName("msg1") val msg: String?
}