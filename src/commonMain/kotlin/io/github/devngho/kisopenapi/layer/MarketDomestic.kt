package io.github.devngho.kisopenapi.layer

import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.domestic.inquire.InquireCondition
import io.github.devngho.kisopenapi.requests.domestic.inquire.InquireConditionList
import io.github.devngho.kisopenapi.requests.domestic.inquire.InquireHoliday
import io.github.devngho.kisopenapi.requests.domestic.inquire.InquireTradeVolumeRank
import io.github.devngho.kisopenapi.requests.util.*
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import kotlin.jvm.JvmInline

class MarketDomestic(val api: KISApiClient) : Market {
    /**
     * 종목 검색 조건
     */
    data class Condition(val name: String, val conditionKey: ConditionKey)

    /**
     * 종목 검색 조건 키
     */
    @JvmInline
    value class ConditionKey(val value: String)

    /**
     * 종목 검색 조건에 해당하는 종목을 가져옵니다.
     *
     * @param conditionKey 종목 검색 조건
     * @return [List]. [StockDomestic]의 목록
     */
    @DemoNotSupported
    @Deprecated("Use search(ConditionKey) instead.")
    suspend fun search(conditionKey: String): Result<List<StockDomestic>> =
        InquireCondition(api)
            .call(InquireCondition.ConditionData(conditionKey))
            .also { if (!it.isOk) return Result(null, it.error) }
            .getOrThrow().output!!
            .mapNotNull { if (it.ticker != null) api.stockDomestic(it.ticker) else null }
            .let { Result(it) }

    /**
     * 종목 검색 조건에 해당하는 종목을 가져옵니다.
     *
     * @param conditionKey 종목 검색 조건
     * @return [List]. [StockDomestic]의 목록
     */
    @DemoNotSupported
    suspend fun search(conditionKey: ConditionKey): Result<List<StockDomestic>> =
        InquireCondition(api)
            .call(InquireCondition.ConditionData(conditionKey.value))
            .also { if (!it.isOk) return Result(null, it.error) }
            .getOrThrow().output!!
            .mapNotNull { if (it.ticker != null) api.stockDomestic(it.ticker) else null }
            .let { Result(it) }

    /**
     * 종목 검색 조건의 목록을 가져옵니다. [MarketDomestic.search]의 conditionKey에 사용할 수 있습니다.
     *
     * @return 종목 검색 조건 목록의 [Map]. key는 이름이고 value는 conditionKey입니다.
     */
    @DemoNotSupported
    suspend fun getSearchConditions(): Result<List<Condition>> =
        InquireConditionList(api)
            .call(InquireConditionList.ConditionData())
            .also { if (!it.isOk) return Result(null, it.error) }
            .getOrThrow().output!!
            .filter { it.conditionKey != null && it.conditionName != null }
            .map { Condition(it.conditionName!!, ConditionKey(it.conditionKey!!)) }
            .let { Result(it) }


    @DemoNotSupported
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
    @DemoNotSupported
    suspend fun isHoliday(date: Date): Result<Boolean> =
        getHolidays(date, date)
            .also { if (!it.isOk) return Result(null, it.error) }
            .getOrThrow()
            .also { if (it.isEmpty()) return Result(null, RequestException("Holiday not found.", RequestCode.Unknown)) }
            .let { Result(it.values.last()) }

    /**
     * 주어진 기간 동안의 휴장일 여부를 가져옵니다.
     *
     * @param from 시작일
     * @param to 종료일
     * @return [Map]. [Date]가 키이고, 휴장 여부가 값입니다.
     */
    @DemoNotSupported
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
    @DemoNotSupported
    suspend fun getHolidays(range: ClosedRange<Date>): Result<Map<Date, Boolean>> =
        getHolidays(range.start, range.endInclusive)
}