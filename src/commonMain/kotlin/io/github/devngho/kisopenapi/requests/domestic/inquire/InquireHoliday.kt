package io.github.devngho.kisopenapi.requests.domestic.inquire

import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.Data
import io.github.devngho.kisopenapi.requests.DataRequest
import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.data.CorporationRequest
import io.github.devngho.kisopenapi.requests.data.Msg
import io.github.devngho.kisopenapi.requests.data.TradeContinuousData
import io.github.devngho.kisopenapi.requests.data.TradeContinuousResponse
import io.github.devngho.kisopenapi.requests.util.*
import io.github.devngho.kisopenapi.requests.util.RequestException.Companion.throwIfClientIsDemo
import io.github.devngho.kisopenapi.requests.util.YYYYMMDDSerializer.YYYYMMDD
import io.ktor.client.request.*
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 국내 주식 거래일과 휴장일 여부를 조회하고 반환합니다.
 */
@DemoNotSupported
class InquireHoliday(override val client: KISApiClient) :
    DataRequest<InquireHoliday.InquireHolidayData, InquireHoliday.InquireHolidayResponse> {
    private val url = "${client.options.baseUrl}/uapi/domestic-stock/v1/quotations/chk-holiday"

    @Serializable
    data class InquireHolidayResponse(
        @SerialName("msg_cd") override val code: String?,
        @SerialName("msg1") override val msg: String?,
        @SerialName("rt_cd") @Serializable(with = ResultCodeSerializer::class) override val isOk: Boolean?,
        @SerialName("ctx_area_fk") val continuousAreaFK: String?,
        @SerialName("ctx_area_nk") val continuousAreaNK: String?,

        var output: List<InquireHolidayOutput>?,
        override var next: (suspend () -> Result<InquireHolidayResponse>)?,
        override var tradeContinuous: String?
    ) : Response, TradeContinuousResponse<InquireHolidayResponse>, Msg {
        @SerialName("error_description")
        override val errorDescription: String? = null

        @SerialName("error_code")
        override val errorCode: String? = null
    }

    @Serializable
    @Suppress("SpellCheckingInspection")
    data class InquireHolidayOutput(
        /** 기준 일자 */
        @Serializable(with = YYYYMMDDSerializer::class) @SerialName("bass_dt") val baseDate: Date,
        /** 요일 구분 코드 */
        @SerialName("wday_dvsn_cd") val weekdayCode: WeekdayCode,
        /** 영업일 여부(금융기관 업무일) */
        @SerialName("bzdy_yn") @Serializable(with = YNSerializer::class) val isBizDay: Boolean,
        /** 거래일 여부(증권 업무 가능일) */
        @SerialName("tr_day_yn") @Serializable(with = YNSerializer::class) val isTradeDay: Boolean,
        /** 개장일 여부(주식 시장 개장일)
         *
         * 거래 주문을 넣을 때 사용하세요.
         * */
        @SerialName("opnd_yn") @Serializable(with = YNSerializer::class) val isMarketOpen: Boolean,
        /** 결제일 여부(실제 주식 거래일) */
        @SerialName("sttl_day_yn") @Serializable(with = YNSerializer::class) val isPayDay: Boolean,
    ) : Response {
        @SerialName("error_description")
        override val errorDescription: String? = null

        @SerialName("error_code")
        override val errorCode: String? = null
    }

    data class InquireHolidayData(
        val baseDate: Date,
        override var corp: CorporationRequest? = null,
        override var tradeContinuous: String? = "",
        val continuousAreaFK: String = "",
        val continuousAreaNK: String = "")
        : Data, TradeContinuousData

    override suspend fun call(data: InquireHolidayData) = request(data, block = {
        throwIfClientIsDemo()

        @Suppress("SpellCheckingInspection")
        client.httpClient.get(url) {
            setAuth(client)
            setTR(
                if (client.isDemo) throw RequestException(
                    "InquireHoliday cannot run with demo account.",
                    RequestCode.DemoUnavailable
                ) else "CTCA0903R"
            )
            setCorporation(it.corp)

            url { _ ->
                parameters.run {
                    set("BASS_DT", it.baseDate.YYYYMMDD)
                    set("CTX_AREA_NK", it.continuousAreaNK)
                    set("CTX_AREA_FK", it.continuousAreaFK)
                }
            }
        }
    }, continuousModifier = {
        if (it.tradeContinuous == "F" || it.tradeContinuous == "M") {
            this.copy(
//                tradeContinuous = "N",
//                continuousAreaFK = it.continuousAreaFK!!,
//                continuousAreaNK = it.continuousAreaNK!!,
                // 실제 서버가 tr_cd를 지원하지 않아 마지막 조회 일자를 통해 다음 조회 실행
                baseDate = it.output!!.last().baseDate.plus(1, DateTimeUnit.DAY)
            )
        } else data
    })
}