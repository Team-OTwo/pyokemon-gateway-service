package com.pyokemon.gateway_service.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pyokemon.gateway_service.common.constant.ApiPermitConstants;
import com.pyokemon.gateway_service.common.dto.ResponseDto;
import com.pyokemon.gateway_service.security.jwt.JwtTokenValidator;
import com.pyokemon.gateway_service.security.jwt.authentication.JwtAuthentication;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenValidator jwtTokenValidator;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final List<String> PUBLIC_PATHS = Stream.of(
            ApiPermitConstants.AccountApi.PERMIT_ALL,
            ApiPermitConstants.EventApi.PERMIT_ALL,
            ApiPermitConstants.SystemApi.PERMIT_ALL,
            ApiPermitConstants.GatewayApi.PERMIT_ALL,
            ApiPermitConstants.PaymentApi.PERMIT_ALL,
            new String[]{"/favicon.ico"} // Add other specific paths if needed
    ).flatMap(Arrays::stream).toList();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwtToken = jwtTokenValidator.getToken(request);

            if (jwtToken == null) {
                if (isPublicPath(request.getRequestURI())) {
                    filterChain.doFilter(request, response);
                } else {
                    log.warn("Authentication token is required for: {}", request.getRequestURI());
                    setErrorResponse(response, HttpStatus.FORBIDDEN, "Authentication token is required");
                }
                return;
            }

            JwtAuthentication authentication = jwtTokenValidator.validateToken(jwtToken);
            if (authentication != null) {
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("JWT token validation successful for user: {}", authentication.getPrincipal().getName());
                filterChain.doFilter(request, response);
            } else {
                log.warn("Invalid JWT token provided.");
                setErrorResponse(response, HttpStatus.FORBIDDEN, "Invalid authentication token");
            }

        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token detected: {}", e.getMessage());
            setErrorResponse(response, HttpStatus.UNAUTHORIZED, "Token has expired");
        } catch (Exception e) {
            log.error("Error during JWT token validation: {}", e.getMessage(), e);
            setErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while processing the token");
        }
    }

    private boolean isPublicPath(String requestURI) {
        return PUBLIC_PATHS.stream().anyMatch(pattern -> pathMatcher.match(pattern, requestURI));
    }

    private void setErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ResponseDto<Object> errorResponse = ResponseDto.builder()
                .success(false)
                .message(message)
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        String errorBody = objectMapper.writeValueAsString(errorResponse);

        response.setContentLength(errorBody.getBytes("UTF-8").length);
        response.getWriter().write(errorBody);
        response.getWriter().flush();
    }
}
