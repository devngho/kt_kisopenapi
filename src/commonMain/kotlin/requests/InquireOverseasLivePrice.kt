package io.github.devngho.kisopenapi.requests

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.toBigInteger
import io.github.devngho.kisopenapi.KisOpenApi
import io.github.devngho.kisopenapi.requests.response.CorporationRequest
import io.github.devngho.kisopenapi.requests.response.LiveResponse
import io.github.devngho.kisopenapi.requests.response.StockOverseasPrice
import io.github.devngho.kisopenapi.requests.util.*
import io.github.devngho.kisopenapi.requests.util.HHMMSSSerializer.HH_MM_SS
import io.github.devngho.kisopenapi.requests.util.YYYYMMDDSerializer.YYYY_MM_DD
import io.ktor.websocket.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

class InquireOverseasLivePrice(override val client: KisOpenApi): LiveRequest<InquireOverseasLivePrice.InquireLivePriceData, InquireOverseasLivePrice.InquireLivePriceResponse> {
    private fun buildCallBody(data: InquireLivePriceData, trType: String) = """
                {
                    "header": {
                        "approval_key":"${client.websocketToken}",
                        "custtype":"${data.corp!!.consumerType!!.num}",
                        "tr_type":"$trType"
                    },
                    "body": {
                        "input": {
                            "tr_id":"HDFSCNT0",
                            "tr_key":"D${data.market.code}${data.stockCode}"
                        }
                    }
                }
            """.trimIndent()

    @Serializable
    data class InquireLivePriceResponse(
        @SerialName("RSYM") val liveLoadCode: String?,
        @SerialName("SYMB") val stockCode: String?,
        @SerialName("ZDIV") override val decimalPoint: Int?,
        @SerialName("TYMD") val localMarketDate: Date?,
        @SerialName("XYMD") val localDate: Date?,
        @SerialName("XHMS") val localTime: Time?,
        @SerialName("KYMD") val koreaDate: Date?,
        @SerialName("KHMS") val koreaTime: Time?,
        @SerialName("OPEN") @Contextual val marketPrice: BigDecimal?,
        @SerialName("HIGH") @Contextual val highPrice: BigDecimal?,
        @SerialName("LOW") @Contextual val lowPrice: BigDecimal?,
        @SerialName("LAST") @Contextual override val price: BigDecimal?,
        @SerialName("SIGN") @Contextual override val sign: SignPrice?,
        @SerialName("DIFF") @Contextual override val changeFromYesterday: BigDecimal?,
        @SerialName("RATE") @Contextual override val rateFromYesterday: BigDecimal?,
        @SerialName("PBID") @Contextual val bidPrice: BigDecimal?,
        @SerialName("PASK") @Contextual val askPrice: BigDecimal?,
        @SerialName("VBID") @Contextual val bidCount: BigInteger?,
        @SerialName("VASK") @Contextual val askCount: BigInteger?,
        @SerialName("EVOL") @Contextual val confirmVolume: BigInteger?,
        @SerialName("TVOL") @Contextual override val tradeVolume: BigInteger?,
        @SerialName("TAMT") @Contextual override val tradePriceVolume: BigDecimal?,
        @SerialName("BIVL") @Contextual val buyConfirmVolume: BigInteger?,
        @SerialName("ASVL") @Contextual val sellConfirmVolume: BigInteger?,
        @SerialName("STRN") @Contextual val confirmStrength: BigDecimal?,
        @SerialName("MTYP") val marketStatus: MarketStatus?,
    ): Response, StockOverseasPrice {
        override val errorDescription: String? = null
        override val errorCode: String? = null
    }

    data class InquireLivePriceData(val stockCode: String, val market: OverseasMarket, override var corp: CorporationRequest? = null): Data


    override suspend fun register(data: InquireLivePriceData, init: ((LiveResponse) -> Unit)?, block: (InquireLivePriceResponse) -> Unit) {
        if (data.corp == null) data.corp = client.corp
        if (client.websocket == null) client.buildWebsocket()

        client.websocket?.run {
            send(buildCallBody(data, "1"))
            launch {
                client.websocketIncoming?.collect {
                    if (it is Frame.Text) {
                        it.readText()
                            .apply {
                                if (this[0] != '0' && this[0] != '1') {
                                    json.decodeFromString<LiveResponse>(this).run {
                                        if (this.header?.tradeId == "HDFSCNT0" && this.header.tradeKey == data.stockCode && init != null) init(
                                            this
                                        )
                                    }

                                    return@collect
                                }
                            }
                            .run {
                                split("|")
                                    .run {
                                        if (get(1) == "HDFSCNT0") get(3).split("^")
                                        else return@collect
                                    }
                                    .run {
                                        if (get(0) == "D${data.market.code}${data.stockCode}") this
                                        else return@collect
                                    }
                                    .run {
                                        try {
                                            block(
                                                InquireLivePriceResponse(
                                                    this[0],
                                                    this[1],
                                                    this[2].toInt(),
                                                    this[3].YYYY_MM_DD,
                                                    this[4].YYYY_MM_DD,
                                                    this[5].HH_MM_SS,
                                                    this[6].YYYY_MM_DD,
                                                    this[7].HH_MM_SS,
                                                    this[8].toBigDecimal(),
                                                    this[9].toBigDecimal(),
                                                    this[10].toBigDecimal(),
                                                    this[11].toBigDecimal(),
                                                    SignPrice.values().firstOrNull { it.value.toString() == this[12] },
                                                    // 변화량이 음수인 경우에도 changeFromYesterday 값이 양수로 반환됨
                                                    // 편의성 위해 변동률 음수인 경우 changeFromYesterday 값도 음수로 변환함
                                                    if (this[14].startsWith("-")) this[13].toBigDecimal() * -1 else this[13].toBigDecimal(),
                                                    this[14].toBigDecimal(),
                                                    this[15].toBigDecimal(),
                                                    this[16].toBigDecimal(),
                                                    this[17].toBigInteger(),
                                                    this[18].toBigInteger(),
                                                    this[19].toBigInteger(),
                                                    this[20].toBigInteger(),
                                                    this[21].toBigDecimal(),
                                                    this[22].toBigInteger(),
                                                    this[23].toBigInteger(),
                                                    this[24].toBigDecimal(),
                                                    MarketStatus.values().firstOrNull { f -> f.code == this[24] }
                                                )
                                            )
                                        }catch (e: Exception) {e.printStackTrace()}
                                    }
                            }
                    }
                }
            }
        }
    }

    override suspend fun unregister(data: InquireLivePriceData) {
        if (data.corp == null) data.corp = client.corp
        if (client.websocket == null) client.buildWebsocket()

        client.websocket?.run {
            send(buildCallBody(data, "2"))
        }
    }
}