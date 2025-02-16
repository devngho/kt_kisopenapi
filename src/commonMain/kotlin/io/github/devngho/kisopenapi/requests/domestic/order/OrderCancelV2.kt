package io.github.devngho.kisopenapi.requests.domestic.order

import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.Data
import io.github.devngho.kisopenapi.requests.DataRequest
import io.github.devngho.kisopenapi.requests.auth.HashKey.Companion.hashKey
import io.github.devngho.kisopenapi.requests.data.AccountInfo
import io.github.devngho.kisopenapi.requests.data.CorporationRequest
import io.github.devngho.kisopenapi.requests.data.TradeContinuousData
import io.github.devngho.kisopenapi.requests.util.*
import io.ktor.client.request.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * 국내 주식 종목의 주문을 취소하고 주문 정보를 반환합니다.
 */
class OrderCancelV2(override val client: KISApiClient) :
    DataRequest<OrderCancelV2.OrderDataV2, OrderCancel.OrderResponse> {
    private val url = "${client.options.baseUrl}/uapi/domestic-stock/v1/trading/order-rvsecncl"

    @Serializable
    @Suppress("SpellCheckingInspection")
    data class OrderDataV2(
        @SerialName("ORD_DVSN") val orderType: OrderTypeCode,
        /** 전부 취소하려면 0으로 입력하세요.
         * 일부를 취소하려면 취소하려는 수량을 입력하세요.
         */
        @Contextual @SerialName("ORD_QTY") val count: BigInteger,
        @SerialName("ORGN_ODNO") val orderNumber: String,
        @SerialName("QTY_ALL_ORD_YN") @Serializable(with = YNSerializer::class) val orderAll: Boolean,
        @SerialName("CANO") override val accountNumber: String? = null,
        @SerialName("ACNT_PRDT_CD") override val accountProductCode: String? = null,
        /** 스탑지정가호가 주문 시 사용되는 조건 가격 */
        @Contextual @SerialName("CNDT_PRIC") val conditionPrice: BigInteger = BigInteger(0),
        @SerialName("EXCG_ID_DVSN_CD") val market: Market,
        @Transient override var corp: CorporationRequest? = null,
        @Transient override var tradeContinuous: String? = ""
    ) : Data, TradeContinuousData, AccountInfo {
        @SerialName("RVSE_CNCL_DVSN_CD")
        val isAmendOrCancel = "02"

        @Contextual
        @SerialName("ORD_UNPR")
        val price: BigInteger = BigInteger(0)

        @SerialName("KRX_FWDG_ORD_ORGNO")
        val orderOffice: String = ""
    }

    @Suppress("SpellCheckingInspection")
    override suspend fun call(data: OrderDataV2): Result<OrderCancel.OrderResponse> {
        if (client.options.useV1PolyfillForV2 && !isATSAvailable()) {
            return OrderCancel(client).call(
                OrderCancel.OrderData(
                    orderType = data.orderType,
                    count = data.count,
                    orderNumber = data.orderNumber,
                    orderAll = data.orderAll,
                    accountNumber = data.accountNumber,
                    accountProductCode = data.accountProductCode,
                    corp = data.corp,
                    tradeContinuous = data.tradeContinuous
                )
            )
        }

        return request(data) {
            if (it.price.isZero() && it.orderType == OrderTypeCode.SelectPrice) throw RequestException(
                "지정가 주문에서 가격은 필수 값입니다.",
                RequestCode.InvalidOrder
            )

            client.httpClient.post(url) {
                setAuth(client)
                setTR(if (client.isDemo) "VTTC0803U" else "TTTC0013U")
                it.corp?.let { setCorporation(it) }
                setBody(
                    it.copy(
                        accountNumber = it.accountNumber ?: client.account!!.first,
                        accountProductCode = it.accountProductCode ?: client.account!!.second
                    )
                )

                hashKey<OrderDataV2>(client)
            }
        }
    }
}