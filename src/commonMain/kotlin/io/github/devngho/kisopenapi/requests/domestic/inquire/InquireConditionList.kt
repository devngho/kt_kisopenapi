package io.github.devngho.kisopenapi.requests.domestic.inquire

import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.Data
import io.github.devngho.kisopenapi.requests.DataRequest
import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.data.CorporationRequest
import io.github.devngho.kisopenapi.requests.data.TradeIdMsg
import io.github.devngho.kisopenapi.requests.util.*
import io.github.devngho.kisopenapi.requests.util.RequestException.Companion.throwIfClientIsDemo
import io.ktor.client.request.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 국내 주식 종목 검색 조건 목록을 가져와 반환합니다.
 */
@DemoNotSupported
class InquireConditionList(override val client: KISApiClient) :
    DataRequest<InquireConditionList.ConditionData, InquireConditionList.ConditionResponse> {
    private val url = "${client.options.baseUrl}/uapi/domestic-stock/v1/quotations/psearch-title"

    @Serializable
    data class ConditionResponse(
        @SerialName("tr_id") override var tradeId: String?,
        @SerialName("gt_uid") override var globalTradeID: String?,
        @SerialName("msg_cd") override val code: String?,
        @SerialName("msg1") override val msg: String?,
        @SerialName("rt_cd") @Serializable(with = ResultCodeSerializer::class) override val isOk: Boolean?,

        @SerialName("output2") var output: List<ConditionResponseOutput>?,
    ) : Response, TradeIdMsg {
        @SerialName("error_description")
        override val errorDescription: String? = null

        @SerialName("error_code")
        override val errorCode: String? = null
    }

    @Serializable
    data class ConditionResponseOutput(
        @SerialName("user_id") val htsId: String?,
        @SerialName("seq") val conditionKey: String?,
        @SerialName("grp_nm") val groupName: String?,
        @SerialName("condition_nm") val conditionName: String?,
    )

    data class ConditionData(
        override var corp: CorporationRequest? = null,
    ) : Data

    @Suppress("SpellCheckingInspection")
    override suspend fun call(data: ConditionData) = request(data) {
        throwIfClientIsDemo()

        client.httpClient.get(url) {
            setAuth(client)
            setTradeId("HHKST03900300")
            setCorporation(it.corp)

            url { _ ->
                parameters.run {
                    set("user_id", client.htsId ?: return@run)
                }
            }
        }
    }
}