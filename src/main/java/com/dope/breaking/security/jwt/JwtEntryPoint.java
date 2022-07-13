package com.dope.breaking.security.jwt;

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

        String exception = (String) request.getAttribute("exception");
        if (exception != null) {
            setResponse(response, exception);
        }
        //jwt 없이 접급하려고 할때.
        if(authException instanceof InsufficientAuthenticationException){
            JSONObject responseJson = new JSONObject();
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            responseJson.put("message", "로그인이 필요합니다.");

            response.getWriter().print(responseJson);
        }
    }

    private void setResponse(HttpServletResponse response, String exception) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        JSONObject responseJson = new JSONObject();
        responseJson.put("message", exception);

        response.getWriter().print(responseJson);
    }
}
