[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.devngho/kt_kisopenapi/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.devngho/kt_kisopenapi)
# kt_kisopenapi

https://apiportal.koreainvestment.com/about

한국투자증권의 오픈 API 서비스를 쉽게 사용할 수 있는 Kotlin 라이브러리.

## 주의사항
> 이 라이브러리는 **알파 버전**으로, API가 수시로 변경될 수 있습니다.

> 법인 사용이 가능하나, **개인 사용 목적**으로 개발되었으며 법인 사용은 **테스트되지 않았습니다**.

> 개발자([devngho](https://github.com/devngho))와 기여자는 라이브러리의 사용으로 발상한 손해 등에 대한 **책임을 지지 않습니다**.

## 사용하기
**Gradle(Groovy)**
```groovy
implementation 'io.github.devngho:kt_kisopenapi:[VERSION]'
```
**Gradle(Kotlin)**
```kotlin
implementation("io.github.devngho:kt_kisopenapi:[VERSION]")
```
## 기여하기
- 직접 기능을 추가하거나 버그를 수정하고 Pull Request를 보내주세요.
- 또는 기능 추가를 요청하거나 버그를 제보하는 Issue를 남겨주세요.
## 예시
### 권장 방식
```kotlin
// 국내 주식을 조회/거래하는 예시. 자세한 내용은 Wiki를 확인하세요.
val stock = StockDomestic(api, "주식 종목 코드")
stock.updateBy(StockPrice::class)
stock.price

stock.buy(price = 10000, count = 10)

stock.useLiveConfirmPrice {
    print(it)
    this.close()
}
```
```java
// Java에서도 사용할 수 있습니다.
StockDomestic stock = new StockDomestic(api, "주식 종목 코드");
JavaUtil.updateBy(stock, StockPrice.class);
stock.price;
```
### 로우 레벨 API
```kotlin
// 예시
InquirePrice(api).call(InquirePrice.InquirePriceData(""))
```

```java
// 예시
JavaUtil.callWithData(new InquirePrice(api), new InquirePrice.InquirePriceData("", null, ""));
```
- [x] OAuth
  - [x] 접속토큰
      - [x] 발급
      - [x] 폐기
- [x] HashKey
- [ ] 국내주식
    - [ ] 주문
      - [x] 주문
      - [ ] 정정
      - [ ] 예약
      - [x] 잔고 조회
      - [ ] 퇴직연금
    - [ ] 시세
      - [x] 시세
      - [x] 체결
      - [x] 일자별
      - [ ] 호가예상
      - [ ] 투자자
      - [ ] 회원사
      - [ ] ELW
      - [x] 기간별 시세
      - [x] 실시간 시세
      - [x] 거래량순위
- [ ] 국내선물옵션

[//]: # (    - [ ] 주문)

[//]: # (        - [ ] 주문 )

[//]: # (        - [ ] 정정)

[//]: # (        - [ ] 잔고 조회)

[//]: # (    - [ ] 시세)

[//]: # (        - [ ] 시세)

[//]: # (        - [ ] 기간별 시세)
- [ ] 해외주식
  - [ ] 주문
    - [x] 주문
    - [ ] 정정
    - [ ] 예약
    - [x] 잔고 조회
  - [ ] 시세
    - [x] 체결가
    - [ ] 기간별 시세
    - [x] 조건검색
    - [x] 실시간 시세
- [ ] 해외선물옵션

[//]: # (    - [ ] 주문)

[//]: # (        - [ ] 주문)

[//]: # (        - [ ] 정정)

[//]: # (        - [ ] 잔고 조회)

[//]: # (        - [ ] 주문 내역)

[//]: # (    - [ ] 시세)

[//]: # (        - [ ] 상세)

[//]: # (        - [ ] 현재가)
[//]: # (- [ ] 예제 코드)

## 오픈 소스 라이선스
이 라이브러리는 MIT License를 사용합니다. 자세한 내용은 LICENSE 파일을 참조하세요.

다음은 사용하는 라이브러리의 라이선스입니다.
### Kotlin
- [Jetbrains/Kotlin](https://github.com/JetBrains/kotlin) - [Apache License Version 2.0](https://github.com/JetBrains/kotlin/blob/master/license/LICENSE.txt)
- [Kotlin/kotlinx.coroutines](https://github.com/Kotlin/kotlinx.coroutines) - [Apache License Version 2.0](https://github.com/Kotlin/kotlinx.coroutines/blob/master/LICENSE.txt)
- [ktorio/ktor](https://github.com/ktorio/ktor) - [Apache License Version 2.0](https://github.com/ktorio/ktor/blob/main/LICENSE)
### Other
- [korge/krypto](https://github.com/korlibs/korge/tree/main/krypto) : WebSocket AES 처리를 위한 라이브러리 - [MIT License](https://github.com/korlibs/korge/blob/main/krypto/LICENSE)
- [ionspin/kotlin-multiplatform-bignum](http://github.com/ionspin/kotlin-multiplatform-bignum/) : 큰 수 처리를 위한 라이브러리 - [Apache License Version 2.0](https://github.com/ionspin/kotlin-multiplatform-bignum/blob/main/LICENSE)
