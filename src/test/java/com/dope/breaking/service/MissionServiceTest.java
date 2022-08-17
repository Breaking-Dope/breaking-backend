package com.dope.breaking.service;

import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.mission.MissionRequestDto;
import com.dope.breaking.dto.post.LocationDto;
import com.dope.breaking.exception.auth.InvalidAccessTokenException;
import com.dope.breaking.exception.mission.MissionOnlyForPressException;
import com.dope.breaking.repository.MissionRepository;
import com.dope.breaking.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MissionServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MissionRepository missionRepository;

    @InjectMocks
    private MissionService missionService;

    @DisplayName("사용자가 언론사일 경우, 브레이킹미션이 생성된다.")
    @Test
    void createMissionByPress() {

        //Given
        User user = new User("username","password", Role.PRESS);

        LocalDateTime startTime = LocalDateTime.of(2022, Month.AUGUST, 16, 19, 30, 40);
        LocalDateTime endTime = LocalDateTime.of(2022, Month.AUGUST, 20, 19, 30, 40);

        LocationDto locationDto = new LocationDto("full address",10.0,10.0,"depth1","depth2");
        MissionRequestDto missionRequestDto = new MissionRequestDto("title","content", startTime, endTime, locationDto);

        //When
        when(userRepository.findByUsername("username")).thenReturn(Optional.of(user));

        //Then
        missionService.createMission(missionRequestDto,"username");

    }

    @DisplayName("사용자가 언론사가 아닐 경우, 예외가 발생한다.")
    @Test
    void createMissionNotByPress() {

        //Given
        User user = new User("username","password", Role.USER);

        LocalDateTime startTime = LocalDateTime.of(2022, Month.AUGUST, 16, 19, 30, 40);
        LocalDateTime endTime = LocalDateTime.of(2022, Month.AUGUST, 20, 19, 30, 40);

        LocationDto locationDto = new LocationDto("full address",10.0,10.0,"depth1","depth2");
        MissionRequestDto missionRequestDto = new MissionRequestDto("title","content",startTime,endTime, locationDto);

        //When
        when(userRepository.findByUsername("username")).thenReturn(Optional.of(user));

        //Then
        Assertions.assertThrows(MissionOnlyForPressException.class,
                () -> missionService.createMission(missionRequestDto,"username"));

    }

    @DisplayName("유저네임이 무효할 경우, 예외가 발생한다.")
    @Test
    void createMissionWithInvalidUsername() {

        //Given
        User user = new User("username","password", Role.USER);

        LocalDateTime startTime = LocalDateTime.of(2022, Month.AUGUST, 16, 19, 30, 40);
        LocalDateTime endTime = LocalDateTime.of(2022, Month.AUGUST, 20, 19, 30, 40);

        LocationDto locationDto = new LocationDto("full address",10.0,10.0,"depth1","depth2");
        MissionRequestDto missionRequestDto = new MissionRequestDto("title","content",null,null, locationDto);

        //Then
        Assertions.assertThrows(InvalidAccessTokenException.class,
                () -> missionService.createMission(missionRequestDto,"username1")); //When

    }

}