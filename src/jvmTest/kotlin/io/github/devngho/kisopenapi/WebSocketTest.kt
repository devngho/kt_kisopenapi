package io.github.devngho.kisopenapi

import io.github.devngho.kisopenapi.requests.domestic.inquire.live.InquireLiveAskPrice
import io.github.devngho.kisopenapi.requests.domestic.inquire.live.InquireLiveConfirm
import io.github.devngho.kisopenapi.requests.domestic.inquire.live.InquireLivePrice
import io.github.devngho.kisopenapi.requests.overseas.inquire.live.InquireOverseasLiveConfirm
import io.github.devngho.kisopenapi.requests.overseas.inquire.live.InquireOverseasLivePrice
import io.github.devngho.kisopenapi.requests.response.LiveCallBody
import io.github.devngho.kisopenapi.requests.util.RequestCode
import io.github.devngho.kisopenapi.requests.util.json
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.util.logging.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.selects.onTimeout
import kotlinx.coroutines.selects.select

@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
class WebSocketTest : ShouldSpec({
    val logger = KtorSimpleLogger("WebSocketTest")

    GlobalScope.launch {
        api.webSocketEventFlow.collect {
            logger.info(it.toString())
        }
    }

    beforeTest {
        api.closeWebsocket()
        api.buildWebsocket()

        GlobalScope.run {
            launch {
                api.webSocketIncoming!!.collect {
                    logger.info(it)
                }
            }
        }
        delay(1000)
    }

    /**
     * KISApiClient의 websocketOutgoing을 재설정하고, websocket으로 보내는 데이터를 받을 수 있는 채널을 반환합니다.
     */
    fun attachMockWebsocket(): Channel<String> {
        val mockOutgoing = Channel<String>(8)
        api.webSocketOutgoing = mockOutgoing
        return mockOutgoing
    }

    fun createFakeSubscribeDone(tradeId: String, tradeKey: String): String =
        "{\"header\":{\"tr_id\":\"$tradeId\",\"tr_key\":\"$tradeKey\",\"encrypt\":\"N\"},\"body\":{\"rt_cd\":\"0\",\"msg_cd\":\"OPSP0000\",\"msg1\":\"SUBSCRIBE SUCCESS\",\"output\":{\"iv\":\"\",\"key\":\"\"}}}"

    should("연결할 수 있어야 한다") {
        api.webSocketScope shouldNotBe null
        api.webSocketIncoming shouldNotBe null
    }

    should("연결된 후에는 종료할 수 있어야 한다") {
        delay(1000)

        api.closeWebsocket()

        api.webSocketScope shouldBe null
        api.webSocketIncoming shouldBe null
    }

    should("연결 종료 시 종료 이벤트가 발생해야 한다") {
        val isClosed = Channel<Boolean>()

        launch {
            api.webSocketEventFlow.first {
                it is KISApiClient.Event.OnClose
            }

            isClosed.send(true)
        }

        api.closeWebsocket()

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
        val isClosed = Channel<Boolean>()

        launch {
            api.webSocketEventFlow.first {
                it is KISApiClient.Event.OnSend && it.message == "test"
            }

            isClosed.send(true)
        }

        delay(100)
        api.webSocketOutgoing!!.send("test")

        select {
            isClosed.onReceive {
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
                val registerChannel = Channel<Unit>()
                launch {
                    // given
                    val instance = InquireLivePrice(api)
                    val data = InquireLivePrice.InquireLivePriceData(it)
                    instances.add(instance to data)

                    // when
                    instance.register(data, wait = false, force = false, { resp ->
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
            (api.webSocketIncoming!! as MutableSharedFlow<String>).emit("0|HDFSCNT0|${data.tradeKey(api)}^FAKE")

            delay(1000)

            anotherInstance.unregister(anotherData)
        }

        should("다른 인스턴스가 연결을 해제해도 데이터를 수신할 수 있다") {
            val instance = InquireLivePrice(api)
            val data = InquireLivePrice.InquireLivePriceData(testStock)

            // when
            launch { instance.register(data) {} }
            api.webSocketEventFlow.first { it is KISApiClient.Event.OnSend }

            delay(100)

            val anotherInstance = InquireLivePrice(api)
            val anotherData = InquireLivePrice.InquireLivePriceData(testStock)
            anotherInstance.register(anotherData) {}

            delay(100)

            // 먼저 연결한 instance는 아직 연결되어 있으므로 서버에 unsubscribe 요청을 보내지 않음
            launch { anotherInstance.unregister(anotherData) }
            withTimeoutOrNull(1000) {
                api.webSocketEventFlow.first { it is KISApiClient.Event.OnSend }
            } shouldBe null

            // 모든 인스턴스가 연결 해제되면 서버에 unsubscribe 요청을 보냄
            launch { instance.unregister(data) }
            api.webSocketEventFlow.filterIsInstance<KISApiClient.Event.OnSend>().first().let {
                it
                    .let { message -> json.decodeFromString<LiveCallBody>(message.message) }
                    .let { body ->
                        // then
                        body.header.trType shouldBe "2" // unsubscribe
                    }
            }

            api.closeWebsocket()
        }

        should("여러 인스턴스가 동시에 구독해도 1번만 구독 요청한다") {
            // given - instance
            val mockOutgoing = attachMockWebsocket()
            val data = InquireLivePrice.InquireLivePriceData(testStock)
            var res = ""

            // when
            val instances = List(10) { InquireLivePrice(api) }
            instances.forEachIndexed { i, it ->
                it.register(data) {}
                if (i == 0) {
                    res = mockOutgoing.receive() // subscribe request
                    (api.webSocketIncoming!! as MutableSharedFlow<String>).emit(
                        createFakeSubscribeDone(
                            "H0STCNT0", data.tradeKey(
                                api
                            )
                        )
                    ) // fake subscribe done
                }
            }

            // then
            res.let {
                it
                    .also { s -> (api.webSocketIncoming!! as MutableSharedFlow<String>).emit(s) }
                    .let { frame -> json.decodeFromString<LiveCallBody>(frame) }
                    .let { body ->
                        body.header.trType shouldBe "1" // subscribe
                    }
            }
            mockOutgoing.tryReceive().isSuccess shouldBe false // 구독 요청이 1번만 와야 함
        }

        should("연결이 끊기고 구독하면 다시 구독 요청한다") {
            val data = InquireLivePrice.InquireLivePriceData(testStock)

            // when
            val instance = InquireLivePrice(api)
            launch { instance.register(data) {} }
            (api.webSocketEventFlow.first { it is KISApiClient.Event.OnSend } as KISApiClient.Event.OnSend).message.let {
                it
                    .let { frame -> json.decodeFromString<LiveCallBody>(frame) }
                    .let { body ->
                        body.header.trType shouldBe "1" // subscribe
                    }
            }

            delay(100)

            api.closeWebsocket()

            delay(100)

            launch { instance.register(data) {} }

            // then
            (api.webSocketEventFlow.first { it is KISApiClient.Event.OnSend } as KISApiClient.Event.OnSend).message.let {
                it
                    .let { frame -> json.decodeFromString<LiveCallBody>(frame) }
                    .let { body ->
                        body.header.trType shouldBe "1" // subscribe
                    }
            }
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

            // when
            instance.register(data, wait = true) {
                // then
                throw Exception("Should not be called")
            }
            instance.unregister(data)

            val anotherInstance = InquireOverseasLivePrice(api)
            anotherInstance.register(data, wait = true) {
                // then
                it.ticker shouldBe "FAKE"
            }

            // fake 데이터를 보냄
            (api.webSocketIncoming!! as MutableSharedFlow<String>).emit("0|HDFSCNT0|${data.tradeKey(api)}^FAKE")

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
                api.webSocketEventFlow.first { it is KISApiClient.Event.OnSend }
            } shouldBe null

            // 모든 인스턴스가 연결 해제되면 서버에 unsubscribe 요청을 보냄
            launch { instance.unregister(data) }
            api.webSocketEventFlow.filterIsInstance<KISApiClient.Event.OnSend>().first().message
                .also { s -> (api.webSocketIncoming!! as MutableSharedFlow<String>).emit(s) }
                .let { frame -> json.decodeFromString<LiveCallBody>(frame) }
                .let { body ->
                    // then
                    body.header.trType shouldBe "2" // unsubscribe
                }

            api.closeWebsocket()
        }

        should("여러 인스턴스가 동시에 구독해도 1번만 구독 요청한다") {
            // given - instance
            val mockOutgoing = attachMockWebsocket()
            val data = InquireOverseasLivePrice.InquireOverseasLivePriceData(testOverseasStock, testOverseasMarket)

            // when
            val instances = List(10) { InquireOverseasLivePrice(api) }
            instances.forEachIndexed { i, it ->
                it.register(data) {}
                if (i == 0) (api.webSocketIncoming!! as MutableSharedFlow<String>).emit(
                    createFakeSubscribeDone(
                        "HDFSCNT0", data.tradeKey(
                            api
                        )
                    )
                ) // fake subscribe done
            }

            // then
            mockOutgoing.receive().let {
                it
                    .also { s -> (api.webSocketIncoming!! as MutableSharedFlow<String>).emit(s) }
                    .let { frame -> json.decodeFromString<LiveCallBody>(frame) }
                    .let { body ->
                        body.header.trType shouldBe "1" // subscribe
                    }
            }
            mockOutgoing.tryReceive().isSuccess shouldBe false // 구독 요청이 1번만 와야 함
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
            val data = InquireLiveAskPrice.InquireLiveAskPriceData()

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
        should("연결이 끊기면 다시 연결한다") {
            launch {
                api.buildWebsocket().apply {
                    close(CloseReason(CloseReason.Codes.GOING_AWAY, "test"))
                }
            }

            repeat(2) {
                api.webSocketEventFlow.first { it is KISApiClient.Event.OnClose || it is KISApiClient.Event.OnOpen }
            }
        }

        should("구독 목록을 다시 전송한다") {
            val instance = InquireLivePrice(api)
            val data = InquireLivePrice.InquireLivePriceData(testStock)

            launch { instance.register(data) {} }
            api.webSocketEventFlow.first { it is KISApiClient.Event.OnSend }

            delay(1000)

            launch { api.reconnectWebsocket() }

            val body = api.webSocketEventFlow.first { it is KISApiClient.Event.OnSend } as KISApiClient.Event.OnSend

            body.message.let {
                it
                    .let { frame -> json.decodeFromString<LiveCallBody>(frame) }
                    .let { body ->
                        body.header.trType shouldBe "1" // subscribe
                    }
            }
        }
    }
})