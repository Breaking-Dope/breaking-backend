package com.dope.breaking.service;

import com.dope.breaking.domain.post.Location;
import com.dope.breaking.domain.post.Mission;
import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.post.PostType;
import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.mission.MissionFeedResponseDto;
import com.dope.breaking.dto.mission.MissionRequestDto;
import com.dope.breaking.dto.mission.MissionResponseDto;
import com.dope.breaking.dto.post.LocationDto;
import com.dope.breaking.dto.post.WriterDto;
import com.dope.breaking.exception.auth.InvalidAccessTokenException;
import com.dope.breaking.exception.mission.MissionOnlyForPressException;
import com.dope.breaking.exception.mission.NoSuchBreakingMissionException;
import com.dope.breaking.exception.pagination.InvalidCursorException;
import com.dope.breaking.repository.MissionRepository;
import com.dope.breaking.repository.PostRepository;
import com.dope.breaking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

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
        Post post = postRepository.findById(postId).get();
        post.updateMission(mission);

        if(!post.isSold()) {
            Timer timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    if (post.getPrice() == 0) {
                        post.updatePostType(PostType.FREE);
                    }
                    else{
                        post.updatePostType(PostType.CHARGED);
                    }
                }
            };
            timer.schedule(timerTask, java.sql.Timestamp.valueOf(mission.getEndTime()));
        }

        Map<String, Long> result = new LinkedHashMap<>();
        result.put("postId", postId);
        return result;

    }

    public MissionResponseDto readMission(long missionId, String username){

        Mission mission = missionRepository.findById(missionId).orElseThrow(NoSuchBreakingMissionException::new);
        mission.increaseViewCount();

        boolean isMyMission = false;
        if(username != null){
            User user = userRepository.findByUsername(username).get();
            isMyMission = user == mission.getUser();
        }

        return MissionResponseDto.builder()
                .isMyMission(isMyMission)
                .title(mission.getTitle())
                .viewCount(mission.getViewCount())
                .content(mission.getContent())
                .startTime(mission.getStartTime())
                .endTime(mission.getEndTime())
                .locationDto(
                        LocationDto.builder()
                                .address(mission.getLocation().getAddress())
                                .latitude(mission.getLocation().getLatitude())
                                .longitude(mission.getLocation().getLongitude())
                                .region_1depth_name(mission.getLocation().getRegion_1depth_name())
                                .region_2depth_name(mission.getLocation().getRegion_2depth_name()).build()
                )
                .writerDto(
                        WriterDto.builder()
                                .userId(mission.getUser().getId())
                                .profileImgURL(mission.getUser().getOriginalProfileImgURL())
                                .nickname(mission.getUser().getNickname()).build()
                )
                .build();

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
