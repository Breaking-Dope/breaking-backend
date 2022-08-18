package com.dope.breaking.service;

import com.dope.breaking.domain.post.Mission;
import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.post.FeedResultPostDto;
import com.dope.breaking.dto.post.SearchFeedConditionDto;
import com.dope.breaking.dto.user.SearchUserResponseDto;
import com.dope.breaking.exception.auth.InvalidAccessTokenException;
import com.dope.breaking.exception.mission.NoSuchBreakingMissionException;
import com.dope.breaking.exception.pagination.InvalidCursorException;
import com.dope.breaking.exception.user.LoginRequireException;
import com.dope.breaking.exception.user.NoPermissionException;
import com.dope.breaking.exception.user.NoSuchUserException;
import com.dope.breaking.repository.*;
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
    private final BookmarkRepository bookmarkRepository;
    private final PostLikeRepository postLikeRepository;
    private final FollowRepository followRepository;
    private final MissionRepository missionRepository;

    public List<FeedResultPostDto> searchMainFeed(SearchFeedConditionDto searchFeedConditionDto, String username, Long cursorId) {

        User me = null;
        if(username != null) {
            me = userRepository.findByUsername(username).orElseThrow(InvalidAccessTokenException::new);
        }

        Post cursorPost = null;
        if(cursorId != null && cursorId != 0) {
            cursorPost = postRepository.findById(cursorId).orElseThrow(InvalidCursorException::new);
        }

        if(searchFeedConditionDto.getForLastMin() != null) {
            searchFeedConditionDto.setDateFrom(LocalDateTime.now().minusMinutes(searchFeedConditionDto.getForLastMin()));
            searchFeedConditionDto.setDateTo(LocalDateTime.now());
        }

        if(searchFeedConditionDto.getSearchKeyword() != null) {
            searchFeedConditionDto.setSearchKeyword(searchFeedConditionDto.getSearchKeyword().replace('+', ' '));
        }

        List<FeedResultPostDto> result = feedRepository.searchFeedBy(searchFeedConditionDto, cursorPost, me);

        if(me != null) {
            for (FeedResultPostDto dto : result) {
                if (dto.getIsAnonymous()) {
                    dto.setUser(null);
                }
                dto.setIsBookmarked(bookmarkRepository.existsByUserAndPostId(me, dto.getPostId()));
                dto.setIsLiked(postLikeRepository.existsByUserAndPostId(me, dto.getPostId()));
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
            cursorPost = postRepository.findById(cursorId).orElseThrow(InvalidAccessTokenException::new);
        }

        List<FeedResultPostDto> result = feedRepository.searchUserPageBy(searchFeedConditionDto, owner, me, cursorPost);

        if(me != null) {
            for (FeedResultPostDto dto : result) {
                dto.setIsBookmarked(bookmarkRepository.existsByUserAndPostId(me, dto.getPostId()));
                dto.setIsLiked(postLikeRepository.existsByUserAndPostId(me, dto.getPostId()));
            }
        }

        return result;

    }

        }

        if(me != null) {
            for (FeedResultPostDto dto : result) {
                dto.setIsBookmarked(bookmarkRepository.existsByUserAndPostId(me, dto.getPostId()));
                dto.setIsLiked(postLikeRepository.existsByUserAndPostId(me, dto.getPostId()));
            }
        }

        return result;

    }

    public List<SearchUserResponseDto> searchUser(String username, String searchKeyword, Long cursorId, Long size) {

        User me = null;
        if (username != null) {
            me = userRepository.findByUsername(username).orElseThrow(InvalidAccessTokenException::new);
        }

        User cursorUser = null;
        if (cursorId != null && cursorId != 0L) {
            cursorUser = userRepository.findById(cursorId).orElseThrow(InvalidCursorException::new);
        }

        List<SearchUserResponseDto> result = userRepository.searchUserBy(me, searchKeyword, cursorUser, size);

        if(me != null) {
            for (SearchUserResponseDto dto : result) {
                if(followRepository.existsFollowsByFollowedIdAndFollowingId(me.getId(), dto.getUserId()))
                dto.setIsFollowing(true);
            }
        }

        return result;
    }
}
