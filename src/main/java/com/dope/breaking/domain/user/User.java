package com.dope.breaking.domain.user;

import com.dope.breaking.domain.financial.Purchase;
import com.dope.breaking.domain.financial.Statement;
import com.dope.breaking.domain.post.Post;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column(name="USER_ID")
    private Long id;

    @OneToMany(mappedBy = "user")
    private List<Post> postList = new ArrayList<Post>();

    //유저가 팔로잉하는 유저리스트
    @OneToMany(mappedBy = "following", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Follow> followingList = new ArrayList<Follow>();

    //유저를 팔로우하는 유저리스트
    @OneToMany(mappedBy = "followed", orphanRemoval = true)
    private List<Follow> followerList = new ArrayList<Follow>();

    //유저가 차단한 리스트
    @OneToMany(mappedBy = "user")
    private List<Block> blockingList = new ArrayList<Block>();

    //유저를 차단한 리스트
    @OneToMany(mappedBy = "blocking")
    private List<Block> blockerList = new ArrayList<Block>();

    //거래 내역
    @OneToMany(mappedBy = "user")
    private List<Purchase> purchaseList = new ArrayList<Purchase>();

    @OneToMany(mappedBy = "user")
    private List<Bookmark> bookmarkList = new ArrayList<Bookmark>();

    @Builder
    public User(String username, String password, Role role){
        this.username = username;
        this.password = password;
        this.role = role;
    }

    private String realName;

    private String username;

    private String password;

    private String nickname;

    private String statusMsg;

    private String email;

    private String phoneNumber;

    private int balance;

    private String originalProfileImgURL;

    private String compressedProfileImgURL;

    @Column(length = 1000)
    private String refreshToken;


    public void setRequestFields (String originalProfileImgURL, String compressedProfileImgURL, String nickname, String phoneNumber, String email,
             String realName, String statusMsg, String username, Role role) {

        this.originalProfileImgURL = originalProfileImgURL;
        this.compressedProfileImgURL = compressedProfileImgURL;
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.realName = realName;
        this.statusMsg = statusMsg;
        this.username = username;
        this.balance = 0;
        this.role = role;
        this.password = UUID.randomUUID().toString();
    }

    @Enumerated(EnumType.STRING)
    private Role role; //권한 종류

    public String getRoleKey(){
        return this.role.getKey();
    }

    public void updateRefreshToken(String refreshToken){
        this.refreshToken = refreshToken;
    }

    public void destroyRefreshToken(){
        this.refreshToken = null;
    }

    public void updateBalance(int amount) {this.balance += amount;}

}
