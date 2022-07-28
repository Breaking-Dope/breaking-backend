package com.dope.breaking.service;


import com.dope.breaking.domain.comment.Comment;
import com.dope.breaking.domain.hashtag.Hashtag;
import com.dope.breaking.domain.hashtag.HashtagType;
import com.dope.breaking.domain.post.Post;
import com.dope.breaking.exception.comment.NoSuchCommentException;
import com.dope.breaking.exception.post.NoSuchPostException;
import com.dope.breaking.repository.CommentRepository;
import com.dope.breaking.repository.HashtagRepository;
import com.dope.breaking.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class HashtagService {

    private final HashtagRepository hashtagRepository;

    private final PostRepository postRepository;

    private final CommentRepository commentRepository;


    @Transactional
    public void saveHashtag(List<String> postCommentHashtags, Long postOrCommentId, HashtagType hashtagType){

        if (hashtagType == HashtagType.POST) {

            Post post = postRepository.findById(postOrCommentId).orElseThrow(NoSuchPostException::new);

            if (postCommentHashtags != null) {
                for (String hashtag : postCommentHashtags) {

                    Hashtag postCommentHashtag = Hashtag.builder()
                            .content(hashtag)
                            .post(post)
                            .comment(null)
                            .hashtagType(hashtagType)
                            .build();
                    hashtagRepository.save(postCommentHashtag);

                }
            }
        }

        else if (hashtagType == HashtagType.COMMENT){

            Comment comment = commentRepository.findById(postOrCommentId).orElseThrow(NoSuchCommentException::new);

            if (postCommentHashtags != null) {
                for (String hashtag : postCommentHashtags) {

                    Hashtag postCommentHashtag = Hashtag.builder()
                            .content(hashtag)
                            .post(null)
                            .comment(comment)
                            .hashtagType(hashtagType)
                            .build();
                    hashtagRepository.save(postCommentHashtag);

                }
            }
        }
    }

    @Transactional
    public void updateHashtag(List<String> postCommentHashtags, Long postOrCommentId, HashtagType hashtagType){

        if (hashtagType == HashtagType.POST) {
            Post post = postRepository.findById(postOrCommentId).orElseThrow(NoSuchPostException::new);
            hashtagRepository.deleteAllByPost(post);
        }

        else{
            Comment comment = commentRepository.findById(postOrCommentId).orElseThrow(NoSuchCommentException::new);
            hashtagRepository.deleteAllByComment(comment);
        }

        saveHashtag(postCommentHashtags, postOrCommentId, hashtagType);

    }

}
