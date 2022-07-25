package com.dope.breaking.repository;

import com.dope.breaking.domain.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post,Long> {
    Boolean existsByIdAndUserId(Long postid, Long userid);


}
