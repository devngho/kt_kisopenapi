package io.github.devngho.kisopenapi

import io.github.devngho.kisopenapi.requests.domestic.inquire.live.InquireLiveAskPrice
import io.github.devngho.kisopenapi.requests.domestic.inquire.live.InquireLiveConfirm
import io.github.devngho.kisopenapi.requests.domestic.inquire.live.InquireLivePrice
import io.github.devngho.kisopenapi.requests.overseas.inquire.live.InquireOverseasLiveConfirm
import io.github.devngho.kisopenapi.requests.overseas.inquire.live.InquireOverseasLivePrice
import io.github.devngho.kisopenapi.requests.response.LiveCallBody
import io.github.devngho.kisopenapi.requests.response.LiveResponse
import io.github.devngho.kisopenapi.requests.util.MarketWithUnified
import io.github.devngho.kisopenapi.requests.util.RequestCode
import io.github.devngho.kisopenapi.requests.util.Result
import io.github.devngho.kisopenapi.requests.util.json
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.util.logging.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.selects.onTimeout
import kotlinx.coroutines.selects.select

@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
@Suppress("SpellCheckingInspection")
class WebSocketTest : ShouldSpec({
    val logger = KtorSimpleLogger("WebSocketTest")

    GlobalScope.launch {
        api.webSocket.eventFlow.collect {
            logger.info(it.toString())
        }
    }

    GlobalScope.launch {
        api.webSocket.incoming.collect {
            logger.info(it)
        }
    }

    beforeEach {
        api.webSocket.buildWebsocket()
        logger.info("setup test websocket done")
    }

    should("연결할 수 있어야 한다") {
        api.webSocket.scope shouldNotBe null
    }

    should("연결된 후에는 종료할 수 있어야 한다") {
        api.webSocket.closeWebsocket()

        api.webSocket.scope shouldBe null
        api.webSocket.isConnected shouldBe false
    }

    should("연결 종료 시 종료 이벤트가 발생해야 한다") {
        val isClosed = Channel<Boolean>()

        launch {
            api.webSocket.eventFlow.first {
                it is KISApiClient.WebSocket.Event.OnClose
            }

            isClosed.send(true)
        }

        delay(100)
        api.webSocket.closeWebsocket()

        select {
            isClosed.onReceive {
                it shouldBe true
            }
            onTimeout(5000) {
                throw Exception("Timeout")
            }
        }
    }

    should("전송 시 전송 이벤트가 발생해야 한다") {
        val isSent = Channel<Boolean>()

        launch {
            api.webSocket.eventFlow.first {
                it is KISApiClient.WebSocket.Event.OnSend && it.message == "test"
            }

            isSent.send(true)
        }

        delay(100)
        api.webSocket.outgoing.send("test")

        select {
            isSent.onReceive {
                it shouldBe true
            }
            onTimeout(5000) {
                throw Exception("Timeout")
            }
        }
    }

    context("InquireLivePrice") {
        should("여러 종목의 실시간 가격 조회를 요청할 수 있다") {
            val instances = mutableListOf<Pair<InquireLivePrice, InquireLivePrice.InquireLivePriceData>>()

            testStocks.forEach {
                // given
                val instance = InquireLivePrice(api)
                val data = InquireLivePrice.InquireLivePriceData(it)
                instances.add(instance to data)

                // when
                instance.register(data, wait = true, force = false, { resp ->
                    // then
                    resp.getOrThrow().let {
                        it.body?.isOk shouldBe true
                        it.body?.code shouldBe RequestCode.SubscribeSuccess.code
                    }
                }) {}
            }

            instances.forEach { (instance, data) ->
                instance.unregister(data)
            }
        }

        should("NXT/KRX/통합 실시간 가격 조회를 요청할 수 있다") {
            MarketWithUnified.entries.forEach { market ->
                val instances = mutableListOf<Pair<InquireLivePrice, InquireLivePrice.InquireLivePriceData>>()

                testStocks.forEach {
                    // given
                    val instance = InquireLivePrice(api)
                    val data = InquireLivePrice.InquireLivePriceData(it, market)
                    instances.add(instance to data)

                    // when
                    instance.register(data, wait = true, force = false, { resp ->
                        // then
                        resp.getOrThrow().let {
                            it.body?.isOk shouldBe true
                            it.body?.code shouldBe RequestCode.SubscribeSuccess.code
                        }
                    }) {}
                }

                instances.forEach { (instance, data) ->
                    instance.unregister(data)
                }
            }
        }

        should("연결될 때까지 기다릴 수 있다") {
            // given
            val instance = InquireLivePrice(api)
            val data = InquireLivePrice.InquireLivePriceData(testStock)

            // when
            var isInitCallbackCalled = false
            var isRegisterDone = false


            launch {
                instance.register(data, true, force = false, {
                    isInitCallbackCalled = true
                }) {}

                isRegisterDone = true
            }

            delay(5000)

            // then
            isInitCallbackCalled shouldBe true
            isRegisterDone shouldBe true
        }

        should("연결 해제한 후에는 데이터를 수신할 수 없다") {
            // given
            val instance = InquireLivePrice(api)
            val data = InquireLivePrice.InquireLivePriceData(testStock)

            // when
            instance.register(data, wait = true) {
                // then
                throw Exception("Should not be called")
            }
            instance.unregister(data)

            val anotherInstance = InquireLivePrice(api)
            val anotherData = InquireLivePrice.InquireLivePriceData(testStock)
            anotherInstance.register(anotherData, wait = true) {
                // then
                it.ticker shouldBe "FAKE"
            }

            // fake 데이터를 보냄
            (api.webSocket as WebSocketMockClient).incoming.emit("0|H0STCNT0|001|${data.tradeKey(api)}^FAKE")

            delay(1000)

            anotherInstance.unregister(anotherData)
        }

        should("다른 인스턴스가 연결을 해제해도 데이터를 수신할 수 있다") {
            val instance = InquireLivePrice(api)
            val data = InquireLivePrice.InquireLivePriceData(testStock)

            // when
            instance.register(data, wait = true) {}

            delay(100)

            val anotherInstance = InquireLivePrice(api)
            val anotherData = InquireLivePrice.InquireLivePriceData(testStock)
            anotherInstance.register(anotherData, wait = true) {}

            delay(100)

            // 먼저 연결한 instance는 아직 연결되어 있으므로 서버에 unsubscribe 요청을 보내지 않음
            launch { anotherInstance.unregister(anotherData) }
            withTimeoutOrNull(1000) {
                api.webSocket.eventFlow.first { it is KISApiClient.WebSocket.Event.OnSend }
            } shouldBe null

            // 모든 인스턴스가 연결 해제되면 서버에 unsubscribe 요청을 보냄
            launch { instance.unregister(data) }
            api.webSocket.eventFlow.filterIsInstance<KISApiClient.WebSocket.Event.OnSend>().first().let {
                it
                    .let { message -> json.decodeFromString<LiveCallBody>(message.message) }
                    .let { body ->
                        // then
                        body.header.trType shouldBe "2" // unsubscribe
                    }
            }

            api.webSocket.closeWebsocket()
        }

        should("여러 인스턴스가 동시에 구독해도 1번만 구독 요청한다") {
            val data = InquireLivePrice.InquireLivePriceData(testStock)

            // when
            val instances = List(10) { InquireLivePrice(api) }

            val validate = launch {
                api.webSocket.eventFlow.first { it is KISApiClient.WebSocket.Event.OnSend }.let {
                    json.decodeFromString<LiveCallBody>((it as KISApiClient.WebSocket.Event.OnSend).message)
                        .let { body ->
                            body.header.trType shouldBe "1" // 구독
                        }
                }
                (withTimeoutOrNull(5000) { // 더 이상 subscribe 요청이 없는지 확인
                    api.webSocket.eventFlow.first()
                    false
                } != false) shouldBe true
            }

            delay(100)

            instances.forEachIndexed { i, it ->
                it.register(data) {}
            }

            validate.join()
        }

        should("연결이 끊기고 구독하면 다시 구독 요청한다") {
            val data = InquireLivePrice.InquireLivePriceData(testStock)

            // when
            val instance = InquireLivePrice(api)
            launch { instance.register(data) {} }
            (api.webSocket.eventFlow.first { it is KISApiClient.WebSocket.Event.OnSend } as KISApiClient.WebSocket.Event.OnSend).message.let {
                it
                    .let { frame -> json.decodeFromString<LiveCallBody>(frame) }
                    .let { body ->
                        body.header.trType shouldBe "1" // subscribe
                    }
            }

            delay(100)

            api.webSocket.closeWebsocket()

            delay(100)

            launch { instance.register(data) {} }

            // then
            (api.webSocket.eventFlow.first { it is KISApiClient.WebSocket.Event.OnSend } as KISApiClient.WebSocket.Event.OnSend).message.let {
                it
                    .let { frame -> json.decodeFromString<LiveCallBody>(frame) }
                    .let { body ->
                        body.header.trType shouldBe "1" // subscribe
                    }
            }
        }

        should("여러 값이 동시에 들어오면 차례대로 처리한다") {
            val instance = InquireLivePrice(api)
            val data = InquireLivePrice.InquireLivePriceData(testStock)
            var callCount = 0

            // when
            instance.register(data, wait = true) {
                callCount += 1
            }

            val textData =
                "${data.tradeKey(api)}^093354^71900^5^-100^-0.14^72023.83^72100^72400^71700^71900^71800^1^3052507^219853241700^5105^6937^1832^84.90^1366314^1159996^1^0.39^20.28^090020^5^-200^090820^5^-500^092619^2^200^20230612^20^N^65945^216924^1118750^2199206^0.05^2424142^125.92^0^^72100"

            // fake 데이터를 보냄
            (api.webSocket as WebSocketMockClient).incoming.emit("0|H0UNCNT0|004|$textData^$textData^$textData^$textData")

            delay(1000)

            callCount shouldBe 4

            instance.unregister(data)
        }
    }

    context("InquireOverseasLivePrice") {
        should("여러 종목의 실시간 가격 조회를 요청할 수 있어야 한다") {
            val instances =
                mutableListOf<Pair<InquireOverseasLivePrice, InquireOverseasLivePrice.InquireOverseasLivePriceData>>()

            testOverseasStocks.forEach {
                val registerChannel = Channel<Unit>()
                launch {
                    // given
                    val instance = InquireOverseasLivePrice(api)
                    val data = InquireOverseasLivePrice.InquireOverseasLivePriceData(it, testOverseasMarket)
                    instances.add(instance to data)

                    // when
                    instance.register(data, true, force = false, { resp ->
                        // then
                        resp.getOrThrow().let {
                            it.body?.isOk shouldBe true
                            it.body?.code shouldBe RequestCode.SubscribeSuccess.code
                        }
                    }) {}

                    registerChannel.send(Unit)
                }

                select {
                    registerChannel.onReceive { }
                    onTimeout(5000) {
                        throw Exception("Timeout")
                    }
                }

                delay(1000)
            }

            instances.forEach { (instance, data) ->
                instance.unregister(data)
            }
        }

        should("연결될 때까지 기다릴 수 있다") {
            // given
            val instance = InquireOverseasLivePrice(api)
            val data = InquireOverseasLivePrice.InquireOverseasLivePriceData(testOverseasStock, testOverseasMarket)

            // when
            var isInitCallbackCalled = false
            var isRegisterDone = false


            launch {
                instance.register(data, true, force = false, {
                    isInitCallbackCalled = true
                }) {}

                isRegisterDone = true
            }

            delay(5000)

            // then
            isInitCallbackCalled shouldBe true
            isRegisterDone shouldBe true
        }

        should("연결 해제한 후에는 데이터를 수신할 수 없다") {
            // given
            val instance = InquireOverseasLivePrice(api)
            val data = InquireOverseasLivePrice.InquireOverseasLivePriceData(testOverseasStock, testOverseasMarket)
            val anotherInstance = InquireOverseasLivePrice(api)

            // when
            launch {
                instance.register(data, wait = true) {
                    // then
                    throw Exception("Should not be called")
                }
                instance.unregister(data)

                anotherInstance.register(data, wait = true) {
                    // then
                    it.ticker shouldBe "FAKE"
                }
            }

            // fake 데이터를 보냄
            (api.webSocket.incoming as MutableSharedFlow<String>).emit("0|HDFSCNT0|${data.tradeKey(api)}^FAKE")

            delay(1000)

            anotherInstance.unregister(data)
        }

        should("다른 인스턴스가 연결을 해제해도 데이터를 수신할 수 있다") {
            val instance = InquireOverseasLivePrice(api)
            val data = InquireOverseasLivePrice.InquireOverseasLivePriceData(testOverseasStock, testOverseasMarket)

            // when
            instance.register(data) {}

            delay(100)

            val anotherInstance = InquireOverseasLivePrice(api)
            anotherInstance.register(data) {}

            delay(100)

            // 먼저 연결한 instance는 아직 연결되어 있으므로 서버에 unsubscribe 요청을 보내지 않음
            launch { anotherInstance.unregister(data) }
            withTimeoutOrNull(1000) {
                api.webSocket.eventFlow.first { it is KISApiClient.WebSocket.Event.OnSend }
            } shouldBe null

            // 모든 인스턴스가 연결 해제되면 서버에 unsubscribe 요청을 보냄
            launch { instance.unregister(data) }
            api.webSocket.eventFlow.filterIsInstance<KISApiClient.WebSocket.Event.OnSend>().first().message
                .also { s -> (api.webSocket.incoming as MutableSharedFlow<String>).emit(s) }
                .let { frame -> json.decodeFromString<LiveCallBody>(frame) }
                .let { body ->
                    // then
                    body.header.trType shouldBe "2" // unsubscribe
                }

            api.webSocket.closeWebsocket()
        }

        should("여러 인스턴스가 동시에 구독해도 1번만 구독 요청한다") {
            // given - instance
            val data = InquireOverseasLivePrice.InquireOverseasLivePriceData(testOverseasStock, testOverseasMarket)

            val valid = launch {
                api.webSocket.eventFlow.filterIsInstance<KISApiClient.WebSocket.Event.OnSend>().first().let {
                    json.decodeFromString<LiveCallBody>(it.message)
                        .let { body ->
                            body.header.trType shouldBe "1" // subscribe
                        }
                }
                (withTimeoutOrNull(5000) {
                    api.webSocket.eventFlow.first()
                    false
                } != false) shouldBe true
            }

            delay(100)

            // when
            val instances = List(10) { InquireOverseasLivePrice(api) }
            instances.forEachIndexed { i, it ->
                it.register(data) {}
            }

            // then
            valid.join()
        }
    }

    context("InquireLiveConfirm") {
        should("실시간 체결 조회를 요청할 수 있어야 한다") {
            val instance = InquireLiveConfirm(api)
            val data = InquireLiveConfirm.InquireLiveConfirmData()

            instance.register(data, wait = true, force = false, { resp ->
                resp shouldNotBe null
                resp.getOrThrow().let {
                    it.body?.isOk shouldBe true
                    it.body?.code shouldBe RequestCode.SubscribeSuccess.code
                }
            }) {}

            instance.unregister(data)
        }
    }

    context("InquireOverseasLiveConfirm") {
        should("실시간 체결 조회를 요청할 수 있어야 한다") {
            val instance = InquireOverseasLiveConfirm(api)
            val data = InquireOverseasLiveConfirm.InquireOverseasLiveConfirmData()

            instance.register(data, wait = true, force = false, {
                it shouldNotBe null
                it.getOrThrow().let {
                    it.body?.isOk shouldBe true
                    it.body?.code shouldBe RequestCode.SubscribeSuccess.code
                }
            }) {
                logger.info(it.toString())
            }

            instance.unregister(data)
        }
    }

    context("InquireLiveAskPrice") {
        should("실시간 호가를 요청할 수 있어야 한다") {
            val instance = InquireLiveAskPrice(api)
            val data = InquireLiveAskPrice.InquireLiveAskPriceData(testStock)

            instance.register(data, wait = true, force = false, { resp ->
                resp shouldNotBe null
                resp.getOrThrow().let {
                    it.body?.isOk shouldBe true
                    it.body?.code shouldBe RequestCode.SubscribeSuccess.code
                }
            }) {
                logger.info(it.toString())
            }

            instance.unregister(data)
        }
    }

    context("Reconnect") {
        should("종료할 수 있다") {
            delay(1000)

            api.options.autoReconnect = true
            api.webSocket.closeWebsocket()

            delay(1000)

            api.webSocket.scope shouldBe null
            api.webSocket.isConnected shouldBe false

            api.options.autoReconnect = false
        }

        should("구독 목록을 다시 전송한다") {
            val instance = InquireLivePrice(api)
            val data = InquireLivePrice.InquireLivePriceData(testStock)

            instance.register(data, wait = true) {}

            val body = CompletableDeferred<String>()
            launch {
                api.webSocket.eventFlow.first { it is KISApiClient.WebSocket.Event.OnSend }.let {
                    body.complete((it as KISApiClient.WebSocket.Event.OnSend).message)
                }
            }

            delay(100)
            api.webSocket.reconnectWebsocket()

            json.decodeFromString<LiveCallBody>(body.await())
                .let {
                    it.header.trType shouldBe "1" // subscribe
                }
        }

        should("동일한 대상의 구독이 여러 번 요청되지 않는다") {
            val instance = InquireLivePrice(api)
            val data = InquireLivePrice.InquireLivePriceData(testStock)
            val results = mutableListOf<Result<LiveResponse>>()

            repeat(2) { i ->
                instance.register(data, wait = true, init = {
                    results.add(it)
                }) { }
            }

            val isErrored = CompletableDeferred<Boolean>()

            launch {
                withTimeout(5000) {
                    api.webSocket.eventFlow.first { it is KISApiClient.WebSocket.Event.OnError }

                    isErrored.complete(true)
                }
            }

            delay(100)
            api.webSocket.reconnectWebsocket()

            select {
                isErrored.onAwait {
                    it shouldBe false
                }

                onTimeout(5000) {
                    // ok
                }
            }

            results.forEach {
                it.isOk shouldBe true
            }
        }
    }
}) {
    /**
     * 웹소켓 클라이언트를 테스트하기 위한 Mock 클래스입니다.
     *
     * 원본의 incoming을 MutableSharedFlow로 대체합니다.
     * 따라서 테스트 시 incoming을 emit하여 테스트할 수 있습니다.
     *
     * 자세한 예시는 InquireLivePrice 테스트 중 *연결 해제한 후에는 데이터를 수신할 수 없다*를 확인하세요.
     *
     * @property raw 원본 웹소켓 클라이언트
     */
    class WebSocketMockClient(private val raw: KISApiClient.WebSocket) : KISApiClient.WebSocket by raw {
        private val coroutineScope = CoroutineScope(Dispatchers.Default)

        init {
            coroutineScope.launch {
                raw.incoming.collect {
                    incoming.emit(it)
                }
            }
        }

        override val incoming = MutableSharedFlow<String>()
    }
}