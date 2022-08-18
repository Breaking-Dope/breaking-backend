package com.dope.breaking.api;

import com.dope.breaking.dto.comment.CommentResponseDto;
import com.dope.breaking.dto.mission.MissionFeedResponseDto;
import com.dope.breaking.dto.mission.MissionRequestDto;
import com.dope.breaking.service.MissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class MissionAPI {

    private final MissionService missionService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/breaking-mission")
    public ResponseEntity createMission(Principal principal, @RequestBody @Valid MissionRequestDto missionRequestDto){
        missionService.createMission(missionRequestDto, principal.getName());
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/breaking-mission/{missionId}", consumes = {"multipart/form-data"})
    public ResponseEntity<Map<String, Long>> submitPostForMission(Principal principal, @PathVariable Long missionId, @RequestPart(value = "mediaList", required = false) List<MultipartFile> files, @RequestPart(value = "data") String contentData) throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(missionService.postSubmission(principal.getName(),contentData,files,missionId));
    }

    @GetMapping("/breaking-mission/feed")
    public ResponseEntity<List<MissionFeedResponseDto>> getMissionFeed(
            Principal principal,
            @RequestParam(value="cursor") Long cursorMissionId,
            @RequestParam(value="size") Long size) {

        String username = null;
        if(principal!=null) {
            username = principal.getName();
        }

        return ResponseEntity.ok().body(missionService.searchMissionFeed(username, cursorMissionId, size));
    }

}
