package io.github.devngho.kisopenapi.requests.util

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder



@OptIn(ExperimentalSerializationApi::class)
@Serializable(with = StockState.StockStateSerializer::class)
enum class StockState(val num: Int) {
    /**
     * 그 외
     */
    Other(0),
    /**
     * 관리종목
     */
    Manage(51),
    /**
     * 투자의견
     */
    Opinion(52),
    /**
     * 투자경고
     */
    Warning(53),
    /**
     * 투자주의
     */
    Alert(54),
    /**
     * 신용가능
     */
    CreditAvailable(55),
    /**
     * 증거금 100%
     */
    Margin100(57),
    /**
     * 거래정지
     */
    TradeStop(58),
    /**
     * 단기과열
     */
    ShortOverheat(59);

    @ExperimentalSerializationApi
    @Serializer(forClass = StockState::class)
    object StockStateSerializer : DeserializationStrategy<StockState> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("StockState", PrimitiveKind.INT)

        override fun deserialize(decoder: Decoder): StockState {
            val d = decoder.decodeInt()
            return StockState.values().first { it.num == d }
        }

        override fun serialize(encoder: Encoder, value: StockState) {
            encoder.encodeString(if (value.num < 10) "0${value.num}" else "${value.num}")
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable(with = SignYesterday.SignYesterdaySerializer::class)
enum class SignYesterday(val value: Int) {
    Max(1),
    Up(2),
    Complement(3),
    Min(4),
    Down(5);

    @ExperimentalSerializationApi
    @Serializer(forClass = SignYesterday::class)
    object SignYesterdaySerializer : DeserializationStrategy<SignYesterday> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("SignYesterday", PrimitiveKind.INT)

        override fun deserialize(decoder: Decoder): SignYesterday {
            val d = decoder.decodeInt()
            return SignYesterday.values().first { it.value == d }
        }

        override fun serialize(encoder: Encoder, value: SignYesterday) {
            encoder.encodeString("${value.value}")
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable(with = MarketWarnCode.MarketWarnCodeSerializer::class)
enum class MarketWarnCode(val value: Int) {
    /**
     * 없음
     */
    None(0),

    /**
     * 주의
     */
    Alert(1),

    /**
     * 경고
     */
    Warning(2),

    /**
     * 위험
     */
    Danger(3);

    @ExperimentalSerializationApi
    @Serializer(forClass = MarketWarnCode::class)
    object MarketWarnCodeSerializer : DeserializationStrategy<MarketWarnCode> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("MarketWarnCode", PrimitiveKind.INT)

        override fun deserialize(decoder: Decoder): MarketWarnCode {
            val d = decoder.decodeInt()
            return MarketWarnCode.values().first { it.value == d }
        }

        override fun serialize(encoder: Encoder, value: MarketWarnCode) {
            encoder.encodeString("${value.value}")
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable(with = LockCode.LockCodeSerializer::class)
enum class LockCode(val num: Int) {
    Other(0),
    PermissionLock(1),
    AllocationLock(2),
    DistributionLock(3),
    PermissionDistributionLock(4),
    QuarterAllocationLock(5),
    PermissionCenterAllocationLock(6),
    PermissionQuarterAllocationLock(7);

    @ExperimentalSerializationApi
    @Serializer(forClass = LockCode::class)
    object LockCodeSerializer : DeserializationStrategy<LockCode> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LockCode", PrimitiveKind.INT)

        override fun deserialize(decoder: Decoder): LockCode {
            val d = decoder.decodeInt()
            return LockCode.values().first { it.num == d }
        }

        override fun serialize(encoder: Encoder, value: LockCode) {
            encoder.encodeString(if (value.num < 10) "0${value.num}" else "${value.num}")
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable(with = PeriodDivisionCode.PeriodDivisionCodeSerializer::class)
enum class PeriodDivisionCode(val num: String) {
    Days30("D"),
    Weeks30("W"),
    Months30("M");

    @ExperimentalSerializationApi
    @Serializer(forClass = PeriodDivisionCode::class)
    object PeriodDivisionCodeSerializer : DeserializationStrategy<PeriodDivisionCode> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("PeriodDivisionCode", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): PeriodDivisionCode {
            val d = decoder.decodeString()
            return PeriodDivisionCode.values().first { it.num == d }
        }

        override fun serialize(encoder: Encoder, value: PeriodDivisionCode) {
            encoder.encodeString(value.num)
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable(with = ProductTypeCode.ProductTypeSerializer::class)
enum class ProductTypeCode(val num: String) {
    Stock("300"),
    FutureOption("301"),
    Bond("302"),
    Nasdaq("512"),
    NewYork("513"),
    Amex("529"),
    Japan("515"),
    HongKong("501"),
    HongKongCNY("543"),
    HongKongUSD("558"),
    VietnamHanoi("507"),
    VietnamHoChiMinh("508"),
    ChinaSanghaeA("551"),
    ChinaSimCheonA("552")
    ;

    @ExperimentalSerializationApi
    @Serializer(forClass = ProductTypeCode::class)
    object ProductTypeSerializer : DeserializationStrategy<ProductTypeCode> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ProductType", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): ProductTypeCode {
            val d = decoder.decodeString()
            return ProductTypeCode.values().first { it.num == d }
        }

        override fun serialize(encoder: Encoder, value: ProductTypeCode) {
            encoder.encodeString(value.num)
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable(with = OrderTypeCode.OrderTypeSerializer::class)
enum class OrderTypeCode(val num: String) {
    /**
     * 지정가
     */
    SelectPrice("00"),
    /**
    * 시장가
    */
    MarketPrice("01"),
    /**
    * 조건부 지정가
    */
    SelectPriceWithContition("02"),
    /**
    * 최유리 지정가
    */
    SelectPriceBest("03"),
    /**
    * 최우선 지정가
    */
    SelectPriceFirst("04"),
    /**
    * 장전 시간외
    */
    OutTimeBeforeMarket("05"),
    /**
    * 장후 시간외
    */
    OutTimeAfterMarket("06"),
    /**
    * 시간외 단일가
    */
    OutTimeOnlyPrice("07"),
    /**
    * 자기주식
    */
    SelfStock("08"),
    /**
    * 자기주식 S-Option
    */
    SelfStockSOption("09"),
    /**
    * 자기주식 금전신탁
    */
    SelfStockMoneyPrice("10"),
    /**
    * IOC 지정가
    */
    SelectPriceIOC("11"),
    /**
    * FOK 지정가
    */
    SelectPriceFOK("12"),
    /**
    * IOC 시장가
    */
    MarketPriceIOC("13"),
    /**
    * FOK 시장가
    */
    MarketPriceFOK("14"),
    /**
    * IOC 최유리
    */
    BestIOC("15"),
    /**
    * FOK 최유리
    */
    BestFOK("16")
    ;

    @ExperimentalSerializationApi
    @Serializer(forClass = OrderTypeCode::class)
    object OrderTypeSerializer : DeserializationStrategy<OrderTypeCode> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("OrderType", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): OrderTypeCode {
            val d = decoder.decodeString()
            return OrderTypeCode.values().first { it.num == d }
        }

        override fun serialize(encoder: Encoder, value: OrderTypeCode) {
            encoder.encodeString(value.num)
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable(with = ConsumerTypeCode.ConsumerTypeSerializer::class)
enum class ConsumerTypeCode(val num: String) {
    Corporation("B"),
    Personal("P")
    ;


    @ExperimentalSerializationApi
    @Serializer(forClass = ConsumerTypeCode::class)
    object ConsumerTypeSerializer : DeserializationStrategy<ConsumerTypeCode> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ConsumerType", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): ConsumerTypeCode {
            val d = decoder.decodeString()
            return ConsumerTypeCode.values().first { it.num == d }
        }

        override fun serialize(encoder: Encoder, value: ConsumerTypeCode) {
            encoder.encodeString(value.num)
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable(with = InquireDivisionCode.InquireDivisionSerializer::class)
enum class InquireDivisionCode(val num: String) {
    ByLoanDays("01"),
    ByStock("02")
    ;


    @ExperimentalSerializationApi
    @Serializer(forClass = InquireDivisionCode::class)
    object InquireDivisionSerializer : DeserializationStrategy<InquireDivisionCode> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("InquireDivision", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): InquireDivisionCode {
            val d = decoder.decodeString()
            return InquireDivisionCode.values().first { it.num == d }
        }

        override fun serialize(encoder: Encoder, value: InquireDivisionCode) {
            encoder.encodeString(value.num)
        }
    }
}
