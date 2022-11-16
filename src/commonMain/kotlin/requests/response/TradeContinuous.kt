package io.github.devngho.kisopenapi.requests.response

import io.github.devngho.kisopenapi.requests.Data
import io.github.devngho.kisopenapi.requests.Response
import kotlinx.serialization.SerialName

interface TradeContinuousData: Data {
    @SerialName("tr_cont")
    val tradeContinuous: String?
}

interface TradeContinuousResponse: Response {
    @SerialName("tr_cont") val tradeContinuous: String?
    val next: (suspend () -> Response)?
}