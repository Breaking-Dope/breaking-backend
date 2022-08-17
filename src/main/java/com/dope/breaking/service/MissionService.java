package com.dope.breaking.service;

import com.dope.breaking.domain.post.Location;
import com.dope.breaking.domain.post.Mission;
import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.mission.MissionRequestDto;
import com.dope.breaking.exception.auth.InvalidAccessTokenException;
import com.dope.breaking.exception.mission.MissionOnlyForPressException;
import com.dope.breaking.exception.mission.NoSuchBreakingMissionException;
import com.dope.breaking.repository.MissionRepository;
import com.dope.breaking.repository.PostRepository;
import com.dope.breaking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MissionService {

    private final MissionRepository missionRepository;
    private final UserRepository userRepository;
    private final PostService postService;
    private final PostRepository postRepository;

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

    @Transactional
    public Map<String, Long> postSubmission(String username, String contentData, List<MultipartFile> files, Long missionId) throws Exception {

        Mission mission = missionRepository.findById(missionId).orElseThrow(NoSuchBreakingMissionException::new);
        Long postId = postService.create(username,contentData,files);
        postRepository.findById(postId).get().updateMission(mission);
        Map<String, Long> result = new LinkedHashMap<>();
        result.put("postId", postId);
        return result;

    }

}
