package io.github.devngho.kisopenapi.requests

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.toBigInteger
import com.soywiz.krypto.encoding.Base64
import io.github.devngho.kisopenapi.KisOpenApi
import io.github.devngho.kisopenapi.requests.response.CorporationRequest
import io.github.devngho.kisopenapi.requests.response.LiveResponse
import io.github.devngho.kisopenapi.requests.util.*
import io.github.devngho.kisopenapi.requests.util.YNSerializer.YN
import io.ktor.websocket.*
import kotlinx.coroutines.launch
import kotlinx.serialization.*

class InquireLiveConfirm(override val client: KisOpenApi): LiveRequest<InquireLiveConfirm.InquireLiveConfirmData, InquireLiveConfirm.InquireLiveConfirmResponse> {
    private fun buildCallBody(data: InquireLiveConfirmData, trType: String) = """
                {
                    "header": {
                        "approval_key":"${client.websocketToken}",
                        "custtype":"${data.corp!!.consumerType!!.num}",
                        "tr_type":"$trType"
                    },
                    "body": {
                        "input": {
                            "tr_id": "${if(client.isDemo) "H0STCNI9" else "H0STCNI0"}",
                            "tr_key":"${client.htsId}"
                        }
                    }
                }
            """.trimIndent()

    @Serializable
    data class InquireLiveConfirmResponse(
        @SerialName("mksc_shrn_iscd") val stockShortCode: String?  = null,
        @SerialName("stck_cntg_hour") val stockConfirmTime: String?  = null,
        @SerialName("stck_prpr") @Contextual val price: BigInteger?  = null,
        @SerialName("prdy_vrss_sign") val signFromYesterday: SignPrice?  = null,
        @SerialName("prdy_vrss") @Contextual val changeFromYesterday: BigInteger?  = null,
        @SerialName("prdy_ctrt") @Contextual val rateFromYesterday: BigDecimal?  = null,
        @SerialName("wghn_avrg_stck_prc") @Contextual val weightedAverageStockPrice: BigDecimal?  = null,
        @SerialName("stck_oprc") @Contextual val marketPrice: BigInteger?  = null,
        @SerialName("stck_hgpr") @Contextual val highPrice: BigInteger?  = null,
        @SerialName("stck_lwpr") @Contextual val lowPrice: BigInteger?  = null,
        @SerialName("askp_rsqn1") @Contextual val sellAskPriceCount1: BigInteger?  = null,
        @SerialName("bidp_rsqn1") @Contextual val buyAskPriceCount1: BigInteger?  = null,
        @SerialName("cntg_vol") @Contextual val confirmTradeVolume: BigInteger?  = null,
        @SerialName("acml_vol") @Contextual val accumulateTradeVolume: BigInteger?  = null,
        @SerialName("acml_tr_pbmn") @Contextual val accumulateTradePrice: BigInteger?  = null,
        @SerialName("seln_cntg_csnu") @Contextual val sellConfirmCount: BigInteger?  = null,
        @SerialName("shnu_cntg_csnu") @Contextual val buyConfirmCount: BigInteger?  = null,
        @SerialName("ntby_cntg_csnu") @Contextual val naturalConfirmCount: BigInteger?  = null,
        @SerialName("cttr") @Contextual val confirmStrength: BigDecimal?  = null,
        @SerialName("total_askp_rsqn") @Contextual val totalSellAskPriceCount: BigInteger?  = null,
        @SerialName("total_bidp_rsqn") @Contextual val totalBuyAskPriceCount: BigInteger?  = null,
        @SerialName("ccld_dvsn") val confirmDivision: String?  = null,
        @SerialName("shnu_rate") @Contextual val buyRate: BigDecimal?  = null,
        @SerialName("prdy_vol_vrss_acml_vol_rate") @Contextual val rateTradeVolumeFromYesterday: BigDecimal?  = null,
        @SerialName("oprc_hour") val marketPriceTime: String?  = null,
        @SerialName("oprc_vrss_prpr_sign") val differentMarketPriceSign: SignPrice?  = null,
        @SerialName("oprc_vrss_prpr") @Contextual val differentMarketPrice: BigInteger?  = null,
        @SerialName("hgpr_hour") val highPriceTime: String?  = null,
        @SerialName("hgpr_vrss_prpr_sign") val differentHighPriceSign: SignPrice?  = null,
        @SerialName("hgpr_vrss_prpr") @Contextual val differentHighPrice: BigInteger?  = null,
        @SerialName("lwpr_hour") val lowPriceTime: String?  = null,
        @SerialName("lwpr_vrss_prpr_sign") val differentLowPriceSign: SignPrice?  = null,
        @SerialName("lwpr_vrss_prpr") @Contextual val differentLowPrice: BigInteger?  = null,
        @SerialName("bsop_date") val bizDate: String?  = null,
        @SerialName("new_mkop_cls_code") val newMarketDivisionCode: String?  = null,
        @SerialName("trht_yn") @Serializable(with = YNSerializer::class) val isTradeStopped: Boolean? = null,
        @SerialName("askp1") @Contextual val sellAskPrice1: BigInteger?  = null,
        @SerialName("bidp1") @Contextual val buyAskPrice1: BigInteger?  = null,
        @SerialName("seln_cntg_smtn") @Contextual val sellTotalCount: BigInteger?  = null,
        @SerialName("shnu_cntg_smtn") @Contextual val buyTotalCount: BigInteger?  = null,
        @SerialName("vol_tnrt") @Contextual val tradeVolumeTurningRate: BigDecimal?  = null,
        @SerialName("prdy_smns_hour_acml_vol") @Contextual val accumulateTradeVolumeFromYesterdaySameHour: BigInteger?  = null,
        @SerialName("prdy_smns_hour_acml_vol_rate") @Contextual val rateAccumulateTradeVolumeFromYesterdaySameHour: BigInteger?  = null,
        @SerialName("hour_cls_code") val hourCode: HourCode?  = null,
        @SerialName("mrkt_trtm_cls_code") val marketTerminatedCode: String?  = null,
        @SerialName("vi_stnd_prc") @Contextual val viActivatePrice: BigInteger?  = null,
        ): Response {
        override val errorDescription: String? = null
        override val errorCode: String? = null
    }

