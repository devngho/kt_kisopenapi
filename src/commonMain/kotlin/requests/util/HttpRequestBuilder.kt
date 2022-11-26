package io.github.devngho.kisopenapi.requests.util

import io.github.devngho.kisopenapi.KisOpenApi
import io.github.devngho.kisopenapi.requests.response.CorporationRequest
import io.ktor.client.request.*
import io.ktor.http.*

fun HttpRequestBuilder.auth(client: KisOpenApi) {
    contentType(ContentType.Application.Json)
    headers {
        append(HttpHeaders.Authorization, "Bearer ${client.oauthToken}")
        append("appkey", client.appKey)
        append("appsecret", client.appSecret)
    }
}

fun HttpRequestBuilder.stock(stockCode: String) {
    url {
        parameters.run {
            append("FID_COND_MRKT_DIV_CODE", "J")
            append("FID_INPUT_ISCD", stockCode)
        }
    }
}

fun HttpRequestBuilder.tradeId(tradeId: String) {
    headers {
        append("tr_id", tradeId)
    }
}

fun HttpRequestBuilder.corporation(corp: CorporationRequest){
    headers {
        corp.globalUID?.let { append("gt_uid", it) }
        corp.consumerType?.let { append("custtype", it.num) }
        corp.phoneNumber?.let { append("phone_number", it.replace("-", "").trim()) }
        corp.ipAddr?.let { append("ip_addr", it.replace(":", "").trim()) }
        corp.personalSecKey?.let { append("personalseckey", it) }
    }
}