package com.dope.breaking.exception.post;

import com.dope.breaking.exception.BreakingException;
import com.dope.breaking.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class MissionOnlyForPressException extends BreakingException {

    public MissionOnlyForPressException() {super(ErrorCode.MISSION_ONLY_FOR_PRESS, HttpStatus.BAD_REQUEST);}

}