    data class InquireLiveConfirmData(override var corp: CorporationRequest? = null): Data

    lateinit var iv: String
    lateinit var key: String

    override suspend fun register(data: InquireLiveConfirmData, init: ((LiveResponse) -> Unit)?, block: (InquireLiveConfirmResponse) -> Unit) {
        if (data.corp == null) data.corp = client.corp
        if (client.websocket == null) client.buildWebsocket()

        client.websocket?.run {
            send(buildCallBody(data, "1"))
            launch {
                client.websocketIncoming?.collect {
                    if (it is Frame.Text) {
                        it.readText()
                            .apply { println(this) }
                            .apply {
                                if (this[0] != '0' && this[0] != '1') {
                                    json.decodeFromString<LiveResponse>(this).run {
                                        if (this.header?.tradeId == (if (client.isDemo) "H0STCNI9" else "H0STCNI0") && this.header.tradeKey == client.htsId && init != null) {
                                            iv = this.output!!.iv!!
                                            key = this.output.key!!
                                            init(this)
                                        }
                                    }

                                    return@collect
                                }
                            }
                            .run {
                                split("|")
                                    .run {
                                        println(decodeAES(key, iv, Base64.decode(get(3))).decodeToString().split("^"))
                                        if (get(1) != "H0STCNT0") decodeAES(key, iv, Base64.decode(get(3))).decodeToString().split("^")
                                        else return@collect
                                    }
                                    .run {
                                        if (get(0) == client.htsId) this
                                        else return@collect
                                    }
                                    .run {
                                        block(InquireLiveConfirmResponse(
                                            this[0],
                                            this[1],
                                            this[2].toBigInteger(),
                                            SignPrice.values().find { f -> f.value.toString() == this[3] },
                                            this[4].toBigInteger(),
                                            this[5].toBigDecimal(),
                                            this[6].toBigDecimal(),
                                            this[7].toBigInteger(),
                                            this[8].toBigInteger(),
                                            this[9].toBigInteger(),
                                            this[10].toBigInteger(),
                                            this[11].toBigInteger(),
                                            this[12].toBigInteger(),
                                            this[13].toBigInteger(),
                                            this[14].toBigInteger(),
                                            this[15].toBigInteger(),
                                            this[16].toBigInteger(),
                                            this[17].toBigInteger(),
                                            this[18].toBigDecimal(),
                                            this[19].toBigInteger(),
                                            this[20].toBigInteger(),
                                            this[21],
                                            this[22].toBigDecimal(),
                                            this[23].toBigDecimal(),
                                            this[24],
                                            SignPrice.values().find { f -> f.value.toString() == this[25] },
                                            this[26].toBigInteger(),
                                            this[27],
                                            SignPrice.values().find { f -> f.value.toString() == this[28] },
                                            this[29].toBigInteger(),
                                            this[30],
                                            SignPrice.values().find { f -> f.value.toString() == this[31] },
                                            this[32].toBigInteger(),
                                            this[33],
                                            this[34],
                                            this[35].YN,
                                            this[36].toBigInteger(),
                                            this[37].toBigInteger(),
                                            this[38].toBigInteger(),
                                            this[39].toBigInteger(),
                                            this[40].toBigDecimal(),
                                            this[41].toBigInteger(),
                                            this[42].toBigInteger(),
                                            HourCode.values().find { f -> f.num == this[43] },
                                            this[44]
                                        ))
                                    }
                            }
                    }
                }
            }
        }
    }

    override suspend fun unregister(data: InquireLiveConfirmData) {
        if (data.corp == null) data.corp = client.corp
        if (client.websocket == null) client.buildWebsocket()

        client.websocket?.run {
            send(buildCallBody(data, "2"))
        }
    }
}