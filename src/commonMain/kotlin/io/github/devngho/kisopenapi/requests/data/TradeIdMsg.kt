package io.github.devngho.kisopenapi.requests.data

import kotlinx.serialization.SerialName

interface TradeIdMsg: Msg {
    @SerialName("tr_id") var tradeId: String?
    @SerialName("gt_uid") var globalTradeID: String?
}