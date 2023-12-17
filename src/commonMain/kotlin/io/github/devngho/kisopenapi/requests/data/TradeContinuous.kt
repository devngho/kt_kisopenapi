package io.github.devngho.kisopenapi.requests.data

import io.github.devngho.kisopenapi.requests.Data
import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.util.Result
import kotlinx.serialization.SerialName

interface TradeContinuousData : Data {
    @SerialName("tr_cont")
    var tradeContinuous: String?
}

interface TradeContinuousResponse<T : Response> : Response {
    @SerialName("tr_cont")
    var tradeContinuous: String?
    var next: (suspend () -> Result<T>)?
}