package com.dope.breaking.api;

import com.dope.breaking.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class PostLikeAPI {


    private final PostLikeService postLikeService;


}
