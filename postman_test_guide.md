# 게이트웨이 서비스 Postman 테스트 가이드

## 1. 환경 설정

### 기본 설정
- **게이트웨이 URL**: `http://localhost:8086`
- **계정 서비스 URL**: `http://localhost:8081` (게이트웨이 뒤에 위치)

### 환경 변수 설정
1. Postman에서 환경 생성: "Pyokemon Gateway"
2. 다음 변수 추가:
   - `gateway_url`: `http://localhost:8086`
   - `token`: (로그인 후 저장될 JWT 토큰)

## 2. 테스트 시나리오

### 2.1 공개 API 테스트 (인증 필요 없음)

#### 헬스 체크 API
- **요청 방법**: GET
- **URL**: `{{gateway_url}}/api/account/health`
- **예상 응답**: 200 OK

#### 로그인 API
- **요청 방법**: POST
- **URL**: `{{gateway_url}}/api/account/login`
- **헤더**:
  - Content-Type: application/json
- **요청 본문**:
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```
- **응답 처리**:
  - 성공 시 JWT 토큰이 반환됨
  - 토큰을 환경 변수 `token`에 저장

### 2.2 인증이 필요한 API 테스트

#### 계정 정보 조회
- **요청 방법**: GET
- **URL**: `{{gateway_url}}/api/account/me`
- **헤더**:
  - Authorization: Bearer {{token}}
- **예상 응답**: 200 OK와 사용자 정보

#### 이벤트 목록 조회
- **요청 방법**: GET
- **URL**: `{{gateway_url}}/api/event/list`
- **헤더**:
  - Authorization: Bearer {{token}}
- **예상 응답**: 200 OK와 이벤트 목록

## 3. JWT 토큰 검증

### 유효한 토큰 구조
```
{
  "userId": "user123",  // 또는 "accountId": "user123"
  "role": "USER",
  "tokenType": "access",
  "exp": 1627484400  // 만료 시간 (Unix timestamp)
}
```

### 헤더 전달 확인
게이트웨이는 인증된 요청에 다음 헤더를 추가합니다:
- `X-Auth-AccountId`: 사용자 계정 ID
- `X-Auth-Role`: 사용자 역할

## 4. 오류 테스트

### 인증 없이 보호된 API 접근
- **요청 방법**: GET
- **URL**: `{{gateway_url}}/api/account/me`
- **헤더**: 없음
- **예상 응답**: 401 Unauthorized

### 만료된 토큰으로 요청
- **요청 방법**: GET
- **URL**: `{{gateway_url}}/api/account/me`
- **헤더**:
  - Authorization: Bearer (만료된 토큰)
- **예상 응답**: 401 Unauthorized

### 잘못된 경로 요청
- **요청 방법**: GET
- **URL**: `{{gateway_url}}/api/unknown/path`
- **예상 응답**: 404 Not Found

## 5. CORS 테스트

### 프리플라이트 요청
- **요청 방법**: OPTIONS
- **URL**: `{{gateway_url}}/api/account/me`
- **헤더**:
  - Origin: http://example.com
  - Access-Control-Request-Method: GET
  - Access-Control-Request-Headers: Authorization
- **예상 응답**: 
  - 200 OK
  - Access-Control-Allow-Origin: *
  - Access-Control-Allow-Methods: *
  - Access-Control-Allow-Headers: *

## 6. 테스트 자동화 (Postman Collection Runner)

1. 위 테스트 케이스를 컬렉션으로 구성
2. 다음 순서로 테스트 실행:
   - 헬스 체크
   - 로그인 (토큰 저장)
   - 인증이 필요한 API 테스트
   - 오류 케이스 테스트