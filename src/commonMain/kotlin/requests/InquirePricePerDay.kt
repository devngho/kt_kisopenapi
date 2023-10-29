package io.github.devngho.kisopenapi.requests

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.KisOpenApi
import io.github.devngho.kisopenapi.requests.response.*
import io.github.devngho.kisopenapi.requests.util.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class InquirePricePerDay(override val client: KisOpenApi):
    DataRequest<InquirePricePerDay.InquirePricePerDayData, InquirePricePerDay.InquirePricePerDayResponse> {
    private val url = if (client.isDemo) "https://openapivts.koreainvestment.com:29443/uapi/domestic-stock/v1/quotations/inquire-daily-price"
                      else               "https://openapi.koreainvestment.com:9443/uapi/domestic-stock/v1/quotations/inquire-daily-price"

    @OptIn(ExperimentalSerializationApi::class)
    @Serializable(with = PeriodDivisionCode.PeriodDivisionCodeSerializer::class)
    @Suppress("unused")
    enum class PeriodDivisionCode(val num: String) {
        Days30("D"),
        Weeks30("W"),
        Months30("M");

        @ExperimentalSerializationApi
        object PeriodDivisionCodeSerializer : KSerializer<PeriodDivisionCode> {
            override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("PeriodDivisionCode", PrimitiveKind.STRING)

            override fun deserialize(decoder: Decoder): PeriodDivisionCode {
                val d = decoder.decodeString()
                return entries.first { it.num == d }
            }

            override fun serialize(encoder: Encoder, value: PeriodDivisionCode) {
                encoder.encodeString(value.num)
            }
        }
    }

    @Serializable
    data class InquirePricePerDayResponse(
        @SerialName("tr_id") override var tradeId: String?,
        @SerialName("tr_cont") override var tradeContinuous: String?,
        @SerialName("gt_uid") override var globalTradeID: String?,
        @SerialName("msg_cd") override val code: String?,
        @SerialName("msg1") override val msg: String?,
        @SerialName("rt_cd") @Serializable(with = ResultCodeSerializer::class) override val isOk: Boolean?,

        var output: List<InquirePricePerDayResponseOutput>?, override var next: (suspend () -> Response)?
    ): Response, TradeContinuousResponse, TradeIdMsg {
        @SerialName("error_description")
        override val errorDescription: String? = null

        @SerialName("error_code")
        override val errorCode: String? = null
    }

    @Serializable
    @Suppress("SpellCheckingInspection")
    data class InquirePricePerDayResponseOutput(
        @SerialName("stck_bsop_date") val bizDate: String?,
        @SerialName("stck_oprc") @Contextual override val openingPrice: BigInteger?,
        @SerialName("stck_hgpr") @Contextual override val highPrice: BigInteger?,
        @SerialName("stck_lwpr") @Contextual override val lowPrice: BigInteger?,
        /**
         * Close Price
         */
        @SerialName("stck_clpr") @Contextual override val price: BigInteger?,
        @SerialName("prdy_vrss") @Contextual override val changeFromYesterday: BigInteger?,
        @SerialName("prdy_vrss_sign") override val signFromYesterday: SignPrice?,
        @SerialName("prdy_ctrt") @Contextual override val rateFromYesterday: BigDecimal?,
        @SerialName("flng_cls_code") val lockCode: LockCode?,
        @SerialName("acml_prtt_rate") @Contextual val accumulateDivisionRate: BigDecimal?,
        @SerialName("acml_vol") @Contextual override val accumulateTradeVolume: BigInteger?,
        @SerialName("hts_frgn_ehrt") @Contextual override val htsForeignerExhaustionRate: BigDecimal?,
        @SerialName("frgn_ntby_qty") @Contextual override val foreignerNetBuyCount: BigInteger?,
        @SerialName("prdy_vrss_vol_rate") @Contextual override val rateTradeVolumeFromYesterday: BigDecimal?
    ): StockPriceHighMax, StockTrade, StockPriceChange, StockPriceForeigner {
        @SerialName("error_description")
        override val errorDescription: String? = null

        @SerialName("error_code")
        override val errorCode: String? = null
    }

    data class InquirePricePerDayData(
        val ticker: String,
        val period: PeriodDivisionCode = PeriodDivisionCode.Days30,
        val useOriginalPrice: Boolean = false,
                                      override var corp: CorporationRequest? = null, override var tradeContinuous: String? = ""): Data, TradeContinuousData

    @Suppress("SpellCheckingInspection")
    override suspend fun call(data: InquirePricePerDayData): InquirePricePerDayResponse = client.rateLimiter.rated {
        if (data.corp == null) data.corp = client.corp

        val res = client.httpClient.get(url) {
            auth(client)
            tradeId("FHKST01010400")
            stock(data.ticker)
            data.corp?.let { corporation(it) }

            url {
                parameters.run {
                    append("FID_PERIOD_DIV_CODE", data.period.num)
                    append("FID_ORG_ADJ_PRC", if (data.useOriginalPrice) "1" else "0")
                }
            }
        }

        res.body<InquirePricePerDayResponse>().apply {
            if (this.errorCode != null) throw RequestError(this.errorDescription)

            processHeader(res)
            setNext(data, this)
        }
    }
}