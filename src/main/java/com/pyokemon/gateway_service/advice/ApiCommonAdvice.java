package com.pyokemon.gateway_service.advice;

import com.pyokemon.gateway_service.common.dto.ResponseDto;
import com.pyokemon.gateway_service.common.exception.BadParameter;
import com.pyokemon.gateway_service.common.exception.ClientError;
import com.pyokemon.gateway_service.common.exception.NotFound;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@Order(value = 1)
@RestControllerAdvice
public class ApiCommonAdvice {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({BadParameter.class})
    public ResponseDto<String> handleBadParameter(BadParameter e) {
        return ResponseDto.error(
                e.getErrorCode(),
                e.getErrorMessage()
        );
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({NotFound.class})
    public ResponseDto<String> handleNotFound(NotFound e) {
        return ResponseDto.error(
                e.getErrorCode(),
                e.getErrorMessage()
        );
    }


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({ClientError.class})
    public ResponseDto<String> handleClientException(ClientError e) {
        return ResponseDto.error(
                e.getErrorCode(),
                e.getErrorMessage()
        );
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({Exception.class})
    public ResponseDto<String> handleException(Exception e) {
        log.error("Unhandled exception caught", e);
        return ResponseDto.error(
                "ServerError",
                "서버 에러입니다."
        );
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler({InsufficientAuthenticationException.class})
    public ResponseDto<String> handleInsufficientAuthenticationException(
            InsufficientAuthenticationException e) {
        return ResponseDto.error(
                "Unauthenticated",
                "인증되지 않았습니다.");
    }
}
