package com.dope.breaking.service;


import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.user.Bookmark;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.exception.BreakingException;
import com.dope.breaking.exception.ErrorCode;
import com.dope.breaking.exception.bookmark.AlreadyBookmarkedException;
import com.dope.breaking.exception.bookmark.AlreadyUnbookmarkedException;
import com.dope.breaking.repository.BookmarkRepository;
import com.dope.breaking.repository.PostRepository;
import com.dope.breaking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;

    private final UserRepository userRepository;

    private final PostRepository postRepository;

    @Transactional
    public ResponseEntity bookmarkPost(String username, Long postId){
        User user = userRepository.findByUsername(username).get();
        Post post = postRepository.getById(postId);
        if(bookmarkRepository.existsByUserAndPost(user, post)) throw new AlreadyBookmarkedException();
        Bookmark bookmark = Bookmark.builder()
                .post(post)
                .user(user).build();
        bookmarkRepository.save(bookmark);
        return new ResponseEntity(HttpStatus.CREATED);
    }

    @Transactional
    public ResponseEntity unbookmarkPost(String username, Long postId){
        User user = userRepository.findByUsername(username).get();
        Post post = postRepository.getById(postId);
        if(!bookmarkRepository.existsByUserAndPost(user, post)) throw new AlreadyUnbookmarkedException();
        Bookmark bookmark = Bookmark.builder()
                .post(post)
                .user(user).build();
        bookmarkRepository.save(bookmark);
        return new ResponseEntity(HttpStatus.CREATED);
    }

}
