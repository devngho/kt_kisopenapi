package io.github.devngho.kisopenapi.requests.util

import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.LiveData
import io.github.devngho.kisopenapi.requests.LiveRequest
import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.response.LiveCallBody
import io.github.devngho.kisopenapi.requests.response.LiveResponse
import io.github.devngho.kisopenapi.requests.response.LiveResponseBodyOutput
import io.ktor.util.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * [response]가 [tradeId], [tradeKey], [code]에 적합한 [LiveResponse]인지 확인합니다.
 *
 * @param response 확인할 값
 * @param tradeId 요청 ID
 * @param tradeKey 요청 키
 * @param code 적합한 코드
 */
private fun isMatchCondition(response: String, tradeId: String, tradeKey: String, code: List<String>): Boolean {
    if (response[0] == '0' || response[0] == '1') return false

    val liveResponse = json.decodeFromString<LiveResponse>(response)
    return (liveResponse.header?.tradeId == tradeId) &&
            (liveResponse.header.tradeKey == tradeKey) &&
            code.contains(liveResponse.body?.code)
}

@Suppress("SpellCheckingInspection")
/**
 * 웹소켓으로 들어오는 [tradeId]와 [tradeKey], [code]에 적합한 [LiveResponse] 데이터를 기다립니다.
 *
 * @param client [KISApiClient]
 * @param tradeId 요청 ID
 * @param tradeKey 요청 키
 * @param code 적합한 코드
 */
internal suspend fun waitFor(client: KISApiClient, tradeId: String, tradeKey: String, vararg code: RequestCode) {
    val codes = code.map { it.code }
    client.webSocket.incoming
        .first { isMatchCondition(it, tradeId, tradeKey, codes) }
}

@Suppress("SpellCheckingInspection")
/**
 * 웹소켓으로 들어오는 적합한 [LiveResponse] 데이터를 기다립니다. 값이 들어오면 [block]을 실행합니다.
 *
 * @param client [KISApiClient]
 * @param tradeId 요청 ID
 * @param tradeKey 요청 키
 * @param code 적합한 코드
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
        client.webSocket.incoming
            .first { isMatchCondition(it, tradeId, tradeKey, codes) }

    block(json.decodeFromString(value))
}

@Suppress("SpellCheckingInspection")
/**
 * 실시간 요청을 수행합니다.
 *
 * @param data 요청 데이터
 * @param subscribed 요청 등록 정보
 * @param tradeId 요청 ID
 * @param tradeKey 요청 키
 * @param wait 요청이 완료될 때까지 대기할지 여부입니다.
 * @param updateJob 인스턴스의 job을 설정하는 함수
 * @param transformBody 수신된 데이터를 [T]로 변환하는 함수
 * @param block 요청을 수행하는 함수
 */
@OptIn(ExperimentalContracts::class, InternalApi::class)
internal suspend fun <T : Response, U : LiveData> LiveRequest<U, T>.requestStart(
    data: U,
    subscribed: WebSocketSubscribed,
    tradeId: String,
    tradeKey: String,
    wait: Boolean,
    force: Boolean,
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
        if (!client.webSocket.isConnected) client.webSocket.buildWebsocket()

        val processedData = data.apply {
            this.corp = this.corp ?: req.client.corpRequest
        }

        lateinit var key: String
        lateinit var iv: String

        val requestRequired = client.webSocketManager.subscribeStart(subscribed, force = force) {
            iv = it.body!!.output!!.iv!!
            key = it.body.output!!.key!!

            init(Result(it))
        }

        attachListener(
            client,
            tradeId,
            tradeKey,
            getAES = { key to iv },
            block = block,
            transformBody = transformBody
        ).let { updateJob(it) }

        if (requestRequired) {
            val waitJob = createWaitJob(
                client,
                tradeId,
                tradeKey,
                subscribed,
                init,
                setAES = { output ->
                    key = output.key!!
                    iv = output.iv!!
                })

            sendSubscribeRequest(client, processedData, tradeId, tradeKey)

            if (wait) waitJob.join()
        }
    }
}

/**
 * 구독 요청이 완료될 때까지 대기하고, 구독 요청을 처리하는 [Job]을 생성하고 반환합니다.
 *
 * @param client [KISApiClient]
 * @param tradeId 요청 ID
 * @param tradeKey 요청 키
 * @param subscribed 요청 등록 정보
 * @param init 구독 요청이 완료되었을 때 실행할 함수
 * @param setAES AES 키를 설정하는 함수
 * @return 구독 요청을 처리하는 [Job]
 */
