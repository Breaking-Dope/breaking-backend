package com.dope.breaking.domain.post;

import com.dope.breaking.domain.baseTimeEntity.BaseTimeEntity;
import com.dope.breaking.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Mission extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="MISSION_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name="USER_ID")
    private User user;

    private String title;

    private String content;

    private Long viewCount = 0L;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Embedded
    private Location location;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Post> postList = new ArrayList<>();

    @Builder
    public Mission(User user, String title, String content, LocalDateTime startTime, LocalDateTime endTime, Location location){
        this.user = user;
        this.title = title;
        this.content = content;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
    }

    public void increaseViewCount() {
        viewCount++;
    }

}
