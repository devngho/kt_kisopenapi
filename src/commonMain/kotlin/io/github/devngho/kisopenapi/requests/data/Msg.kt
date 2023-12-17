package io.github.devngho.kisopenapi.requests.data

import kotlinx.serialization.SerialName

interface Msg {
    @SerialName("msg_cd") val code: String?
    @SerialName("msg1") val msg: String?
    @SerialName("rt_cd") val isOk: Boolean?
}