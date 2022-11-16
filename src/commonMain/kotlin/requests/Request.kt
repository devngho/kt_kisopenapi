package io.github.devngho.kisopenapi.requests

import io.github.devngho.kisopenapi.KisOpenApi

interface Data
@Suppress("propertyName")
interface Response {
    val error_description: String?
    val error_code: String?
}

sealed interface Request<T: Response> {
    val client: KisOpenApi
}

sealed interface NoDataRequest<T : Response>: Request<T>{
    suspend fun call(): T
}

sealed interface DataRequest<T: Data, U: Response>: Request<U> {
    suspend fun call(data: T): U
}
