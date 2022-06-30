package com.dope.breaking.apiTests;

import com.dope.breaking.service.MediaService;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MediaUploaderTest {

    private final MediaService mediaService;

    @PostMapping("/mediaUploaderTest")
    public void testResponse(@RequestPart Request request, @RequestPart List<MultipartFile> mediaFiles) throws Exception {
        List<String> fileNameList = mediaService.uploadMedias(mediaFiles);

        for (String s : fileNameList) {
            System.out.println(s);
        }
    }

    @Getter
    @NoArgsConstructor
    public static class Request{

        private String message;

    }
}
