package com.dope.breaking.service;


import com.dope.breaking.domain.user.Follow;
import com.dope.breaking.repository.FollowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;

    public void deleteById(Long followId){
        followRepository.deleteById(followId);
    }

    public Optional<Follow> findById(Long followId) {return followRepository.findById(followId);}

}
