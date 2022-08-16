package com.dope.breaking.repository;

import com.dope.breaking.domain.post.Location;
import com.dope.breaking.domain.post.Mission;
import com.dope.breaking.domain.user.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MissionRepositoryTest {

    @Autowired MissionRepository missionRepository;
    @Autowired UserRepository userRepository;

    @DisplayName("브레이킹 미션을 생성할 시, 미션이 정상적으로 저장된다.")
    @Test
    void createMission(){

        //Given
        User user = new User();
        userRepository.save(user);

        Location location = new Location("full address", 10.0, 10.0, "depth1", "depth2");
        Mission mission = new Mission(user,"title","content",null,null,location);

        //When
        Long missionId = missionRepository.save(mission).getId();

        //Then
        Assertions.assertEquals(1, missionRepository.findAll().size());
        Assertions.assertEquals(user,missionRepository.findById(missionId).get().getUser());

    }

}