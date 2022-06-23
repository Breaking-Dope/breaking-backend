package com.dope.breaking.domain;

import com.dope.breaking.domain.Financials.Purchase;
import com.dope.breaking.domain.Financials.Statement;
import com.dope.breaking.domain.UserRelationship.Block;
import com.dope.breaking.domain.UserRelationship.Follow;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class User {
    @Id @GeneratedValue
    @Column(name="USER_ID") //userId
    private Long id;

    @OneToMany(mappedBy = "user")
    private List<Post> postList = new ArrayList<Post>();

    //유저가 팔로잉하는 유저리스트
    @OneToMany(mappedBy = "user")
    private List<Follow> followingList = new ArrayList<Follow>();

    //유저를 팔로우하는 유저리스트
    @OneToMany(mappedBy = "following")
    private List<Follow> followerList = new ArrayList<Follow>();

    //추후 차단 기능 구현//

    //유저가 차단한 리스트
    @OneToMany(mappedBy = "user")
    private List<Block> blockingList = new ArrayList<Block>();

    //유저를 차단한 리스트
    @OneToMany(mappedBy = "blocking")
    private List<Block> blockerList = new ArrayList<Block>();

    /////////////////////////////

    //입출금 및 거래내역

    @OneToMany(mappedBy = "user")
    private List<Statement> statementList =  new ArrayList<Statement>();

    @OneToMany(mappedBy = "user")
    private List<Purchase> purchaseList = new ArrayList<Purchase>();

    /////////////////////////////

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


}
