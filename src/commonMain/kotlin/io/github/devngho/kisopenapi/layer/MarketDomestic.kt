package io.github.devngho.kisopenapi.layer

import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.domestic.inquire.InquireHoliday
import io.github.devngho.kisopenapi.requests.domestic.inquire.InquireTradeVolumeRank
import io.github.devngho.kisopenapi.requests.util.Date
import io.github.devngho.kisopenapi.requests.util.RequestCode
import io.github.devngho.kisopenapi.requests.util.RequestException
import io.github.devngho.kisopenapi.requests.util.Result
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus

class MarketDomestic(val api: KISApiClient) : Market {
    suspend fun getRank(
        belongClassifier: InquireTradeVolumeRank.BelongClassifier,
        queryBuilder: (InquireTradeVolumeRank.InquireTradeVolumeRankData.() -> Unit)? = null
    ): Result<List<StockDomestic>> =
        InquireTradeVolumeRank(api)
            .call(
                InquireTradeVolumeRank.InquireTradeVolumeRankData(belongClassifier)
                    .let { if (queryBuilder != null) it.apply(queryBuilder) else it })
            .also { if (!it.isOk) return Result(null, it.error) }
            .getOrThrow().output!!
            .mapNotNull {
                api.stockDomestic(it.ticker ?: return@mapNotNull null).apply {
                    updateBy(it)
                }
            }
            .let { Result(it) }

    /**
     * 주어진 날짜의 휴장 여부를 가져옵니다.
     *
     * @param date 날짜
     * @return 휴장 여부
     */
    suspend fun isHoliday(date: Date): Result<Boolean> =
        getHolidays(date, date)
            .also { if (!it.isOk) return Result(null, it.error) }
            .getOrThrow()
            .also { if (it.isEmpty()) return Result(null, RequestException("No Holiday found.", RequestCode.Unknown)) }
            .let { Result(it.values.last()) }

    /**
     * 주어진 기간 동안의 휴장일 여부를 가져옵니다.
     *
     * @param from 시작일
     * @param to 종료일
     * @return [Map]. [Date]가 키이고, 휴장 여부가 값입니다.
     */
    suspend fun getHolidays(from: Date, to: Date): Result<Map<Date, Boolean>> =
        InquireHoliday(api)
            .call(InquireHoliday.InquireHolidayData(from))
            .also { if (!it.isOk) return Result(null, it.error) }
            .getOrThrow().output!!
            .associate { holiday -> holiday.baseDate to holiday.isMarketOpen }
            .let {
                val last = it.keys.last()
                if (last < to) {
                    // to 일자까지 가져오기 위해 재귀 호출
                    val next = getHolidays(last.plus(1, DateTimeUnit.DAY), to).let { nextOutput ->
                        if (!nextOutput.isOk) return nextOutput
                        else nextOutput.getOrThrow()
                    }
                    Result(next + it)
                } else Result(it.filterKeys { key -> key in from..to })
            }

    /**
     * 주어진 기간 동안의 휴장일 여부를 가져옵니다.
     *
     * @param range 기간
     * @return [Map]. [Date]가 키이고, 휴장 여부가 값입니다.
     */
    suspend fun getHolidays(range: ClosedRange<Date>): Result<Map<Date, Boolean>> =
        getHolidays(range.start, range.endInclusive)
}