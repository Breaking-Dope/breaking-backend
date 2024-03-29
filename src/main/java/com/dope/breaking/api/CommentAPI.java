package com.dope.breaking.api;

import com.dope.breaking.dto.comment.CommentRequestDto;
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
            @RequestParam(value="cursor") Long cursorId,
            @RequestParam(value="size") Long size
    ) {
        String username = null;
        if(principal != null)  {
            username = principal.getName();
        }

        SearchCommentConditionDto searchCommentConditionDto = SearchCommentConditionDto.builder()
                .targetType(CommentTargetType.POST)
                .targetId(postId)
                .size(size)
                .build();

        List<CommentResponseDto> result = commentService.getCommentList(searchCommentConditionDto, username, cursorId);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/post/{postId}/comment")
    public ResponseEntity<Void> addComment(
            Principal principal,
            @PathVariable Long postId,
            @RequestBody @Valid CommentRequestDto commentRequestDto) {
        commentService.addComment(postId, principal.getName(), commentRequestDto.getContent(), commentRequestDto.getHashtagList());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/post/comment/{commentId}/reply")
    public ResponseEntity<List<CommentResponseDto>> getReplyFromComment(
            Principal principal,
            @PathVariable Long commentId,
            @RequestParam(value="cursor") Long cursorId,
            @RequestParam(value="size") Long size) {
        String username = null;
        if(principal != null)  {
            username = principal.getName();
        }

        SearchCommentConditionDto searchCommentConditionDto = SearchCommentConditionDto.builder()
                .targetType(CommentTargetType.COMMENT)
                .targetId(commentId)
                .size(size)
                .build();

        List<CommentResponseDto> result = commentService.getCommentList(searchCommentConditionDto, username, cursorId);

        return ResponseEntity.ok().body(result);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/post/comment/{commentId}/reply")
    public ResponseEntity<Void> addReply(
            Principal principal,
            @PathVariable Long commentId,
            @RequestBody @Valid CommentRequestDto commentRequestDto) {
        commentService.addReply(commentId, principal.getName(), commentRequestDto.getContent(), commentRequestDto.getHashtagList());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping ("/post/comment/{commentId}")
    public ResponseEntity<Void> updateCommentAndReply(
            Principal principal,
            @PathVariable Long commentId,
            @RequestBody @Valid CommentRequestDto commentRequestDto) {
        commentService.updateCommentOrReply(principal.getName(), commentId, commentRequestDto.getContent(), commentRequestDto.getHashtagList());
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping ("/post/comment/{commentId}")
    public ResponseEntity<Void> deleteCommentAndReply(Principal principal, @PathVariable Long commentId){
        commentService.deleteCommentOrReply(principal.getName(), commentId);
        return ResponseEntity.ok().build();
    }
    
}
