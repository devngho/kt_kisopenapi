[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.devngho/kt_kisopenapi/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.devngho/kt_kisopenapi)
# kt-kisopenapi

https://apiportal.koreainvestment.com/about

한국투자증권의 REST & WebSocket 방식 새 API 서비스를 쉽게 사용할 수 있는 Kotlin 라이브러리.

## 주의사항

> 법인 사용이 가능하나, **개인 사용 목적**으로 개발되었습니다.

> 개발자([devngho](https://github.com/devngho))는 라이브러리의 사용에 대해 **책임지지 않습니다**.

## 사용하기
Maven central에 배포되어 있습니다.

## 개발 도와주기
Pull requests를 사용하거나 Issues를 만들어주세요!
## 구현 진행
### API
다이렉트로 API 접속(비권장)
```kotlin
// 예시
InquirePrice(api).call(InquirePrice.InquirePriceData(""))
```
- [x] OAuth
  - [x] 접속토큰
      - [x] 발급
      - [x] 폐기
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
      - [ ] 기간별 시세
      - [ ] 실시간 시세
- [ ] 국내선물옵션
    - [ ] 주문
        - [ ] 주문 
        - [ ] 정정
        - [ ] 잔고 조회
    - [ ] 시세
        - [ ] 시세
        - [ ] 기간별 시세
- [ ] 해외주식
  - [ ] 주문
    - [ ] 주문
    - [ ] 정정
    - [ ] 예약
    - [ ] 잔고 조회
  - [ ] 현재가
    - [ ] 체결가
    - [ ] 기간별 시세
    - [ ] 조건검색
- [ ] 해외선물옵션
    - [ ] 주문
        - [ ] 주문
        - [ ] 정정
        - [ ] 잔고 조회
        - [ ] 주문 내역
    - [ ] 시세
        - [ ] 상세
        - [ ] 현재가
- [ ] 예제 코드

### Layer
```kotlin
// 예시(구현중)
val stock = Stock(api, "")
stock.updateBy(InquirePrice(api))
stock.price

stock.buy(price = 10000, count = 10)

stock.useLiveConfirmPrice {
    it.price
    stock.closeLiveConfirmPrice() // or it.close()
}


```
레이어를 통해 접속(권장)

## 오픈 소스 라이선스
### Kotlin
- [Kotlin/kotlinx.coroutines](https://github.com/Kotlin/kotlinx.coroutines) - [Apache License Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)
- [ktorio/ktor](https://github.com/ktorio/ktor) - [Apache License Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)
### Other
- [ionspin/kotlin-multiplatform-bignum](http://github.com/ionspin/kotlin-multiplatform-bignum/) - [Apache License Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)