@OptIn(InternalApi::class)
private fun createWaitJob(
    client: KISApiClient,
    tradeId: String,
    tradeKey: String,
    subscribed: WebSocketSubscribed,
    init: suspend (Result<LiveResponse>) -> Unit,
    setAES: suspend (LiveResponseBodyOutput) -> Unit
) = client.webSocket.scope!!.launch {
    waitFor(client, tradeId, tradeKey, RequestCode.SubscribeSuccess, RequestCode.AlreadyInSubscribe) {
        client.webSocketManager.subscribeDone(subscribed, it)

        if (
            it.body?.output?.iv == null ||
            it.body.output.key == null
        ) {
            init(Result(it, RequestException("IV or Key is null.", RequestCode.Unknown)))
            return@waitFor
        }

        setAES(it.body.output)
        init(Result(it))
    }
}

/**
 * 구독 요청을 전송합니다.
 *
 * @param client [KISApiClient]
 * @param data 요청 데이터
 * @param tradeId 요청 ID
 * @param tradeKey 요청 키
 */
private suspend fun sendSubscribeRequest(
    client: KISApiClient,
    data: LiveData,
    tradeId: String,
    tradeKey: String
) {
    val body = LiveCallBody.buildCallBody(
        token = client.tokens.webSocketToken!!,
        consumerType = data.corp!!.consumerType!!.num,
        trId = tradeId,
        trKey = tradeKey,
        trType = "1"
    )

    client.webSocket.outgoing.send(body)
}

/**
 * 웹소켓으로 들어오는 데이터를 처리하는 [Job]을 생성하고 반환합니다.
 *
 * @param client [KISApiClient]
 * @param tradeId 요청 ID
 * @param tradeKey 요청 키
 * @param getAES AES 키를 반환하는 함수
 * @param block 데이터를 처리하는 함수
 * @param transformBody 수신된 데이터를 [T]로 변환하는 함수
 * @return 데이터를 처리하는 [Job]
 */
private fun <T : Response> attachListener(
    client: KISApiClient,
    tradeId: String,
    tradeKey: String,
    getAES: suspend () -> Pair<String, String>,
    block: suspend (T) -> Unit,
    transformBody: suspend (List<String>) -> T
) = client.webSocket.scope!!.launch {
    client.webSocket.incoming.collect {
        if (it[0] != '0' && it[0] != '1') return@collect // 데이터 값은 0, 1로만 시작합니다.

        val list = it.split("|")
        if (list[1] != tradeId) return@collect

        val body = (
                if (it[0] == '1') {
                    val (key, iv) = getAES()
                    AES.decodeAES(key, iv, list[3].decodeBase64Bytes())
                } else list[3]
                ).split("^")

        if (body[0] != tradeKey) return@collect
        block(transformBody(body))
    }
}

@OptIn(InternalApi::class)
@Suppress("SpellCheckingInspection")
/**
 * 실시간 요청을 종료합니다.
 *
 * @param data 요청 데이터
 * @param subscribed 요청 등록 정보
 * @param tradeId 요청 ID
 * @param tradeKey 요청 키
 * @param wait 요청이 완료될 때까지 대기하려면 true, 아니면 false
 */
internal suspend fun <T : Response, U : LiveData> LiveRequest<U, T>.requestEnd(
    data: U,
    subscribed: WebSocketSubscribed,
    tradeId: String,
    tradeKey: String,
    wait: Boolean,
    job: Job?
) {
    coroutineScope {
        val req = this@requestEnd
        if (client.webSocket.scope == null) {
            job?.cancel()
            return@coroutineScope
        }

        val processedData = data.apply {
            this.corp = this.corp ?: req.client.corpRequest
        }

        val requestRequired = client.webSocketManager.unsubscribe(subscribed)

        var waitJob: Job? = null

        if (requestRequired) {
            waitJob = client.webSocket.scope!!.launch {
                waitFor(client, tradeId, tradeKey, RequestCode.UnsubscribeSuccess, RequestCode.UnsubscribeErrorNotFound)
            }
        }

        if (requestRequired) {
            client.webSocket.scope!!.launch {
                val body = LiveCallBody.buildCallBody(
                    token = client.tokens.webSocketToken!!,
                    consumerType = processedData.corp!!.consumerType!!.num,
                    trId = tradeId,
                    trKey = tradeKey,
                    trType = "2"
                )

                client.webSocket.outgoing.send(body)
            }
        }

        waitJob?.let {
            if (wait) it.join()
            it.cancel()
        }
    }
}
