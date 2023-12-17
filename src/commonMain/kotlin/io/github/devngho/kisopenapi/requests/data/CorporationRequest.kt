package io.github.devngho.kisopenapi.requests.data

import io.github.devngho.kisopenapi.requests.util.ConsumerTypeCode

data class CorporationRequest (
    val consumerType: ConsumerTypeCode? = ConsumerTypeCode.Personal,
    val globalUID: String? = null,
    val ipAddr: String? = null,
    val phoneNumber: String? = null,
    val personalSecKey: String? = null
)