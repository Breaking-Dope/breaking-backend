package com.dope.breaking.repository;

import com.dope.breaking.domain.financial.Purchase;
import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.user.Follow;
import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.user.ForListInfoResponseDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PurchaseRepositoryTest {

    @Autowired UserRepository userRepository;
    @Autowired PurchaseRepository purchaseRepository;
    @Autowired FollowRepository followRepository;
    @Autowired PostRepository postRepository;

    @DisplayName("유저가 본인 제보의 구매자를 조회할 시, 정상적으로 조회가 진행된다.")
    @Test
    void viewPurchaseListBySelf(){

        //Given
        User user = new User();
        user.setRequestFields("img","img","nickname","01012345678","email","hi","there","username", Role.USER);
        userRepository.save(user);

        Post post = new Post();
        post.setUser(user);
        postRepository.save(post);

        User buyer1 = new User();
        User buyer2 = new User();
        userRepository.save(buyer1);
        userRepository.save(buyer2);

        Purchase purchase1 = new Purchase(buyer1, post,1000);
        Purchase purchase2 = new Purchase(buyer2, post,1000);
        purchaseRepository.save(purchase1);
        purchaseRepository.save(purchase2);

        Follow follow = new Follow(user,buyer1);
        followRepository.save(follow);

        //When
        List<ForListInfoResponseDto> purchaseUserList = purchaseRepository.purchaseList(user,post,0L,10);

        //Then
        Assertions.assertTrue(purchaseUserList.get(0).isFollowing());
        Assertions.assertFalse(purchaseUserList.get(1).isFollowing());

    }

}