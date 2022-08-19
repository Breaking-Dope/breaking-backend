package com.dope.breaking.service;

import com.dope.breaking.domain.post.Location;
import com.dope.breaking.domain.post.Mission;
import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.mission.MissionFeedResponseDto;
import com.dope.breaking.dto.mission.MissionRequestDto;
import com.dope.breaking.dto.mission.MissionResponseDto;
import com.dope.breaking.dto.post.LocationDto;
import com.dope.breaking.exception.auth.InvalidAccessTokenException;
import com.dope.breaking.exception.mission.MissionOnlyForPressException;
import com.dope.breaking.exception.pagination.InvalidCursorException;
import com.dope.breaking.repository.MissionRepository;
import com.dope.breaking.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.BDDMockito.given;

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
        User user = new User("username", "password", Role.PRESS);

        LocalDateTime startTime = LocalDateTime.of(2022, Month.AUGUST, 16, 19, 30, 40);
        LocalDateTime endTime = LocalDateTime.of(2022, Month.AUGUST, 20, 19, 30, 40);

        LocationDto locationDto = new LocationDto("full address", 10.0, 10.0, "depth1", "depth2");
        MissionRequestDto missionRequestDto = new MissionRequestDto("title", "content", startTime, endTime, locationDto);

        //When
        when(userRepository.findByUsername("username")).thenReturn(Optional.of(user));

        //Then
        missionService.createMission(missionRequestDto, "username");

    }

    @DisplayName("사용자가 언론사가 아닐 경우, 예외가 발생한다.")
    @Test
    void createMissionNotByPress() {

        //Given
        User user = new User("username", "password", Role.USER);

        LocalDateTime startTime = LocalDateTime.of(2022, Month.AUGUST, 16, 19, 30, 40);
        LocalDateTime endTime = LocalDateTime.of(2022, Month.AUGUST, 20, 19, 30, 40);

        LocationDto locationDto = new LocationDto("full address", 10.0, 10.0, "depth1", "depth2");
        MissionRequestDto missionRequestDto = new MissionRequestDto("title", "content", startTime, endTime, locationDto);

        //When
        when(userRepository.findByUsername("username")).thenReturn(Optional.of(user));

        //Then
        assertThrows(MissionOnlyForPressException.class,
                () -> missionService.createMission(missionRequestDto,"username"));

    }

    @DisplayName("유저네임이 무효할 경우, 예외가 발생한다.")
    @Test
    void createMissionWithInvalidUsername() {

        //Given
        User user = new User("username", "password", Role.USER);

        LocalDateTime startTime = LocalDateTime.of(2022, Month.AUGUST, 16, 19, 30, 40);
        LocalDateTime endTime = LocalDateTime.of(2022, Month.AUGUST, 20, 19, 30, 40);

        LocationDto locationDto = new LocationDto("full address", 10.0, 10.0, "depth1", "depth2");
        MissionRequestDto missionRequestDto = new MissionRequestDto("title", "content", null, null, locationDto);

        //Then
        assertThrows(InvalidAccessTokenException.class,
                () -> missionService.createMission(missionRequestDto,"username1")); //When

    }

    @DisplayName("게시글이 존재하지 않을 때, 예외를 반환한다.")
    @Test
    void readMissionButNoExistMissionId() {
        User user = new User("username", "password", Role.USER);

        LocalDateTime startTime = LocalDateTime.parse("2016-10-31 23:59:59", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        LocalDateTime endTime = LocalDateTime.parse("2016-11-31 23:59:59", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        LocationDto locationDto = new LocationDto("full address", 10.0, 10.0, "depth1", "depth2");
        MissionRequestDto missionRequestDto = new MissionRequestDto("title", "content", null, null, locationDto);

        //Then
        assertThrows(InvalidAccessTokenException.class,
                () -> missionService.createMission(missionRequestDto, "username1")); //When
    }

    @DisplayName("작성자가 자신의 미션을 조회 시, 정상적으로 조회된다.")
    @Test
    void isMyMissionIsTrueWhenWriter(){
        User user = new User("username", "password", Role.PRESS);

        when(userRepository.save(user)).thenReturn(user);
        when(userRepository.findByUsername("username")).thenReturn(Optional.of(user));
        userRepository.save(user);

        LocalDateTime startTime = LocalDateTime.of(2022, Month.AUGUST, 16, 19, 30, 40);
        LocalDateTime endTime = LocalDateTime.of(2022, Month.AUGUST, 20, 19, 30, 40);

        Location location = new Location("full address", 10.0, 10.0, "depth1", "depth2");
        Mission mission = Mission.builder()
                .content("content")
                .title("title")
                .user(user)
                .endTime(endTime)
                .startTime(startTime)
                .location(location)
                .build();

        given(missionRepository.save(mission)).willReturn(mission);
        missionRepository.save(mission);
        given(missionRepository.findById(1L)).willReturn(Optional.of(mission));

        missionService.readMission(1L, "username");

    }

    @DisplayName("미션 피드가 정상 작동한다.")
    @Test
    void searchMissionFeed() {

        List<MissionFeedResponseDto> dummy = new ArrayList<>();

        dummy.add(new MissionFeedResponseDto());
        dummy.add(new MissionFeedResponseDto());
        dummy.add(new MissionFeedResponseDto());

        when(missionRepository.searchMissionFeed(null, null, 10L)).thenReturn(dummy);

        List<MissionFeedResponseDto> result = missionService.searchMissionFeed(null, null, 10L);
        assertEquals(3, result.size());
    }

    @DisplayName("커서 id가 잘못되었을 경우, 예외가 발생한다.")
    @Test
    void searchMissionFeedInvalidCursor() {

        when(missionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(InvalidCursorException.class,
                () -> missionService.searchMissionFeed(null, 999L, 10L));
    }

}
