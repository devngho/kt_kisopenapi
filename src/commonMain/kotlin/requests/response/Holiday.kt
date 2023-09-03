package io.github.devngho.kisopenapi.requests.response

import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.util.Date
import io.github.devngho.kisopenapi.requests.util.WeekdayCode
import io.github.devngho.kisopenapi.requests.util.YNSerializer
import io.github.devngho.kisopenapi.requests.util.YYYYMMDDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface Holiday: Response {
    @Serializable(with = YYYYMMDDSerializer::class) @SerialName("bass_dt") val baseDate: Date
    @SerialName("wday_dvsn_cd") val weekdayCode: WeekdayCode
    @SerialName("bzdy_yn") @Serializable(with = YNSerializer::class) val isBizDay: Boolean
    @SerialName("tr_day_yn") @Serializable(with = YNSerializer::class) val isTradeDay: Boolean
    @SerialName("opnd_yn") @Serializable(with = YNSerializer::class) val isMarketOpen: Boolean
    @SerialName("sttl_day_yn") @Serializable(with = YNSerializer::class) val isPayDay: Boolean
}