package io.github.devngho.kisopenapi.requests.util

@Suppress("spellCheckingInspection", "unused")
/**
 * [FAQ](https://apiportal.koreainvestment.com/community/10000000-0000-0011-0000-000000000002)
 * 페이지의 에러코드 정리를 참고했습니다.
 */
enum class RequestCode(val code: String) {
    Unknown("Unknown"),

    /**
     * 모의투자에서는 사용할 수 없는 API를 호출했습니다.
     */
    DemoUnavailable("DemoUnavailable"),

    /**
     * 유효하지 않은 주문입니다.
     * 가격이 0이고 지정가 주문인 경우나, 지원하지 않는 주문 유형인 경우에 발생합니다.
     * @see [io.github.devngho.kisopenapi.requests.overseas.order.OrderOverseasBuy]
     */
    InvalidOrder("InvalidOrder"),

    /**
     * 일시적 오류가 발생했습니다.
     */

    TemporarilyException("EGW00001"),

    /**
     * 서버 에러가 발생했습니다.
     */
    ServerException("EGW00002"),

    /**
     * 접근이 거부되었습니다.
     */
    AccessDenied("EGW00003"),

    /**
     * 권한을 부여받지 않은 고객입니다.
     */
    NoPermission("EGW00004"),

    /**
     * 유효하지 않은 요청입니다.
     */
    InvalidRequest("EGW00101"),

    /**
     * AppKey는 필수입니다.
     */
    AppKeyRequired("EGW00102"),

    /**
     * 유효하지 않은 AppKey입니다.
     */
    InvalidAppKey("EGW00103"),

    /**
     * AppSecret은 필수입니다.
     */
    AppSecretRequired("EGW00104"),

    /**
     * 유효하지 않은 AppSecret입니다.
     */
    InvalidAppSecret("EGW00105"),

    /**
     * redirect_uri는 필수입니다.
     */
    RedirectUriRequired("EGW00106"),

    /**
     * 유효하지 않은 redirect_uri입니다.
     */
    InvalidRedirectUri("EGW00107"),

    /**
     * 유효하지 않은 서비스구분(service)입니다.
     */
    InvalidService("EGW00108"),

    /**
     * scope는 필수입니다.
     */
    ScopeRequired("EGW00109"),

    /**
     * 유효하지 않은 scope 입니다.
     */
    InvalidScope("EGW00110"),

    /**
     * 유효하지 않은 state 입니다.
     */
    InvalidState("EGW00111"),

    /**
     * 유효하지 않은 grant 입니다.
     */
    InvalidGrant("EGW00112"),

    /**
     * 응답구분(response_type)은 필수입니다.
     */
    ResponseTypeRequired("EGW00113"),

    /**
     * 지원하지 않는 응답구분(response_type)입니다.
     */
    InvalidResponseType("EGW00114"),

    /**
     * 권한부여 타입(grant_type)은 필수입니다.
     */
    GrantTypeRequired("EGW00115"),

    /**
     * 지원하지 않는 권한부여 타입(grant_type)입니다.
     */
    InvalidGrantType("EGW00116"),

    /**
     * 지원하지 않는 토큰 타입(token_type)입니다.
     */
    InvalidTokenType("EGW00117"),

    /**
     * 유효하지 않은 code 입니다.
     */
    InvalidCode("EGW00118"),

    /**
     * code를 찾을 수 없습니다.
     */
    CodeNotFound("EGW00119"),

    /**
     * 기간이 만료된 code 입니다.
     */
    CodeExpired("EGW00120"),

    /**
     * 유효하지 않은 token 입니다.
     */
    InvalidToken("EGW00121"),

    /**
     * token을 찾을 수 없습니다.
     */
    TokenNotFound("EGW00122"),

    /**
     * 기간이 만료된 token 입니다.
     */
    TokenExpired("EGW00123"),

    /**
     * 유효하지 않은 session_key 입니다.
     */
    InvalidSessionKey("EGW00124"),

    /**
     * session_key를 찾을 수 없습니다.
     */
    SessionKeyNotFound("EGW00125"),

    /**
     * 기간이 만료된 session_key 입니다.
     */
    SessionKeyExpired("EGW00126"),

    /**
     * 제휴사번호(corpno)는 필수입니다.
     */
    CorpNoRequired("EGW00127"),

    /**
     * 계좌번호(acctno)는 필수입니다.
     */
    AccountNoRequired("EGW00128"),

    /**
     * HTS_ID는 필수입니다.
     */
    HtsIdRequired("EGW00129"),

    /**
     * 유효하지 않은 유저(user)입니다.
     */
    InvalidUser("EGW00130"),

    /**
     * 유효하지 않은 hashkey입니다.
     */
    InvalidHashKey("EGW00131"),

    /**
     * Content-Type이 유효하지 않습니다.
     */
    InvalidContentType("EGW00132"),

