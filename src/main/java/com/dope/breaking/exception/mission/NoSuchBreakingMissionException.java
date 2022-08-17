package com.dope.breaking.exception.mission;

import com.dope.breaking.exception.BreakingException;
import com.dope.breaking.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class NoSuchBreakingMissionException extends BreakingException {

    public NoSuchBreakingMissionException() {super(ErrorCode.NO_SUCH_BREAKING_MISSION, HttpStatus.BAD_REQUEST);}

}


