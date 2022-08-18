package com.dope.breaking.service;

import com.dope.breaking.domain.post.Location;
import com.dope.breaking.domain.post.Mission;
import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.mission.MissionFeedResponseDto;
import com.dope.breaking.dto.mission.MissionRequestDto;
import com.dope.breaking.dto.mission.MissionResponseDto;
import com.dope.breaking.dto.post.LocationDto;
import com.dope.breaking.dto.post.WriterDto;
import com.dope.breaking.exception.BreakingException;
import com.dope.breaking.exception.ErrorCode;
import com.dope.breaking.exception.auth.InvalidAccessTokenException;
import com.dope.breaking.exception.mission.MissionOnlyForPressException;
import com.dope.breaking.exception.mission.NoSuchBreakingMissionException;
import com.dope.breaking.exception.pagination.InvalidCursorException;
import com.dope.breaking.repository.MissionRepository;
import com.dope.breaking.repository.PostRepository;
import com.dope.breaking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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

    public ResponseEntity<MissionResponseDto> readMission(long missionId, String crntUsername){

        //예외처리 수정 필요
        Mission mission = missionRepository.findById(missionId).orElseThrow(() -> new NoSuchBreakingMissionException());


        boolean isMyMission = false;
        if(crntUsername != null){
            User user = userRepository.findByUsername(crntUsername).get();
            isMyMission = user == mission.getUser(); //JPA의 동일성 보장
        }

        LocationDto locationDto = LocationDto.builder()
                .address(mission.getLocation().getAddress())
                .latitude(mission.getLocation().getLatitude())
                .longitude(mission.getLocation().getLongitude())
                .region_1depth_name(mission.getLocation().getRegion_1depth_name())
                .region_2depth_name(mission.getLocation().getRegion_2depth_name()).build();


        User missionWriter = mission.getUser();
        WriterDto writerDto = WriterDto.builder()
                .userId(missionWriter.getId())
                .profileImgURL(missionWriter.getOriginalProfileImgURL())
                .nickname(missionWriter.getNickname()).build();


        MissionResponseDto missionResponseDto = MissionResponseDto.builder()
                .isMyMission(isMyMission)
                .title(mission.getTitle())
                .content(mission.getContent())
                .startTime(mission.getStartTime())
                .endTime(mission.getEndTime())
                .locationDto(locationDto)
                .writerDto(writerDto)
                .build();

        return new ResponseEntity<MissionResponseDto>(missionResponseDto, HttpStatus.OK);

    }
    
    public List<MissionFeedResponseDto> searchMissionFeed(String username, Long cursorMissionId, Long size) {

        User me = null;
        if(username != null) {
            me = userRepository.findByUsername(username).orElseThrow(InvalidAccessTokenException::new);
        }

        Mission cursorMission = null;
        if(cursorMissionId != null && cursorMissionId != 0) {
            cursorMission = missionRepository.findById(cursorMissionId).orElseThrow(InvalidCursorException::new);
        }

        return missionRepository.searchMissionFeed(me, cursorMission, size);
    }

}
