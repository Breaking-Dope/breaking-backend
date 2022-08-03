package com.dope.breaking.security.jwt;

import com.dope.breaking.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class JwtEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {


        String exceptionInfo = (String) request.getAttribute("exception");
        //JwtAuthenticationFilter에서 전달받은 예외 내용
        if (exceptionInfo != null) {
            setException(response, exceptionInfo);
        }
        //PreAuthorize 어노테이션에 의한 예외
        else if (authException instanceof InsufficientAuthenticationException) {
            exceptionInfo = "InsufficientAuthenticationException";
            setException(response, exceptionInfo);
        }
    }

    private void setException(HttpServletResponse response, String exceptionInfo) throws IOException {
        final String errorCode;
        final String message;
        if (exceptionInfo.equals("UsernameNotFoundException")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            setResponse(response, ErrorCode.NO_SUCH_USER.getCode(), ErrorCode.NO_SUCH_USER.getMessage());
        } else if (exceptionInfo.equals("ExpiredJwtException")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            setResponse(response, ErrorCode.EXPIRED_ACCESS_TOKEN.getCode(), ErrorCode.EXPIRED_ACCESS_TOKEN.getMessage());
        } else if (exceptionInfo.equals("AccessJwtException")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            setResponse(response, ErrorCode.INVALID_ACCESS_TOKEN.getCode(), ErrorCode.INVALID_ACCESS_TOKEN.getMessage());
        } else if( exceptionInfo.equals("InsufficientAuthenticationException")){
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            setResponse(response, ErrorCode.REQUIRE_LOGIN.getCode(),ErrorCode.REQUIRE_LOGIN.getMessage() );
        }
    }


    private void setResponse(HttpServletResponse response, String errorCode, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(response.getStatus());
        JSONObject responseJson = new JSONObject();
        responseJson.put("code", errorCode);
        responseJson.put("message", message);
        response.getWriter().print(responseJson);
    }
}
