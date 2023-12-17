package io.github.devngho.kisopenapi.requests.data

import io.github.devngho.kisopenapi.KISApiClient
import kotlinx.serialization.SerialName

interface AccountInfo {
    @SerialName("CANO")
    val accountNumber: String?

    @SerialName("ACNT_PRDT_CD")
    val accountProductCode: String?

    companion object {
        /**
         * [AccountInfo]에서 계좌번호와 상품코드가 null이면 [KISApiClient.account]에서 가져와 값을 채웁니다.
         * 값을 채운 겍체는 원본 객체를 delegate로 가지고 있습니다.
         *
         * @param client [KISApiClient] 객체
         * @return [AccountInfo] 객체.
         */
        inline fun <reified T : AccountInfo> T.fillFrom(client: KISApiClient) =
            if (this.accountNumber == null || this.accountProductCode == null)
                object : AccountInfo by this {
                    override val accountNumber: String = client.account!![0]
                    override val accountProductCode: String = client.account!![1]
                } as T
            else this
    }
}