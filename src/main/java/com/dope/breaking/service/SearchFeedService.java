package com.dope.breaking.service;

import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.post.FeedResultPostDto;
import com.dope.breaking.dto.post.SearchFeedConditionDto;
import com.dope.breaking.exception.auth.InvalidAccessTokenException;
import com.dope.breaking.exception.post.NoSuchPostException;
import com.dope.breaking.exception.user.LoginRequireException;
import com.dope.breaking.exception.user.NoPermissionException;
import com.dope.breaking.exception.user.NoSuchUserException;
import com.dope.breaking.repository.FeedRepository;
import com.dope.breaking.repository.PostRepository;
import com.dope.breaking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchFeedService {

    private final FeedRepository feedRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public List<FeedResultPostDto> searchMainFeed(SearchFeedConditionDto searchFeedConditionDto, String username, Long cursorId) {

        User me = null;
        if(username != null) {
            me = userRepository.findByUsername(username).orElseThrow(InvalidAccessTokenException::new);
        }

        Post cursorPost = null;
        if(cursorId != null && cursorId != 0) {
            cursorPost = postRepository.findById(cursorId).orElseThrow(NoSuchPostException::new);
        }

        if(searchFeedConditionDto.getForLastMin() != null) {
            searchFeedConditionDto.setDateFrom(LocalDateTime.now().minusMinutes(searchFeedConditionDto.getForLastMin()));
            searchFeedConditionDto.setDateTo(LocalDateTime.now());
        }

        List<FeedResultPostDto> result = feedRepository.searchFeedBy(searchFeedConditionDto, cursorPost, me);
        for(FeedResultPostDto dto: result) {
            if(dto.getIsAnonymous()) {
                dto.setUser(null);
            }
        }

        return result;

    }

    public List<FeedResultPostDto> searchUserFeed(SearchFeedConditionDto searchFeedConditionDto, Long ownerId, String username, Long cursorId) {

        User owner = userRepository.findById(ownerId).orElseThrow(NoSuchUserException::new);

        User me = null;
        if(username != null) {

            me = userRepository.findByUsername(username).orElseThrow(InvalidAccessTokenException::new);

            if(searchFeedConditionDto.getUserPageFeedOption() != UserPageFeedOption.WRITE
                    && owner != me) {
                throw new NoPermissionException();
            }

        } else if(searchFeedConditionDto.getUserPageFeedOption() != UserPageFeedOption.WRITE) {

            throw new LoginRequireException();
        }

        Post cursorPost = null;
        if(cursorId != null && cursorId != 0) {
            cursorPost = postRepository.findById(cursorId).orElseThrow(NoSuchPostException::new);
        }

        return feedRepository.searchUserPageBy(searchFeedConditionDto, owner, me, cursorPost);

    }

}
