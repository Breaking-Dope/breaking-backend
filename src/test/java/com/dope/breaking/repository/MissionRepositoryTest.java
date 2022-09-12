package com.dope.breaking.repository;

import com.dope.breaking.domain.post.Location;
import com.dope.breaking.domain.post.Mission;
import com.dope.breaking.domain.post.PostType;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.mission.MissionFeedResponseDto;
import com.dope.breaking.dto.post.FeedResultPostDto;
import com.dope.breaking.dto.post.SearchFeedConditionDto;
import com.dope.breaking.service.SearchMissionConditionDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MissionRepositoryTest {

    @Autowired
    MissionRepository missionRepository;
    @Autowired
    UserRepository userRepository;

    @DisplayName("브레이킹 미션을 생성할 시, 미션이 정상적으로 저장된다.")
    @Test
    void createMission() {

        //Given
        User user = new User();
        userRepository.save(user);

        Location location = new Location("full address", 10.0, 10.0, "depth1", "depth2");
        Mission mission = new Mission(user, "title", "content", null, null, location);

        //When
        Long missionId = missionRepository.save(mission).getId();

        //Then
        assertEquals(1, missionRepository.findAll().size());
        assertEquals(user,missionRepository.findById(missionId).get().getUser());

    }

    @DisplayName("브레이킹 미션을 조회 시, 미션 상제 정보가 반횐된다.")
    @Test
    void readMission() {

        //Given
        User user = new User();
        userRepository.save(user);

        Location location = new Location("full address", 10.0, 10.0, "depth1", "depth2");
        Mission mission = new Mission(user, "title", "content", null, null, location);
        Long missionId = missionRepository.save(mission).getId();

        //When
        Mission mission1 = missionRepository.findById(missionId).get();


        //Then
        Assertions.assertEquals("title", mission1.getTitle());
        Assertions.assertEquals("content", mission1.getContent());

        Assertions.assertEquals(null, mission1.getStartTime());

        Assertions.assertEquals(null, mission1.getEndTime());

        Assertions.assertEquals(user, mission1.getUser());

    }

    @DisplayName("미션 피드 조회가 정상적으로 동작한다.")
    @Test
    void searchMissionFeed() {

        User user = new User();
        userRepository.save(user);

        for(int i=0;i<10;i++) {
            Location location = new Location("full address", 10.0, 10.0, "depth1", "depth2");
            Mission mission = new Mission(user, "title", "content", null, null, location);
            missionRepository.save(mission);
        }
        SearchMissionConditionDto searchMissionConditionDto = SearchMissionConditionDto.builder().build();

        List<MissionFeedResponseDto> result = missionRepository.searchMissionFeed(null, null, 20L, searchMissionConditionDto);

        assertEquals(10, result.size());

    }

    @DisplayName("미션 피드에서 내가 출제한 미션은 isMyMission이 true로 반환된다.")
    @Test
    void searchMissionFeedIsMyMission() {

        User me = new User();
        userRepository.save(me);

        Location location = new Location("full address", 10.0, 10.0, "depth1", "depth2");
        Mission mission = new Mission(me, "title", "content", null, null, location);
        missionRepository.save(mission);

        SearchMissionConditionDto searchMissionConditionDto = SearchMissionConditionDto.builder().build();

        List<MissionFeedResponseDto> result = missionRepository.searchMissionFeed(me, null, 20L, searchMissionConditionDto);

        assertEquals(1, result.size());
        assertTrue(result.get(0).getIsMyMission());

    }


    @DisplayName("유저가 조회한 게시글은 조회수가 증가한다.")
    @Test
    void missionViewCountIncrement() {

        User missionOwner = new User();
        userRepository.save(missionOwner);

        Mission mission = Mission.builder()
                .user(missionOwner)
                .build();
        missionRepository.save(mission);

        mission.increaseViewCount();
        mission.increaseViewCount();
        mission.increaseViewCount();

        assertEquals(3, mission.getViewCount());
    }

    @DisplayName("유저가 조회한 미션은 미션 피드에서 조회수가 증가한다.")
    @Test
    void missionViewCountIncrementOnFeed() {

        User missionOwner = new User();
        userRepository.save(missionOwner);

        Mission mission = Mission.builder()
                .user(missionOwner)
                .build();
        missionRepository.save(mission);

        mission.increaseViewCount();
        mission.increaseViewCount();
        mission.increaseViewCount();

        SearchMissionConditionDto searchMissionConditionDto = SearchMissionConditionDto.builder().build();

        List<MissionFeedResponseDto> result = missionRepository.searchMissionFeed(null, null, 20L, searchMissionConditionDto);

        assertEquals(3, result.get(0).getViewCount());
    }

    @DisplayName("해당 미션과 관련된 포스트의 개수가 나타난다.")
    @Test
    void missionRelatedPostCount() {

        User missionOwner = new User();
        userRepository.save(missionOwner);

        Mission mission = Mission.builder()
                .user(missionOwner)
                .build();
        missionRepository.save(mission);

        for(int i=0;i<10;i++) {
            Post post = new Post();
            post.setUser(missionOwner);
            post.updateMission(mission);
            postRepository.save(post);
        }

        List<MissionFeedResponseDto> result = missionRepository.searchMissionFeed(null, null, 20L);

        assertEquals(1, result.size());
        assertEquals(10, result.get(0).getPostCount());
    }

}