package io.github.devngho.kisopenapi.requests

import io.github.devngho.kisopenapi.KisOpenApi
import io.github.devngho.kisopenapi.requests.response.CorporationRequest
import io.github.devngho.kisopenapi.requests.response.LiveResponse

interface Data {
    val corp: CorporationRequest?
}
@Suppress("propertyName")
interface Response {
    val error_description: String?
    val error_code: String?
}

sealed interface Request<T: Response> {
    val client: KisOpenApi
}

sealed interface LiveRequest<T : Response>: Request<T>{
    suspend fun register(init: ((LiveResponse) -> Unit)? = null, block: (T) -> Unit)
    suspend fun unregister()

}

sealed interface LiveDataRequest<T: Data, U : Response>: Request<U>{
    suspend fun register(data: T, init: ((LiveResponse) -> Unit)? = null, block: (U) -> Unit)
    suspend fun unregister(data: T)

}

sealed interface NoDataRequest<T : Response>: Request<T>{
    suspend fun call(): T
}

sealed interface DataRequest<T: Data, U: Response>: Request<U> {
    suspend fun call(data: T): U
}
