package com.dope.breaking.api;

import com.dope.breaking.dto.mission.MissionRequestDto;
import com.dope.breaking.service.MissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.security.Principal;

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

}
