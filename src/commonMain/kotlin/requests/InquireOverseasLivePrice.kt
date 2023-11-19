package io.github.devngho.kisopenapi.requests

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.toBigInteger
import io.github.devngho.kisopenapi.KisOpenApi
import io.github.devngho.kisopenapi.requests.response.CorporationRequest
import io.github.devngho.kisopenapi.requests.response.LiveResponse
import io.github.devngho.kisopenapi.requests.response.StockOverseasPrice
import io.github.devngho.kisopenapi.requests.response.Ticker
import io.github.devngho.kisopenapi.requests.util.*
import io.github.devngho.kisopenapi.requests.util.HHMMSSSerializer.HHMMSS
import io.github.devngho.kisopenapi.requests.util.YYYYMMDDSerializer.YYYYMMDD
import io.ktor.websocket.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

class InquireOverseasLivePrice(override val client: KisOpenApi): LiveRequest<InquireOverseasLivePrice.InquireLivePriceData, InquireOverseasLivePrice.InquireLivePriceResponse> {
    @Suppress("SpellCheckingInspection")
    private fun buildCallBody(data: InquireLivePriceData, trType: String) = """
                {
                    "header": {
                        "approval_key":"${client.websocketToken}",
                        "custtype":"${data.corp!!.consumerType!!.num}",
                        "tr_type":"$trType",
                        "content-type": "utf-8"
                    },
                    "body": {
                        "input": {
                            "tr_id":"HDFSCNT0",
                            "tr_key":"${data.tradeKey(client)}"
                        }
                    }
                }
            """.trimIndent()

    @Serializable
    @Suppress("SpellCheckingInspection")
    data class InquireLivePriceResponse(
        @SerialName("RSYM") val liveLoadCode: String?,
        @SerialName("SYMB") override val ticker: String?,
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
    ) : Response, StockOverseasPrice, Ticker {
        @SerialName("error_description")
        override val errorDescription: String? = null

        @SerialName("error_code")
        override val errorCode: String? = null
    }

    data class InquireLivePriceData(
        override val ticker: String,
        val market: OverseasMarket,
        override var corp: CorporationRequest? = null
    ) : LiveData, Ticker {
        override fun tradeKey(client: KisOpenApi): String = "D${market.code}$ticker"
    }

    private lateinit var job: Job
    private lateinit var subscribed: KisOpenApi.WebSocketSubscribed

    @Suppress("SpellCheckingInspection", "unchecked_cast")
    override suspend fun register(data: InquireLivePriceData, init: ((LiveResponse) -> Unit)?, block: (InquireLivePriceResponse) -> Unit) {
        if (data.corp == null) data.corp = client.corp
        if (client.websocket == null) client.buildWebsocket()
        subscribed = KisOpenApi.WebSocketSubscribed(
            this@InquireOverseasLivePrice, data, init,
            block as (Response) -> Unit
        )

        client.websocket!!.run {
            job = launch {
                send(buildCallBody(data, "1"))

                client.websocketIncoming?.collect {
                    if (it[0] != '0' && it[0] != '1') {
                        json.decodeFromString<LiveResponse>(it).run {
                            if (
                                this.header?.tradeId == "HDFSCNT0" &&
                                this.header.tradeKey == data.tradeKey(client)
                                && init != null
                            ) {
                                client.subscribe(subscribed)
                                init(this)
                            }
                        }
                    } else {
                        it.split("|")
                            .takeIf { v -> v[1] == "HDFSCNT0" }
                            ?.let { v -> v[3].split("^") }
                            ?.takeIf { v -> v[0] == data.tradeKey(client) }
                            ?.run {
                                try {
                                    block(
                                        //<editor-fold desc="InquireLivePriceResponse 생성">
                                        InquireLivePriceResponse(
                                            this[0],
                                            this[1],
                                            this[2].toInt(),
                                            this[3].YYYYMMDD,
                                            this[4].YYYYMMDD,
                                            this[5].HHMMSS,
                                            this[6].YYYYMMDD,
                                            this[7].HHMMSS,
                                            this[8].toBigDecimal(),
                                            this[9].toBigDecimal(),
                                            this[10].toBigDecimal(),
                                            this[11].toBigDecimal(),
                                            SignPrice.entries.firstOrNull { it.value.toString() == this[12] },
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
                                            MarketStatus.entries.firstOrNull { f -> f.code == this[24] }
                                        )
                                        //</editor-fold>
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
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

        if (client.unsubscribe(subscribed)) client.websocket?.run {
            send(buildCallBody(data, "2"))
        }

        job.cancel()
    }
}