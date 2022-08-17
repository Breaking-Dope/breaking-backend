package com.dope.breaking.service;

import com.dope.breaking.domain.post.Location;
import com.dope.breaking.domain.post.Mission;
import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.mission.MissionRequestDto;
import com.dope.breaking.exception.auth.InvalidAccessTokenException;
import com.dope.breaking.exception.post.MissionOnlyForPressException;
import com.dope.breaking.repository.MissionRepository;
import com.dope.breaking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MissionService {

    private final MissionRepository missionRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createMission(MissionRequestDto missionRequestDto, String username){

        User user = userRepository.findByUsername(username).orElseThrow(InvalidAccessTokenException::new);

        if(user.getRole() == Role.USER){
            throw new MissionOnlyForPressException();
        }

        Location location = new Location(
                missionRequestDto.getLocationDto().getAddress(),
                missionRequestDto.getLocationDto().getLongitude(),
                missionRequestDto.getLocationDto().getLatitude(),
                missionRequestDto.getLocationDto().getRegion_1depth_name(),
                missionRequestDto.getLocationDto().getRegion_2depth_name());

        Mission mission = new Mission(
                user,
                missionRequestDto.getTitle(),
                missionRequestDto.getContent(),
                missionRequestDto.getStartTime(),
                missionRequestDto.getEndTime(),
                location);

        missionRepository.save(mission);

    }

}
