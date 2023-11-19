package io.github.devngho.kisopenapi.requests.response

import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.util.Date
import io.github.devngho.kisopenapi.requests.util.WeekdayCode
import io.github.devngho.kisopenapi.requests.util.YNSerializer
import io.github.devngho.kisopenapi.requests.util.YYYYMMDDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * [KIS Developers 문서](https://apiportal.koreainvestment.com/apiservice/apiservice-domestic-stock-quotations)를 참조하세요.
 * @see io.github.devngho.kisopenapi.requests.InquireHoliday
 */
interface Holiday: Response {
    /** 기준 일자 */
    @Serializable(with = YYYYMMDDSerializer::class) @SerialName("bass_dt") val baseDate: Date

    /** 요일 구분 코드 */
    @SerialName("wday_dvsn_cd") val weekdayCode: WeekdayCode

    /** 영업일 여부(금융기관 업무일)
     * */
    @SerialName("bzdy_yn") @Serializable(with = YNSerializer::class) val isBizDay: Boolean

    /** 거래일 여부(증권 업무 가능일) */
    @SerialName("tr_day_yn") @Serializable(with = YNSerializer::class) val isTradeDay: Boolean

    /** 개장일 여부(주식 시장 개장일)
     *
     * 거래 주문을 넣을 때 사용하세요.
     * */
    @SerialName("opnd_yn") @Serializable(with = YNSerializer::class) val isMarketOpen: Boolean

    /** 결제일 여부(실제 주식 거래일) */
    @SerialName("sttl_day_yn") @Serializable(with = YNSerializer::class) val isPayDay: Boolean
}