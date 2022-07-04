package com.dope.breaking.domain.baseTimeEntity;

import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@Getter
@MappedSuperclass // 상속할 경우 필드들도 칼럼으로 인식하도록 한다.
@EntityListeners(AuditingEntityListener.class) //EntityListener 등록
public abstract class BaseTimeEntity {
    @CreatedDate
    @Column(updatable = false) //생성 시점부터 수정 불가로 만들어 주자
    private LocalDateTime createdDate; //최초 생성 시간

    @LastModifiedDate
    private LocalDateTime modifiedDate; //마지막 수정 시간

}