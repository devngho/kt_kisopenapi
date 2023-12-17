package io.github.devngho.kisopenapi.requests.util

import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.LiveData
import io.github.devngho.kisopenapi.requests.LiveRequest
import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.response.LiveCallBody
import io.github.devngho.kisopenapi.requests.response.LiveResponse
import io.ktor.util.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

private fun isMatchCondition(response: String, tradeId: String, tradeKey: String, code: List<String>): Boolean {
    if (response[0] == '0' || response[0] == '1') return false

    val liveResponse = json.decodeFromString<LiveResponse>(response)
    return (liveResponse.header?.tradeId == tradeId) &&
            (liveResponse.header.tradeKey == tradeKey) &&
            code.contains(liveResponse.body?.code)
}

/**
 * 웹소켓으로 들어오는 특정 조건의 [LiveResponse] 데이터를 기다립니다.
 *
 * @param client [KISApiClient]
 */
internal suspend fun waitFor(client: KISApiClient, tradeId: String, tradeKey: String, vararg code: RequestCode) {
    val codes = code.map { it.code }
    client.webSocketIncoming!!
        .first { isMatchCondition(it, tradeId, tradeKey, codes) }
}

/**
 * 웹소켓으로 들어오는 특정 조건의 [LiveResponse] 데이터를 기다립니다. 값이 들어오면 [block]을 실행합니다.
 *
 * @param client [KISApiClient]
 * @param block [LiveResponse]를 받아 처리하는 블럭
 */
@OptIn(ExperimentalContracts::class)
internal suspend fun <T> waitFor(
    client: KISApiClient,
    tradeId: String,
    tradeKey: String,
    vararg code: RequestCode,
    block: suspend (LiveResponse) -> T
) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    val codes = code.map { it.code }
    val value =
        client.webSocketIncoming!!
            .first { isMatchCondition(it, tradeId, tradeKey, codes) }

    block(json.decodeFromString(value))
}

/**
 * 실시간 요청을 수행합니다.
 * @param data 요청 데이터
 * @param subscribed 요청 등록 정보
 * @param tradeId 요청 ID
 * @param tradeKey 요청 키
 * @param wait 요청이 완료될 때까지 대기할지 여부입니다.
 * @param updateJob 인스턴스의 job을 설정하는 함수
 * @param transformBody 수신된 데이터를 [T]로 변환하는 함수
 * @param block 요청을 수행하는 함수
 */
@OptIn(ExperimentalContracts::class)
internal suspend fun <T : Response, U : LiveData> LiveRequest<U, T>.requestStart(
    data: U,
    subscribed: KISApiClient.WebSocketSubscribed,
    tradeId: String,
    tradeKey: String,
    wait: Boolean,
    updateJob: (Job) -> Unit,
    init: suspend (Result<LiveResponse>) -> Unit,
    block: suspend (T) -> Unit,
    transformBody: suspend (List<String>) -> T
) {
    contract {
        callsInPlace(init, InvocationKind.EXACTLY_ONCE)
        callsInPlace(block, InvocationKind.UNKNOWN)
    }

    coroutineScope {
        val req = this@requestStart
        if (client.webSocketScope == null) client.buildWebsocket()

        val processedData = data.apply {
            this.corp = this.corp ?: req.client.corp
        }

        val requestRequired = client.subscribeStart(subscribed) {
            init(Result(it))
        }

        lateinit var key: String
        lateinit var iv: String

        var waitJob: Job? = null

        if (requestRequired) {
            waitJob = client.webSocketScope!!.launch {
                waitFor(client, tradeId, tradeKey, RequestCode.SubscribeSuccess, RequestCode.AlreadyInSubscribe) {
                    client.subscribeDone(subscribed, it)

                    if (
                        it.body?.output?.iv == null ||
                        it.body.output.key == null
                    ) {
                        init(Result(it, RequestException("IV or Key is null.", RequestCode.Unknown)))
                        return@waitFor
                    }

                    iv = it.body.output.iv
                    key = it.body.output.key

                    init(Result(it))
                }
            }
        }

        if (requestRequired) {
            client.webSocketScope!!.launch {
                val body = LiveCallBody.buildCallBody(
                    token = client.webSocketToken!!,
                    consumerType = processedData.corp!!.consumerType!!.num,
                    trId = tradeId,
                    trKey = tradeKey,
                    trType = "1"
                )

                client.webSocketOutgoing!!.send(body)
            }
        }

        client.webSocketScope!!.launch {
            client.webSocketIncoming?.collect {
                if (it[0] != '0' && it[0] != '1') return@collect // 데이터 값은 0, 1로만 시작합니다.

                val list = it.split("|")
                if (list[1] != tradeId) return@collect

                val body = (
                        if (it[0] == '1') AES.decodeAES(key, iv, list[3].decodeBase64Bytes())
                        else list[3]
                        ).split("^")

                if (body[0] != tradeKey) return@collect
                block(transformBody(body))
            }
        }.let { updateJob.invoke(it) }

        if (wait) waitJob?.join()
    }
}

/**
 * 실시간 요청을 종료합니다.
 * @param data 요청 데이터
 * @param subscribed 요청 등록 정보
 * @param tradeId 요청 ID
 * @param tradeKey 요청 키
 * @param wait 요청이 완료될 때까지 대기할지 여부입니다.
 */
internal suspend fun <T : Response, U : LiveData> LiveRequest<U, T>.requestEnd(
    data: U,
    subscribed: KISApiClient.WebSocketSubscribed?,
    tradeId: String,
    tradeKey: String,
    wait: Boolean,
    job: Job?
) {
    coroutineScope {
        val req = this@requestEnd
        if (client.webSocketScope == null) {
            job?.cancel()
            return@coroutineScope
        }

        val processedData = data.apply {
            this.corp = this.corp ?: req.client.corp
        }

        val requestRequired = client.unsubscribe(subscribed!!)

        var waitJob: Job? = null

        if (requestRequired) {
            waitJob = client.webSocketScope!!.launch {
                waitFor(client, tradeId, tradeKey, RequestCode.UnsubscribeSuccess, RequestCode.UnsubscribeErrorNotFound)
            }
        }

        if (requestRequired) {
            client.webSocketScope!!.launch {
                val body = LiveCallBody.buildCallBody(
                    token = client.webSocketToken!!,
                    consumerType = processedData.corp!!.consumerType!!.num,
                    trId = tradeId,
                    trKey = tradeKey,
                    trType = "2"
                )

                client.webSocketOutgoing!!.send(body)
            }
        }

        if (wait) waitJob?.join()

        job?.cancel()
    }
}
