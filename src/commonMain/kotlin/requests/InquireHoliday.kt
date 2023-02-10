package io.github.devngho.kisopenapi.requests

import io.github.devngho.kisopenapi.KisOpenApi
import io.github.devngho.kisopenapi.requests.response.*
import io.github.devngho.kisopenapi.requests.util.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class InquireHoliday(override val client: KisOpenApi):
    DataRequest<InquireHoliday.InquireHolidayData, InquireHoliday.InquireHolidayResponse> {
    private val url = if (client.isDemo) throw DemoError("InquireHoliday cannot run with demo account.")
                        else             "https://openapi.koreainvestment.com:9443/uapi/domestic-stock/v1/quotations/chk-holiday"

    @Serializable
    data class InquireHolidayResponse(
        @SerialName("msg_cd") override val code: String?,
        @SerialName("msg1") override val msg: String?,
        @SerialName("rt_cd") @Serializable(with = ResultCodeSerializer::class) override val isOk: Boolean?,
        @SerialName("ctx_area_fk") val continuousAreaFK: String?,
        @SerialName("ctx_area_nk") val continuousAreaNK: String?,

        var output: List<InquireHolidayOutput>?,
        override var next: (suspend () -> InquireHolidayResponse)?,
        override var tradeContinuous: String?
    ): Response, TradeContinuousResponse, Msg {
        override val error_description: String? = null
        override val error_code: String? = null
    }

    @Serializable
    data class InquireHolidayOutput(
        /**
         * YYYYMMDD
         */
        @SerialName("bass_dt") override val baseDate: String,
        @SerialName("wday_dvsn_cd") override val weekdayCode: WeekdayCode,
        @SerialName("bzdy_yn") @Serializable(with = YNSerializer::class) override val isBizDay: Boolean,
        @SerialName("tr_day_yn") @Serializable(with = YNSerializer::class) override val isTradeDay: Boolean,
        @SerialName("opnd_yn") @Serializable(with = YNSerializer::class) override val isMarketOpen: Boolean,
        @SerialName("sttl_day_yn") @Serializable(with = YNSerializer::class) override val isPayDay: Boolean,
    ): Holiday, Response {
        override val error_description: String? = null
        override val error_code: String? = null
    }

    data class InquireHolidayData(
        /**
         * date style : YYYYMMDD
         */
        val baseDate: String,
        override var corp: CorporationRequest? = null,
        override val tradeContinuous: String? = "",
        val continuousAreaFK: String = "",
        val continuousAreaNK: String = "")
        : Data, TradeContinuousData

    override suspend fun call(data: InquireHolidayData): InquireHolidayResponse {
        if (data.corp == null) data.corp = client.corp

        val res = client.httpClient.get(url) {
            auth(client)
            tradeId(if (client.isDemo) throw DemoError("InquireHoliday cannot run with demo account.") else "CTCA0903R")
            data.corp?.let { corporation(it) }

            url {
                parameters.run {
                    set("BASS_DT", data.baseDate)
                    set("CTX_AREA_NK", data.continuousAreaNK)
                    set("CTX_AREA_FK", data.continuousAreaFK)
                }
            }
        }
        return res.body<InquireHolidayResponse>().apply {
            if (this.error_code != null) throw RequestError(this.error_description)

            res.headers.forEach { s, strings ->
                when(s) {
                    "tr_cont" -> this.tradeContinuous = strings[0]
                }
            }

            if (this.tradeContinuous == "F" || this.tradeContinuous == "M") {
                this.next = {
                    call(data.copy(tradeContinuous = "N", continuousAreaFK = this.continuousAreaFK!!, continuousAreaNK = this.continuousAreaNK!!))
                }
            }
        }
    }
}