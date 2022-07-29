package com.dope.breaking.api;

import com.dope.breaking.dto.comment.CommentRequestDto;
import com.dope.breaking.repository.CommentRepository;
import com.dope.breaking.dto.comment.CommentResponseDto;
import com.dope.breaking.dto.comment.SearchCommentConditionDto;
import com.dope.breaking.service.CommentService;
import com.dope.breaking.service.CommentTargetType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentAPI {

    private final CommentService commentService;

    @GetMapping("/post/{postId}/comment")
    public ResponseEntity<List<CommentResponseDto>> getCommentListFromPost(
            Principal principal,
            @PathVariable Long postId,
            @RequestParam(value="cursor") Long cursor,
            @RequestParam(value="size") Long size
    ) {

        String username = null;
        if(principal != null)  {
            username = principal.getName();
        }

        SearchCommentConditionDto searchCommentConditionDto = SearchCommentConditionDto.builder()
                .targetType(CommentTargetType.POST)
                .targetId(postId)
                .cursorId(cursor)
                .size(size)
                .build();

        List<CommentResponseDto> result = commentService.getCommentList(searchCommentConditionDto, postId, username);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/post/{postId}/comment")
    public ResponseEntity addComment(@PathVariable Long postId,
                                     @RequestBody @Valid CommentRequestDto commentRequestDto,
                                     Principal principal){

        commentService.addComment(postId, principal.getName(), commentRequestDto.getContent(), commentRequestDto.getHashtagList());
        return ResponseEntity.status(HttpStatus.CREATED).build();

    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/post/comment/{commentId}/reply")
    public ResponseEntity addReply(@PathVariable Long commentId,
                                   @RequestBody @Valid CommentRequestDto commentRequestDto,
                                   Principal principal){

        commentService.addReply(commentId, principal.getName(), commentRequestDto.getContent(), commentRequestDto.getHashtagList());
        return ResponseEntity.status(HttpStatus.CREATED).build();

    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping ("/post/comment/{commentId}")
    public ResponseEntity updateCommentAndReply(@PathVariable Long commentId,
                                                @RequestBody @Valid CommentRequestDto commentRequestDto,
                                                Principal principal){

        commentService.updateCommentOrReply(principal.getName(), commentId, commentRequestDto.getContent(), commentRequestDto.getHashtagList());
        return ResponseEntity.status(HttpStatus.CREATED).build();

    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping ("/post/comment/{commentId}")
    public ResponseEntity deleteCommentAndReply(@PathVariable Long commentId, Principal principal){

        commentService.deleteCommentOrReply(principal.getName(), commentId);
        return ResponseEntity.ok().build();

    }
    
}
