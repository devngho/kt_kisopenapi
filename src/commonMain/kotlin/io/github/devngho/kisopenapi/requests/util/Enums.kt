package io.github.devngho.kisopenapi.requests.util

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


@OptIn(ExperimentalSerializationApi::class)
@Serializable(with = StockState.StockStateSerializer::class)
@Suppress("unused")
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
    object StockStateSerializer : KSerializer<StockState?> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("StockState?", PrimitiveKind.INT)

        override fun deserialize(decoder: Decoder): StockState? {
            val d = decoder.decodeInt()
            return entries.firstOrNull { it.num == d }
        }

        override fun serialize(encoder: Encoder, value: StockState?) {
            encoder.encodeString("${value?.num ?: 0}".padStart(2, '0'))
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable(with = SignPrice.SignPriceSerializer::class)
@Suppress("unused")
enum class SignPrice(val value: Int) {
    Max(1),
    Up(2),
    Complement(3),
    Min(4),
    Down(5);

    @ExperimentalSerializationApi
    object SignPriceSerializer : KSerializer<SignPrice?> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("SignPrice?", PrimitiveKind.INT)

        override fun deserialize(decoder: Decoder): SignPrice? {
            val d = decoder.decodeInt()
            return fromCode(d)
        }

        override fun serialize(encoder: Encoder, value: SignPrice?) {
            encoder.encodeString("${value?.value ?: 0}")
        }
    }

    companion object {
        private val codeMap = entries.associateBy { it.value }

        fun fromCode(code: Int) = codeMap[code]
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable(with = MarketWarnCode.MarketWarnCodeSerializer::class)
@Suppress("unused")
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
    object MarketWarnCodeSerializer : KSerializer<MarketWarnCode?> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("MarketWarnCode?", PrimitiveKind.INT)

        override fun deserialize(decoder: Decoder): MarketWarnCode? {
            val d = decoder.decodeInt()
            return entries.firstOrNull { it.value == d }
        }

        override fun serialize(encoder: Encoder, value: MarketWarnCode?) {
            encoder.encodeString("${value?.value ?: 0}")
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable(with = LockCode.LockCodeSerializer::class)
@Suppress("unused")
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
    object LockCodeSerializer : KSerializer<LockCode?> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LockCode?", PrimitiveKind.INT)

        override fun deserialize(decoder: Decoder): LockCode? {
            val d = decoder.decodeInt()
            return entries.firstOrNull { it.num == d }
        }

        override fun serialize(encoder: Encoder, value: LockCode?) {
            encoder.encodeString("${value?.num ?: 0}".padStart(2, '0'))
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable(with = ProductTypeCode.ProductTypeSerializer::class)
@Suppress("unused", "SpellCheckingInspection")
enum class ProductTypeCode(val num: String) {
    Stock("300"),
    FutureOption("301"),
    Bond("302"),
    ELS("306"),
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
    object ProductTypeSerializer : KSerializer<ProductTypeCode> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ProductType", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): ProductTypeCode {
            val d = decoder.decodeString()
            return entries.first { it.num == d }
        }

        override fun serialize(encoder: Encoder, value: ProductTypeCode) {
            encoder.encodeString(value.num)
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable(with = OrderTypeCode.OrderTypeSerializer::class)
@Suppress("unused")
enum class OrderTypeCode(val num: String, val isPriceSelectable: Boolean = true) {
    /**
     * 지정가
     */
    SelectPrice("00"),

    /**
     * 시장가
     */
    MarketPrice("01", false),

    /**
     * 조건부 지정가
     */
    SelectPriceWithCondition("02"),

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
    OutTimeBeforeMarket("05", false),

    /**
     * 장후 시간외
     */
    OutTimeAfterMarket("06", false),

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
    MarketPriceIOC("13", false),

    /**
     * FOK 시장가
     */
    MarketPriceFOK("14", false),

    /**
     * IOC 최유리
     */
    BestIOC("15"),

    /**
     * FOK 최유리
     */
    BestFOK("16"),

    /**
     * 장개시지정가(미국 매매 전용)
     */
    USALimitOnOpen("32"),

    /**
     * 장마감지정가(미국 매매 전용)
     */
    USALimitOnClose("34"),

    /**
     * 장개시시장가(미국 매도 전용)
     */
    USAMarketOnOpen("31", false),

    /**
     * 장마감시장가(미국 매도 전용)
     */
    USAMarketOnClose("33", false),

    USAMarketTWAP("35"),
    USAMarketVWAP("36"),


    /**
     * 단주지정가(홍콩 매도 전용)
     */
    HONGKONGSingleSelectPrice("50")
    ;

    @ExperimentalSerializationApi
    object OrderTypeSerializer : KSerializer<OrderTypeCode> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("OrderType", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): OrderTypeCode {
            val d = decoder.decodeString()
            return entries.first { it.num == d }
        }

        override fun serialize(encoder: Encoder, value: OrderTypeCode) {
            encoder.encodeString(value.num)
        }
    }

    companion object {
        private val codeMap = OrderTypeCode.entries.associateBy { it.num }

        fun fromCode(code: String) = codeMap[code]
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable(with = ConsumerTypeCode.ConsumerTypeSerializer::class)
@Suppress("unused")
enum class ConsumerTypeCode(val num: String) {
    Corporation("B"),
    Personal("P")
    ;


    @ExperimentalSerializationApi
    object ConsumerTypeSerializer : KSerializer<ConsumerTypeCode> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ConsumerType", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): ConsumerTypeCode {
            val d = decoder.decodeString()
            return entries.first { it.num == d }
        }

        override fun serialize(encoder: Encoder, value: ConsumerTypeCode) {
            encoder.encodeString(value.num)
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable(with = InquireDivisionCode.InquireDivisionSerializer::class)
@Suppress("unused")
enum class InquireDivisionCode(val num: String) {
    ByLoanDays("01"),
    ByStock("02")
    ;


    @ExperimentalSerializationApi
    object InquireDivisionSerializer : KSerializer<InquireDivisionCode> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("InquireDivision", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): InquireDivisionCode {
            val d = decoder.decodeString()
            return entries.first { it.num == d }
        }

        override fun serialize(encoder: Encoder, value: InquireDivisionCode) {
            encoder.encodeString(value.num)
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable(with = WeekdayCode.WeekdayCodeSerializer::class)
@Suppress("unused")
enum class WeekdayCode(val num: String) {
    Sat("07"),
    Sun("01"),
    Mon("02"),
    Tue("03"),
    Wed("04"),
    Thu("05"),
    Fri("06")
    ;


    @ExperimentalSerializationApi
    object WeekdayCodeSerializer : KSerializer<WeekdayCode> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Weekday", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): WeekdayCode {
            val d = decoder.decodeString()
            return entries.first { it.num == d }
        }

        override fun serialize(encoder: Encoder, value: WeekdayCode) {
            encoder.encodeString(value.num)
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable(with = HourCode.HourCodeSerializer::class)
@Suppress("unused")
enum class HourCode(val num: String) {
    InMarket("0"),
    AfterMarket("A"),
    BeforeMarket("B"),
    VIActivated("C"),
    AfterHourSinglePrice("D")
    ;


    @ExperimentalSerializationApi
    object HourCodeSerializer : KSerializer<HourCode> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Hour", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): HourCode {
            val d = decoder.decodeString()
            return entries.first { it.num == d }
        }

        override fun serialize(encoder: Encoder, value: HourCode) {
            encoder.encodeString(value.num)
        }
    }

    companion object {
        private val codeMap = HourCode.entries.associateBy { it.num }

        fun fromCode(code: String) = codeMap[code]
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable(with = MarketForOrder.MarketForOrderSerializer::class)
enum class MarketForOrder(val code: String) {
    KRX("KRX"),
    NEXTRADE("NXT"),
    SOR("SOR")
    ;

    @ExperimentalSerializationApi
    object MarketForOrderSerializer : KSerializer<MarketForOrder> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("MarketForOrder", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): MarketForOrder {
            val d = decoder.decodeString()
            return MarketForOrder.valueOf(d.uppercase())
        }

        override fun serialize(encoder: Encoder, value: MarketForOrder) {
            encoder.encodeString(value.code)
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable(with = Market.MarketSerializer::class)
enum class Market(val code: String) {
    KRX("KRX"),
    NEXTRADE("NXT"),
    ;

    @ExperimentalSerializationApi
    object MarketSerializer : KSerializer<Market> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Market", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Market {
            val d = decoder.decodeString()
            return Market.valueOf(d.uppercase())
        }

        override fun serialize(encoder: Encoder, value: Market) {
            encoder.encodeString(value.code)
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable(with = MarketWithUnified.MarketWithUnifiedSerializer::class)
enum class MarketWithUnified(val code: String) {
    KRX("ST"),
    NEXTRADE("NX"),

    /** 통합 **/
    UNIFIED("UN"),
    ;

    @ExperimentalSerializationApi
    object MarketWithUnifiedSerializer : KSerializer<MarketWithUnified> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("MarketWithUnified", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): MarketWithUnified {
            val d = decoder.decodeString()
            return MarketWithUnified.valueOf(d.uppercase())
        }

        override fun serialize(encoder: Encoder, value: MarketWithUnified) {
            encoder.encodeString(value.code)
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable(with = MarketWithSOR.MarketWithSORSerializer::class)
enum class MarketWithSOR(val code: Int) {
    KRX(1),
    NEXTRADE(2),
    SOR_KRX(3),
    SOR_NEXTRADE(4),
    ;

    @ExperimentalSerializationApi
    object MarketWithSORSerializer : KSerializer<MarketWithSOR> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("MarketWithSOR", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): MarketWithSOR {
            val d = decoder.decodeInt()
            return fromCode(d)!!
        }

        override fun serialize(encoder: Encoder, value: MarketWithSOR) {
            encoder.encodeString(value.code.toString())
        }
    }

    companion object {
        private val codeMap = entries.associateBy { it.code }

        fun fromCode(code: Int) = codeMap[code]
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable(with = OverseasMarket.OverseasMarketSerializer::class)
enum class OverseasMarket(val code: String) {
    HONGKONG("HKS"),
    NEWYORK("NYS"),
    NASDAQ("NAS"),
    AMEX("AMS"),
    TOKYO("TSE"),
    SHANGHAI("SHS"),
    SHENZHEN("SZS"),
    SHANGHAI_INDEX("SHI"),
    SHENZHEN_INDEX("SZI"),
    HOCHIMINH("HSX"),
    HANOI("HNX"),
    NEWYORK_DAY("BAY"),
    NASDAQ_DAY("BAQ"),
    AMEX_DAY("BAA"),
    HKS("HKS"),
    NYS("NYS"),
    NAS("NAS"),
    AMS("AMS"),
    TSE("TSE"),
    SHS("SHS"),
    SZS("SZS"),
    SHI("SHI"),
    SZI("SZI"),
    HSX("HSX"),
    HNX("HNX"),
    BAY("BAY"),
    BAQ("BAQ"),
    BAA("BAA")
    ;

    @ExperimentalSerializationApi
    object OverseasMarketSerializer : KSerializer<OverseasMarket> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("OverseasMarket", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): OverseasMarket {
            val d = decoder.decodeString()
            return OverseasMarket.valueOf(d.uppercase())
        }

        override fun serialize(encoder: Encoder, value: OverseasMarket) {
            encoder.encodeString(value.code)
        }
    }

    companion object {
        val OverseasMarket.fourChar: String
            get() = @Suppress("SpellCheckingInspection") when (this) {
                NASDAQ,
                NAS,
                NASDAQ_DAY,
                BAQ -> "NASD"

                NEWYORK,
                NYS,
                NEWYORK_DAY,
                BAY -> "NYSE"

                AMEX,
                AMS,
                AMEX_DAY,
                BAA -> "AMEX"

                TOKYO,
                TSE -> "TKSE"

                SHANGHAI,
                SHANGHAI_INDEX,
                SHS,
                SHI -> "SHAA"

                HONGKONG,
                HKS -> "SEHK"

                SHENZHEN,
                SHENZHEN_INDEX,
                SZI,
                SZS -> "SZAA"

                HANOI,
                HNX -> "HASE"

                HOCHIMINH,
                HSX -> "VNSE"
            }
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable(with = MarketStatus.MarketStatusSerializer::class)
@Suppress("unused")
enum class MarketStatus(val code: String) {
    OPEN("1"),
    BEFORE_START("2"),
    AFTER_CLOSE("3");

    @ExperimentalSerializationApi
    object MarketStatusSerializer : KSerializer<MarketStatus> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("MarketStatus", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): MarketStatus {
            val d = decoder.decodeString()
            return entries.first { it.code == d }
        }

        override fun serialize(encoder: Encoder, value: MarketStatus) {
            encoder.encodeString(value.code)
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable(with = Currency.CurrencySerializer::class)
@Suppress("unused")
enum class Currency(val code: String) {
    USD("USD"),
    HKD("HKD"),
    CNY("CNY"),
    JPY("JPY"),
    VND("VND");

    @ExperimentalSerializationApi
    object CurrencySerializer : KSerializer<Currency> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Currency", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Currency {
            val d = decoder.decodeString()
            return entries.first { it.code == d }
        }

        override fun serialize(encoder: Encoder, value: Currency) {
            encoder.encodeString(value.code)
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable(with = LoanType.LoanTypeSerializer::class)
@Suppress("unused")
enum class LoanType(val code: String) {
    /** 해당 사항 없음 **/
    None("00"),

    /** 자기융자 일반형 **/
    GeneralSelf("01"),

    /** 자기융자 투자형 **/
    InvestmentSelf("03"),

    /** 유통융자 일반형 **/
    GeneralDistribution("05"),

    /** 유통융자 투자형 **/
    InvestmentDistribution("06"),

    /** 자기대주 **/
    SelfStock("07"),

    /** 유통대주 **/
    DistributionStock("09"),

    /** 주식담보대출 **/
    StockCollateral("11"),

    /** 수익증권담보대출 **/
    IncomeSecuritiesCollateral("12"),

    /** ELS담보대출 **/
    ELS("13"),

    /** 채권담보대출 **/
    BondCollateral("14"),

    /** 해외주식담보대출 **/
    OverseasStockCollateral("15"),

    /** 기업신용공여 **/
    CorporateCredit("16"),

    /** 소액자동담보대출 **/
    SmallAutoCollateral("31"),

    /** 매도담보대출 **/
    SellCollateral("41"),

    /** 환매자금대출 **/
    RedemptionCollateral("42"),

    /** 매입환매자금대출 **/
    BuyRedemptionCollateral("43"),

    /** 대여매도담보대출 **/
    LendSellCollateral("44"),

    /** 대차거래 **/
    Borrowing("81"),

    /** 법인CMA론 **/
    CorporationCMA("82"),

    /** 공모주청약자금대출 **/
    PublicOfferingSubscription("91"),

    /** 매입자금 **/
    Buy("92"),

    /** 미수론서비스 **/
    UnpaidService("93"),

    /** 대여 **/
    Lend("94");

    @ExperimentalSerializationApi
    object LoanTypeSerializer : KSerializer<LoanType> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LoanType", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): LoanType {
            val d = decoder.decodeString()
            return entries.firstOrNull { it.code == d } ?: None
        }

        override fun serialize(encoder: Encoder, value: LoanType) {
            encoder.encodeString(value.code)
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable(with = PeriodDivisionCode.PeriodDivisionCodeSerializer::class)
@Suppress("unused")
enum class PeriodDivisionCode(val num: String) {
    Days("D"),
    Weeks("W"),
    Months("M"),
    Years("Y");

    @ExperimentalSerializationApi
    object PeriodDivisionCodeSerializer : KSerializer<PeriodDivisionCode> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("PeriodDivisionCode", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): PeriodDivisionCode {
            val d = decoder.decodeString()
            return entries.first { it.num == d }
        }

        override fun serialize(encoder: Encoder, value: PeriodDivisionCode) {
            encoder.encodeString(value.num)
        }
    }
}