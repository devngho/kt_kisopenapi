package io.github.devngho.kisopenapi.requests.util

import io.github.devngho.kisopenapi.requests.LiveData
import io.github.devngho.kisopenapi.requests.LiveRequest
import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.response.LiveResponse

/**
 * 웹소켓 구독 정보를 나타냅니다.
 *
 * @param request 구독 요청
 * @param data 구독 요청 데이터
 * @param initFunc 구독 초기화 함수
 * @param block 구독 콜백
 */
@InternalApi
data class WebSocketSubscribed(
    val request: LiveRequest<LiveData, Response>,
    val data: LiveData,
    val initFunc: (suspend (Result<LiveResponse>) -> Unit)? = null,
    val block: suspend (Response) -> Unit
)