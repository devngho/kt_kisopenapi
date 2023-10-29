package io.github.devngho.kisopenapi.requests

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.KisOpenApi
import io.github.devngho.kisopenapi.requests.response.*
import io.github.devngho.kisopenapi.requests.util.*
import io.github.devngho.kisopenapi.requests.util.YYYYMMDDSerializer.YYYY_MM_DD
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class InquirePriceSeries(override val client: KisOpenApi):
    DataRequest<InquirePriceSeries.InquirePriceSeriesData, InquirePriceSeries.InquirePriceSeriesResponse> {
    private val url = if (client.isDemo) "https://openapivts.koreainvestment.com:29443/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice"
                      else               "https://openapi.koreainvestment.com:9443/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice"

    @OptIn(ExperimentalSerializationApi::class)
    @Serializable(with = PeriodDivisionCode.PeriodDivisionCodeSerializer::class)
    @Suppress("unused")
    enum class PeriodDivisionCode(val num: String) {
        Days("D"),
        Weeks("W"),
        Months("M"),
        Years("Y");

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
    data class InquirePriceSeriesResponse(
        @SerialName("tr_id") override var tradeId: String?,
        @SerialName("tr_cont") override var tradeContinuous: String?,
        @SerialName("gt_uid") override var globalTradeID: String?,
        @SerialName("msg_cd") override val code: String?,
        @SerialName("msg1") override val msg: String?,
        @SerialName("rt_cd") @Serializable(with = ResultCodeSerializer::class) override val isOk: Boolean?,

        var output1: InquirePrice.InquirePriceResponseOutput?, var output2: List<InquirePriceSeriesResponseOutput2>?, override var next: (suspend () -> Response)?
    ): Response, TradeContinuousResponse, TradeIdMsg {
        @SerialName("error_description")
        override val errorDescription: String? = null

        @SerialName("error_code")
        override val errorCode: String? = null
    }

    @Serializable
    @Suppress("SpellCheckingInspection")
    data class InquirePriceSeriesResponseOutput2(
        @SerialName("stck_bsop_date") val bizDate: String?,
        @SerialName("stck_oprc") @Contextual override val openingPrice: BigInteger?,
        @SerialName("stck_hgpr") @Contextual override val highPrice: BigInteger?,
        @SerialName("stck_lwpr") @Contextual override val lowPrice: BigInteger?,
        /**
         * Close Price
         */
        @SerialName("stck_clpr") @Contextual override val price: BigInteger?,
        @SerialName("prdy_vrss") @Contextual val changeFromYesterday: BigInteger?,
        @SerialName("prdy_vrss_sign") val signFromYesterday: SignPrice?,
        @SerialName("flng_cls_code") val lockCode: LockCode?,
        @SerialName("acml_prtt_rate") @Contextual val accumulateDivisionRate: BigDecimal?,
        @SerialName("acml_vol") @Contextual val accumulateTradeVolume: BigInteger?,
        @SerialName("acml_tr_pbmn") @Contextual val accumulateTradePrice: BigInteger?,
        @SerialName("prtt_rate") @Contextual val divisionRate: BigDecimal?,
        @SerialName("mod_yn") @Serializable(with = YNSerializer::class) val isModified: Boolean?,
        @SerialName("revl_issu_reas") val reasonForRevision: String?,
    ): StockPriceHighMax {
        @SerialName("error_description")
        override val errorDescription: String? = null

        @SerialName("error_code")
        override val errorCode: String? = null
    }

    data class InquirePriceSeriesData(
        val ticker: String,
        val period: PeriodDivisionCode = PeriodDivisionCode.Days,
        val useOriginalPrice: Boolean = false,
        val startDate: Date = Date(0, 0, 0),
        val endDate: Date = Date(0, 0, 0),
        override var corp: CorporationRequest? = null, override var tradeContinuous: String? = ""): Data, TradeContinuousData

    @Suppress("SpellCheckingInspection")
    override suspend fun call(data: InquirePriceSeriesData): InquirePriceSeriesResponse = client.rateLimiter.rated {
        if (data.corp == null) data.corp = client.corp

        val res = client.httpClient.get(url) {
            auth(client)
            tradeId("FHKST03010100")
            stock(data.ticker)
            data.corp?.let { corporation(it) }

            url {
                parameters.run {
                    append("FID_PERIOD_DIV_CODE", data.period.num)
                    append("FID_INPUT_DATE_1", data.startDate.YYYY_MM_DD)
                    append("FID_INPUT_DATE_2", data.endDate.YYYY_MM_DD)
                    append("FID_ORG_ADJ_PRC", if (data.useOriginalPrice) "1" else "0")
                }
            }
        }

        res.body<InquirePriceSeriesResponse>().apply {
            if (this.errorCode != null) throw RequestError(this.errorDescription)

            processHeader(res)
            setNext(data, this)
        }
    }
}