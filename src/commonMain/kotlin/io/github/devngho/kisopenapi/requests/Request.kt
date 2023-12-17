package io.github.devngho.kisopenapi.requests

import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.data.CorporationRequest
import io.github.devngho.kisopenapi.requests.response.LiveResponse
import io.github.devngho.kisopenapi.requests.util.RequestException
import io.github.devngho.kisopenapi.requests.util.Result
import kotlinx.serialization.SerialName
import kotlinx.serialization.SerializationException
import kotlin.coroutines.cancellation.CancellationException

interface Data {
    var corp: CorporationRequest?
}

interface LiveData : Data {
    fun tradeKey(client: KISApiClient): String
}

interface Response {
    @SerialName("error_description")
    val errorDescription: String?

    @SerialName("error_code")
    val errorCode: String?
}

interface Request<T : Response> {
    val client: KISApiClient
}

interface LiveRequest<T : LiveData, U : Response> : Request<U> {
    /**
     * 실시간 요청을 등록합니다.
     * @param data 등록할 요청의 데이터
     * @param wait 등록이 완료될 때까지 대기할지 여부입니다.
     * @param force 이미 등록된 요청이 있을 경우 강제로 등록할지 여부입니다.
     * @param init 요청이 등록되었을 때의 콜백.
     * @param block 실시간 값이 수신되었을 때의 콜백
     */
    suspend fun register(
        data: T,
        wait: Boolean = false,
        force: Boolean = false,
        init: (suspend (Result<LiveResponse>) -> Unit)? = null,
        block: suspend (U) -> Unit
    )

    /**
     * 실시간 요청 등록을 해제합니다.
     * @param data 등록 해제할 요청의 데이터입니다.
     * @param wait 등록 해제가 완료될 때까지 대기할지 여부입니다.
     */
    suspend fun unregister(data: T, wait: Boolean = false)
}

interface NoDataRequest<T : Response> : Request<T> {
    @Throws(SerializationException::class, CancellationException::class, RequestException::class)
    /**
     * 데이터를 담지 않고 API를 호출한 후 결과를 반환합니다.
     * @return API 호출 결과
     * @throws SerializationException 올바르지 않은 데이터가 반환되었을 경우
     * @throws RequestException 요청에 에러가 발생했거나 올바르지 않은 데이터를 전달했을 경우
     * @see [io.github.devngho.kisopenapi.requests.domestic.inquire.InquirePrice]
     */
    suspend fun call(): Result<T>

}

interface DataRequest<T : Data, U : Response> : Request<U> {
    @Throws(SerializationException::class, CancellationException::class, RequestException::class)
    /**
     * 데이터를 담고 API를 호출한 후 결과를 반환합니다.
     * @return API 호출 결과
     * @throws SerializationException 올바르지 않은 데이터를 전달하거나 반환했을 경우
     * @throws RequestException 요청에 에러가 발생했거나 올바르지 않은 데이터를 전달했을 경우
     * @see [io.github.devngho.kisopenapi.requests.domestic.inquire.InquirePrice]
     */
    suspend fun call(data: T): Result<U>
}
