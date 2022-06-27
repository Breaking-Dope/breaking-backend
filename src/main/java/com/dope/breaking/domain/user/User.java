package com.dope.breaking.domain.user;

import com.dope.breaking.domain.financial.Purchase;
import com.dope.breaking.domain.financial.Statement;
import com.dope.breaking.domain.post.Post;
import lombok.Getter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class User {

    @Id @GeneratedValue
    @Column(name="USER_ID")
    private Long id;

    @OneToMany(mappedBy = "user")
    private List<Post> postList = new ArrayList<Post>();

    //유저가 팔로잉하는 유저리스트
    @OneToMany(mappedBy = "user")
    private List<Follow> followingList = new ArrayList<Follow>();

    //유저를 팔로우하는 유저리스트
    @OneToMany(mappedBy = "following")
    private List<Follow> followerList = new ArrayList<Follow>();

    //유저가 차단한 리스트
    @OneToMany(mappedBy = "user")
    private List<Block> blockingList = new ArrayList<Block>();

    //유저를 차단한 리스트
    @OneToMany(mappedBy = "blocking")
    private List<Block> blockerList = new ArrayList<Block>();

    //입출금 내역
    @OneToMany(mappedBy = "user")
    private List<Statement> statementList =  new ArrayList<Statement>();
    //거래 내역
    @OneToMany(mappedBy = "user")
    private List<Purchase> purchaseList = new ArrayList<Purchase>();


    @OneToMany(mappedBy = "user")
    private List<Bookmark> bookmarkList = new ArrayList<Bookmark>();

    private String firstname;

    private String lastname;

    private String nickname;

    private String statusMsg;

    private String email;

    private String phoneNumber;

    private int balance;

    private String profileImgURL;

    public void SignUp
            (String profileImgURL, String nickname, String phoneNumber, String email,
             String firstname, String lastname, String statusMsg){

        this.profileImgURL = profileImgURL;
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.firstname = firstname;
        this.lastname = lastname;
        this.statusMsg = statusMsg;

    }

}
