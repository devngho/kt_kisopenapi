package io.github.devngho.kisopenapi.requests.util

import io.github.devngho.kisopenapi.KisOpenApi
import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.response.CorporationRequest
import io.github.devngho.kisopenapi.requests.response.TradeContinuousResponse
import io.github.devngho.kisopenapi.requests.response.TradeIdMsg
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

fun HttpRequestBuilder.auth(client: KisOpenApi) {
    contentType(ContentType.Application.Json)
    headers {
        append(HttpHeaders.Authorization, "Bearer ${client.oauthToken}")
        append("appkey", client.appKey)
        append("appsecret", client.appSecret)
    }
}

fun HttpRequestBuilder.stock(ticker: String) {
    url {
        parameters.run {
            append("FID_COND_MRKT_DIV_CODE", "J")
            append("FID_INPUT_ISCD", ticker)
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
        append("seq_no", if(corp.consumerType == ConsumerTypeCode.Personal) "" else "01")
    }
}

fun <T: Response> T.processHeader(res: HttpResponse){
    res.headers.forEach { s, strings ->
        when(s) {
            "tr_id" -> if (this is TradeIdMsg) this.tradeId = strings[0]
            "tr_cont" -> if (this is TradeContinuousResponse) this.tradeContinuous = strings[0]
            "gt_uid" -> if (this is TradeIdMsg) this.globalTradeID = strings[0]
        }
    }
}