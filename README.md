[//]: # (// @formatter:off)

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.devngho/kt_kisopenapi/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.devngho/kt_kisopenapi)
[![javadoc](https://javadoc.io/badge2/io.github.devngho/kt_kisopenapi/javadoc.svg)](https://javadoc.io/doc/io.github.devngho/kt_kisopenapi)
[![Java Version](https://img.shields.io/badge/java->=11-blue)]()
[![License MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://raw.githubusercontent.com/devngho/kt_kisopenapi/master/LICENSE)
# kt_kisopenapi

[한국투자증권 KIS Developers](https://apiportal.koreainvestment.com/about)

한국투자증권의 오픈 API 서비스를 Kotlin/Java 환경에서 사용할 수 있는 라이브러리입니다.
대부분의 API를 지원하며, 중복 요청 없는 웹소켓 등 다양한 기능을 제공합니다.

## 주의사항

> **Java 11** 이상을 요구합니다.

> 이 라이브러리는 **알파 버전**으로, API가 수시로 변경될 수 있습니다.

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
val stock = api.stockDomestic(api, "주식 종목 코드")
stock.updateBy<StockPrice>()
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
JavaUtil.updateBy(stock, StockPrice.class).get();
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
      - [x] 정정/취소
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
      - [x] 조건 검색
      - [x] 거래량순위
- [ ] 국내선물옵션 _(지원 예정 없음)_

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
    - [x] 정정/취소
    - [ ] 예약
    - [x] 잔고 조회
  - [ ] 시세
    - [ ] 기간별 시세
    - [x] 조건검색
    - [ ] 실시간 시세
        - [x] 지연체결가
        - [ ] 지연호가(아시아)
        - [ ] 지연호가(미국)
- [ ] 해외선물옵션 _(지원 예정 없음)_

## 오픈 소스 라이선스
이 라이브러리는 MIT License를 사용합니다. 자세한 내용은 LICENSE 파일을 참조하세요.

다음은 사용하는 라이브러리의 라이선스입니다.
### Kotlin
- [Jetbrains/Kotlin](https://github.com/JetBrains/kotlin) - [Apache License Version 2.0](https://github.com/JetBrains/kotlin/blob/master/license/LICENSE.txt)
- [Kotlin/kotlinx.coroutines](https://github.com/Kotlin/kotlinx.coroutines) - [Apache License Version 2.0](https://github.com/Kotlin/kotlinx.coroutines/blob/master/LICENSE.txt)
- [ktorio/ktor](https://github.com/ktorio/ktor) - [Apache License Version 2.0](https://github.com/ktorio/ktor/blob/main/LICENSE)
### Other
- [soywiz-archive/krypto](https://github.com/soywiz-archive/krypto/blob/master/LICENSE) (코드 복사함) - [MIT License](https://github.com/soywiz-archive/krypto/blob/master/LICENSE)
- [ionspin/kotlin-multiplatform-bignum](http://github.com/ionspin/kotlin-multiplatform-bignum/) - [Apache License Version 2.0](https://github.com/ionspin/kotlin-multiplatform-bignum/blob/main/LICENSE)
- [kotest](https://github.com/kotest/kotest) - [Apache License Version 2.0](https://github.com/kotest/kotest/blob/master/LICENSE)