    /**
     * 초당 거래건수를 초과하였습니다.
     */
    ExceedTransactionPerSecond("EGW00201"),

    /**
     * GW라우팅 중 오류가 발생했습니다.
     */
    GWRoutingError("EGW00202"),

    /**
     * OPS라우팅 중 오류가 발생했습니다.
     */
    OPSRoutingError("EGW00203"),

    /**
     * Internal Gateway 인스턴스를 잘못 입력했습니다.
     */
    InvalidInternalGatewayInstance("EGW00204"),

    /**
     * credentials_type이 유효하지 않습니다.(Bearer)
     */
    InvalidCredentialsType("EGW00205"),

    /**
     * API 사용 권한이 없습니다.
     */
    NoPermissionToUseAPI("EGW00206"),

    /**
     * IP 주소가 없거나 유효하지 않습니다.
     */
    InvalidIPAddress("EGW00207"),

    /**
     * 고객유형(custtype)이 유효하지 않습니다.
     */
    InvalidCustomerType("EGW00208"),

    /**
     * 일련번호(seq_no)가 유효하지 않습니다.
     */
    InvalidSequenceNumber("EGW00209"),

    /**
     * 법인고객의 경우 모의투자를 이용할 수 없습니다.
     */
    NoMockTradingForCorporation("EGW00210"),

    /**
     * 고객명(personalname)은 필수 입니다.
     */
    PersonalNameRequired("EGW00211"),

    /**
     * 휴대전화번호(personalphone)는 필수 입니다.
     */
    PersonalPhoneRequired("EGW00212"),

    /**
     * 제휴사명(corpname)은 필수 입니다.
     */
    CorpNameRequired("EGW00213"),

    /**
     * Gateway 라우팅 오류가 발생했습니다.
     */
    GatewayRoutingError("EGW00300"),

    /**
     * 연결 시간이 초과되었습니다.직전 거래를 반드시 확인하세요.
     */
    ConnectionTimeout("EGW00301"),

    /**
     * 거래시간이 초과되었습니다.직전 거래를 반드시 확인하세요.
     */
    TransactionTimeout("EGW00302"),

    /**
     * 법인고객에게 허용되지 않은 IP접근입니다.
     */
    InvalidIPAccessForCorporation("EGW00303"),

    /**
     * 고객식별키(법인 personalSeckey, 개인 appSecret)가 유효하지 않습니다.
     */
    InvalidPersonalSecKey("EGW00304"),

    /**
     * 모의투자 TR 이 아닙니다.
     */
    NotDemoTradingTR("EGW2004"),

    /**
     * SUBSCRIBE SUCCESS
     */
    SubscribeSuccess("OPSP0000"),

    /**
     * UNSUBSCRIBE SUCCESS
     */
    UnsubscribeSuccess("OPSP0001"),

    /**
     * ALREADY IN SUBSCRIBE
     */
    AlreadyInSubscribe("OPSP0002"),

    /**
     * UNSUBSCRIBE ERROR(not found!)
     */
    UnsubscribeErrorNotFound("OPSP0003"),

    /**
     * SUBSCRIBE INTERNAL ERROR
     */
    SubscribeInternalError("OPSP0007"),

    /**
     * MAX SUBSCRIBE OVER
     */
    MaxSubscribeOver("OPSP0008"),

    /**
     * SUBSCRIBE ERROR : mci send failed
     */
    SubscribeErrorMCISendFailed("OPSP0009"),

    /**
     * SUBSCRIBE WARNNING : invalid appkey
     */
    SubscribeWarningInvalidAppKey("OPSP0010"),

    /**
     * JSON PARSING ERROR : invalid tr_key
     */
    JsonParsingErrorInvalidTrKey("OPSP8993"),

    /**
     * JSON PARSING ERROR : personalseckey not found
     */
    JsonParsingErrorPersonalSecKeyNotFound("OPSP8994"),

    /**
     * JSON PARSING ERROR : appsecret not found
     */
    JsonParsingErrorAppSecretNotFound("OPSP8995"),

    /**
     * JSON PARSING ERROR : ALREADY IN USE appkey
     */
    JsonParsingErrorAlreadyInUseAppKey("OPSP8996"),

    /**
     * JSON PARSING ERROR : invalid tr_type
     */
    JsonParsingErrorInvalidTrType("OPSP8997"),

    /**
     * JSON PARSING ERROR : invalid custtype
     */
    JsonParsingErrorInvalidCustomerType("OPSP8998"),

    /**
     * JSON PARSING ERROR : resource not available (ALLOC_CALL_PARAM)
     */
    JsonParsingErrorResourceNotAvailable("OPSP8999"),

    /**
     * JSON PARSING ERROR : invalid tr_key
     */
    JsonParsingErrorInvalidTrKey2("OPSP9990"),

    /**
     * JSON PARSING ERROR : input not found
     */
    JsonParsingErrorInputNotFound("OPSP9991"),

