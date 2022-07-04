package com.dope.breaking.dto.post;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@ToString
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PostCreateRequestDto {
    @NotNull
    private String title;
    @NotNull
    private String content;

    private int price;
    @NotNull
    private Boolean isAnonymous;
    @NotNull
    private String postType;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.SCALAR, pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime eventTime;


    @JsonProperty("location") //locationDto에 location이라는 이름으로 된, key값을 받음.
    @Valid
    private LocationDto locationDto;


    private List<String> hashtagList;
}
