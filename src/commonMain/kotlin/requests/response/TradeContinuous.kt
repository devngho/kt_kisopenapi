package io.github.devngho.kisopenapi.requests.response

import io.github.devngho.kisopenapi.requests.Data
import io.github.devngho.kisopenapi.requests.DataRequest
import io.github.devngho.kisopenapi.requests.Response
import kotlinx.serialization.SerialName

interface TradeContinuousData: Data {
    @SerialName("tr_cont")
    var tradeContinuous: String?
}

interface TradeContinuousResponse: Response {
    @SerialName("tr_cont")
    var tradeContinuous: String?
    var next: (suspend () -> Response)?
}

fun <T: DataRequest<U, V>, U: TradeContinuousData, V: TradeContinuousResponse> T.setNext(data: U, res: V) {
    if (res.tradeContinuous == "F" || res.tradeContinuous == "M") {
        res.next = {
            this.call(data.apply {
                tradeContinuous = "N"
            })
        }
    }
}