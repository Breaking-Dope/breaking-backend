package com.dope.breaking.service;

import com.dope.breaking.repository.PostLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;



}
