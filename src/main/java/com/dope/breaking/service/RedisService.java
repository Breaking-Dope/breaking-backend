package com.dope.breaking.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisService {
    private final RedisTemplate<String, Object> redisTemplate;

    private final RedisTemplate<String, Object> redisBlackListTemplate; //블랙리스트를 위한 template

    public String getData(String key){
        return (String) redisTemplate.opsForValue().get(key); //key값을 바탕으로 value를 가져온다.
    }


    public void setData(String key, String value){ //키 : 밸류
        this.redisTemplate.opsForValue().set(key, value);
    }

    public void setDataWithExpiration(String key, String value, Long time){  // 키 : 밸류 : 만료시간.
        if(this.getData(key) != null){ //우선 삭제.
            this.redisTemplate.delete(key);
        }
        Duration expireDuration = Duration.ofSeconds(time);
        redisTemplate.opsForValue().set(key, value, expireDuration);
    }

    public boolean hasKey(String key) {
        return redisBlackListTemplate.hasKey(key);
    }

    public void deleteValues(String key){ //데이터 삭제
        redisTemplate.delete(key);
    }


    public void setBlackListToken(String key, String value, Long time ){
        Duration expireDuration = Duration.ofSeconds(time);
        redisBlackListTemplate.opsForValue().set(key, value, expireDuration);
    }

    public Object getBlackListToken(String key) {
        return redisBlackListTemplate.opsForValue().get(key);
    }

    public boolean hasKeyBlackListToken(String key) {
        return redisBlackListTemplate.hasKey(key);
    }

    public void deleteBlackListToken(String key) {
        redisBlackListTemplate.delete(key);
    }


}
