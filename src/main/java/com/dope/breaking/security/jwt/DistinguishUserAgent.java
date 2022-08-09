package com.dope.breaking.security.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.rmi.server.ServerCloneException;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component //빈 등록
public class DistinguishUserAgent {

    private final String WEB_BROWSERS_USER_AGENT = "Mozilla";

    private final String ANDROID_USER_AGENT = "Dalvik";

    private final String POSTMAN_USER_AGENT = "PostmanRuntime";

    public String extractUserAgent(String userAgent) throws IOException, ServletException {
        return distinguishUserAgent(userAgent);
    }

    private String distinguishUserAgent(String userAgent){
        if(userAgent.startsWith(WEB_BROWSERS_USER_AGENT)){
            return "WEB";
        }else if (userAgent.startsWith(ANDROID_USER_AGENT)){
            return "ANDROID";
        }else if(userAgent.startsWith(POSTMAN_USER_AGENT)){
            return "POSTMAN";
        }else {
            return"OTHER";
        }
    }

}
