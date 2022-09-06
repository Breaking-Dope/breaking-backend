package com.dope.breaking.domain.post;

import com.dope.breaking.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class Mission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="MISSION_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name="USER_ID")
    private User user;

    private String title;

    private String content;

    private Long viewCount;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Embedded
    private Location location;

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