    /**
     * JSON PARSING ERROR : body not found
     */
    JsonParsingErrorBodyNotFound("OPSP9992"),

    /**
     * JSON PARSING ERROR : internal error
     */
    JsonParsingErrorInternalError("OPSP9993"),

    /**
     * JSON PARSING ERROR : INVALID appkey
     */
    JsonParsingErrorInvalidAppKey("OPSP9994"),

    /**
     * JSON PARSING ERROR : resource not available
     */
    JsonParsingErrorResourceNotAvailable2("OPSP9995"),

    /**
     * JSON PARSING ERROR : appkey
     */
    JsonParsingErrorAppKey("OPSP9996"),

    /**
     * JSON PARSING ERROR : custtype not found
     */
    JsonParsingErrorCustomerTypeNotFound("OPSP9997"),

    /**
     * JSON PARSING ERROR : header not found
     */
    JsonParsingErrorHeaderNotFound("OPSP9998"),

    /**
     * JSON PARSING ERROR : invalid json format
     */
    JsonParsingErrorInvalidJsonFormat("OPSP9999"),

    /**
     * SUBSCRIBE ERROR : invalid tr_id
     */
    SubscribeErrorInvalidTrId("OPSP8991"),

    /**
     * SUBSCRIBE ERROR : invalid tr_key
     */
    SubscribeErrorInvalidTrKey("OPSP8992"),

    /**
     * 호출 전처리 오류 입니다.
     */
    PreprocessError("OPSQ0001"),

    /**
     * 없는 서비스 코드 입니다.
     */
    InvalidServiceCode("OPSQ0002"),

    /**
     * 호출 오류 입니다.
     */
    RequestError("OPSQ0003"),

    /**
     * 호출 후처리 오류 입니다.
     */
    PostprocessError("OPSQ0004"),

    /**
     * 호출 후처리 오류 입니다.
     */
    PostprocessError2("OPSQ0005"),

    /**
     * 호출 후처리 오류 입니다.
     */
    PostprocessError3("OPSQ0006"),

    /**
     * 호출 후처리(헤더설정) 오류 입니다.
     */
    PostprocessErrorHeader("OPSQ0007"),

    /**
     * 호출 후처리(MCI전송) 오류 입니다.
     */
    PostprocessErrorMCISend("OPSQ0008"),

    /**
     * 호출 후처리(MCI수신) 오류 입니다.
     */
    PostprocessErrorMCIReceive("OPSQ0009"),

    /**
     * 호출 결과처리(리소스 부족) 오류 입니다.
     */
    PostprocessErrorResource("OPSQ0010"),

    /**
     * 호출 결과처리(리소스 부족) 오류 입니다.
     */
    PostprocessErrorResource2("OPSQ0011"),

    /**
     * 세션 연결 오류..
     */
    SessionConnectError("OPSQ1002"),

    /**
     * ERROR : INPUT INVALID_CHECK_ACNO
     */
    ErrorInputInvalidCheckAccountNo("OPSQ2000"),

    /**
     * ERROR : INPUT INVALID_CHECK_MRKT_DIV_CODE
     */
    ErrorInputInvalidCheckMarketDivisionCode("OPSQ2001"),

    /**
     * ERROR : INPUT INVALID_CHECK_FIELD_LENGTH
     */
    ErrorInputInvalidCheckFieldLength("OPSQ2002"),

    /**
     * ERROR : SET_MCI_SEND_DATA
     */
    ErrorSetMCISendData("OPSQ2003"),

    /**
     * ERROR : RESPONSE_ADDITEMTOOBJECT
     */
    ErrorResponseAddItemToObject("OPSQ3001"),

    /**
     * ERROR : GET_CALL_PARAM_MCI_SEND_DATA_LEN
     */
    ErrorGetCallParamMCISendDataLength("OPSQ3002"),

    /**
     * ERROR: OUT_STRING_ARRAY ALLOC FAILED
     */
    ErrorOutStringArrayAllocFailed("OPSQ3004"),

    /**
     * JSON PARSING ERROR : body not found
     */
    JsonParsingErrorBodyNotFound2("OPSQ9995"),

    /**
     * JSON PARSING ERROR : header not found
     */
    JsonParsingErrorHeaderNotFound2("OPSQ9996"),

    /**
     * JSON PARSING ERROR : invalid json format
     */
    JsonParsingErrorInvalidJsonFormat2("OPSQ9997"),

    /**
     * JSON PARSING ERROR : seq_no not found
     */
    JsonParsingErrorSequenceNumberNotFound("OPSQ9998"),

    /**
     * JSON PARSING ERROR : tr_id not found
     */
    JsonParsingErrorTrIdNotFound("OPSQ9999");

    companion object {
        private val map = entries.associateBy(RequestCode::code)

        fun fromCode(code: String) = map[code] ?: Unknown
    }
}