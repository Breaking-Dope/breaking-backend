package com.dope.breaking.service;

import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.post.FeedResultPostDto;
import com.dope.breaking.dto.post.SearchFeedConditionDto;
import com.dope.breaking.dto.user.ProfileInformationResponseDto;
import com.dope.breaking.dto.user.SearchUserResponseDto;
import com.dope.breaking.exception.auth.InvalidAccessTokenException;
import com.dope.breaking.exception.pagination.InvalidCursorException;
import com.dope.breaking.exception.user.LoginRequireException;
import com.dope.breaking.exception.user.NoPermissionException;
import com.dope.breaking.exception.user.NoSuchUserException;
import com.dope.breaking.repository.*;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.dope.breaking.domain.user.QFollow.follow;

@Service
@RequiredArgsConstructor
public class SearchFeedService {

    private final FeedRepository feedRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final BookmarkRepository bookmarkRepository;
    private final PostLikeRepository postLikeRepository;
    private final FollowRepository followRepository;

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

        List<FeedResultPostDto> result;

        if(searchFeedConditionDto.getSearchHashtag() != null) {
            result = feedRepository.searchFeedByHashtag(searchFeedConditionDto, cursorPost, me);
        } else {
            result = feedRepository.searchFeedBy(searchFeedConditionDto, cursorPost, me);
        }

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

        List<FeedResultPostDto> result;

        switch (searchFeedConditionDto.getUserPageFeedOption()) {
            case BOOKMARK:
                result = feedRepository.searchUserPageByBookmark(searchFeedConditionDto, owner, me, cursorPost);
            case BUY:
                result = feedRepository.searchUserPageByPurchase(searchFeedConditionDto, owner, me, cursorPost);
            case WRITE:
            default:
                result = feedRepository.searchUserPageBy(searchFeedConditionDto, owner, me, cursorPost);

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
