package io.github.devngho.kisopenapi.requests

import io.github.devngho.kisopenapi.KisOpenApi
import io.github.devngho.kisopenapi.requests.response.CorporationRequest
import io.github.devngho.kisopenapi.requests.response.LiveResponse
import io.github.devngho.kisopenapi.requests.util.RequestError
import kotlinx.serialization.SerialName
import kotlinx.serialization.SerializationException
import kotlin.coroutines.cancellation.CancellationException

interface Data {
    val corp: CorporationRequest?
}
interface Response {
    @SerialName("error_description") val errorDescription: String?
    @SerialName("error_code") val errorCode: String?
}

sealed interface Request<T: Response> {
    val client: KisOpenApi
}

sealed interface LiveRequest<T: Data, U : Response>: Request<U>{
    /**
     * 실시간 요청을 등록합니다.
     * @param init 요청이 등록되었을 때의 콜백입니다.
     * @param block 실시간 값이 수신되었을 때의 콜백입니다.
     */
    suspend fun register(data: T, init: ((LiveResponse) -> Unit)? = null, block: (U) -> Unit)

    /**
     * 실시간 요청 등록을 해제합니다.
     */
    suspend fun unregister(data: T)

}

sealed interface NoDataRequest<T : Response>: Request<T>{
    @Throws(SerializationException::class, CancellationException::class, RequestError::class)
    /**
     * 데이터를 담지 않은 요청을 전달합니다.
     * @throws SerializationException 올바르지 않은 데이터가 반환되었을 경우
     * @throws RequestError 요청에 에러가 발생했거나 올바르지 않은 데이터를 전달했을 경우
     * @see GrantToken
     */
    suspend fun call(): T

}

sealed interface DataRequest<T: Data, U: Response>: Request<U> {
    @Throws(SerializationException::class, CancellationException::class, RequestError::class)
    /**
     * 데이터를 담은 요청을 전달합니다.
     * @throws SerializationException 올바르지 않은 데이터를 전달하거나 반환했을 경우
     * @throws RequestError 요청에 에러가 발생했거나 올바르지 않은 데이터를 전달했을 경우
     * @see InquirePrice
     */
    suspend fun call(data: T): U
}
