package com.pyokemon.gateway_service.common.constant;

public final class ApiPermitConstants {

    private ApiPermitConstants() {
        // 상수 클래스이므로 인스턴스화 방지
    }

    // 계정 관련 허용 엔드포인트
    public static class AccountApi {
        public static final String[] PERMIT_ALL = {
                "/account/api/login",
                "/account/api/app/login",
                "/account/api/users",
                "/account/api/users/check-duplicate",
                "/account/api/users/notification",
                "/account/api/app/verify"
        };
    }

    // 이벤트 관련 허용 엔드포인트
    public static class EventApi {
        public static final String[] PERMIT_ALL = {
                "/event/api/events",
                "/event/api/events/open-today",
                "/event/api/events/to-be-opened",
                "/event/api/seats",
                "/event/api/events/*"
        };
    }

    // 결제 관련 허용 엔드포인트
    public static class PaymentApi {
        public static final String[] PERMIT_ALL = {
                "/payment/api/payments/confirm-save"
        };
    }

    // 시스템 헬스체크 관련 허용 엔드포인트
    public static class SystemApi {
        public static final String[] PERMIT_ALL = {
                "/health/**",
                "/actuator/**",
                "/actuator/health/**"
        };
    }

    // 게이트웨이 관련 허용 엔드포인트
    public static class GatewayApi {
        public static final String[] PERMIT_ALL = {
                "/api/gateway/v1/**"
        };
    }
}

