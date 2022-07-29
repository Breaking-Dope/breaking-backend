package com.dope.breaking.domain.financial;

import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@Getter
public class Purchase {

    @Id @GeneratedValue
    @Column (name = "PURCHASE_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "USER_ID")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "POST_ID")
    private Post post;

    private int price;

    @Builder
    public Purchase(User user, Post post, int price){

        this.post = post;
        this.user = user;
        this.price = price;

    }

}
