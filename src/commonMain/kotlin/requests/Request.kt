package com.github.devngho.kisopenapi.requests

import com.github.devngho.kisopenapi.KisOpenApi

sealed interface Data
object EmptyData : Data
sealed interface Response

sealed interface Request<T: Response> {
    val client: KisOpenApi
}

sealed interface NoDataRequest<T : Response>: Request<T>{
    suspend fun call(): T
}

sealed interface DataRequest<T: Data, U: Response>: Request<U> {
    suspend fun call(data: T): U
}