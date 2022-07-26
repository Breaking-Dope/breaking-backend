package com.dope.breaking.repository;

import com.dope.breaking.domain.financial.Purchase;
import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    int countByPost(Post post);

    boolean existsByPost(Post post);

    boolean existsByPostAndUser(Post post, User user);
}
