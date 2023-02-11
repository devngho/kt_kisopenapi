package io.github.devngho.kisopenapi.requests

import io.github.devngho.kisopenapi.KisOpenApi
import io.github.devngho.kisopenapi.requests.response.CorporationRequest
import io.github.devngho.kisopenapi.requests.response.LiveResponse
import kotlinx.serialization.SerialName

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
    suspend fun register(data: T, init: ((LiveResponse) -> Unit)? = null, block: (U) -> Unit)
    suspend fun unregister(data: T)

}

sealed interface NoDataRequest<T : Response>: Request<T>{
    suspend fun call(): T
}

sealed interface DataRequest<T: Data, U: Response>: Request<U> {
    suspend fun call(data: T): U
}
