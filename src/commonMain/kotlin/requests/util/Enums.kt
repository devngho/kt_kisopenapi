package com.github.devngho.kisopenapi.requests.util

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