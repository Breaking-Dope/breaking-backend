package com.dope.breaking.service;

import com.dope.breaking.domain.media.Media;
import com.dope.breaking.domain.media.MediaType;
import com.dope.breaking.domain.post.Post;
import com.dope.breaking.repository.MediaRepository;
import com.dope.breaking.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnailator;
import org.jcodec.api.FrameGrab;

import org.jcodec.common.model.Picture;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.jcodec.scale.AWTUtil;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MediaService {

    private final MediaRepository mediaRepository;

    private final PostRepository postRepository;

    //디렉토리는 추후 AWS내의 디렉토리로 변경
    //private final String dirName = "/Users/gimmin-u/Desktop/testImgFolder";

    //Martin0o0 dir
    private final String dirName = System.getProperty("user.dir") + "/files";
    private final String basicProfileDir = "profile.png";

    public String getBasicProfileDir() {
        return basicProfileDir;
    }

    public String getDirName() {
        return dirName;
    }

    private final String thumbDirName = System.getProperty("user.dir") + "/thumb";

    public List<String> uploadMedias(List<MultipartFile> medias) throws Exception {

        List<String> fileNameList = new ArrayList<String>();

        try {

            File folder = new File(dirName);

            if (!folder.exists()) {
                folder.mkdirs();
            }

            for (MultipartFile media : medias) {

                String fileName = media.getOriginalFilename();
                String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
                String generatedFileName = UUID.randomUUID().toString() + "." + extension;

                fileNameList.add(generatedFileName);

                File destination = new File(dirName + File.separator + generatedFileName);
                media.transferTo(destination);

            }
        } catch (Exception e) {

            log.error("error: " + e.getMessage());

        } finally {

            return fileNameList;

        }

    }


    public Map<String, List<String>> uploadMediaAndThumbnail(List<MultipartFile> files, int index) throws Exception {

        Map<String, List<String>> mediaNameList = new LinkedHashMap<>();
        List<String> list = new LinkedList<>();
        String generatedThumFileName = new String();
        try {
            File folder = new File(dirName);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            for (int i = 0; i < files.size(); i++) {
                String fileName = files.get(i).getOriginalFilename();
                String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
                List<String> videoExtension = Arrays.asList("mp4", "mov", "mpg", "mpeg", "gif", "rm", "vob");
                String generatedFileName = new String();
                if (!videoExtension.contains(extension)) {
                    generatedFileName = UUID.randomUUID().toString() + "." + extension;
                } else {
                    generatedFileName = UUID.randomUUID().toString() + ".mp4";
                }
                list.add(generatedFileName);
                File destination = new File(dirName + File.separator + generatedFileName);
                files.get(i).transferTo(destination);
                if (index == i) {
                    File thumfolder = new File(thumbDirName);
                    if (!thumfolder.exists()) {
                        thumfolder.mkdirs();
                    }
                    if (!videoExtension.contains(extension)) {
                        generatedThumFileName = "s_" + UUID.randomUUID().toString() + "." + extension;
                        File thumdestination = new File(thumbDirName + File.separator + generatedThumFileName);
                        mediaNameList.put("thumbnail", List.of(generatedThumFileName));
                        Thumbnailator.createThumbnail(destination, thumdestination, 500, 500);
                    } else {
                        Picture frame = FrameGrab.getFrameFromFile(destination, 0);
                        BufferedImage img = AWTUtil.toBufferedImage(frame);
                        generatedThumFileName = "s_" + UUID.randomUUID().toString() + ".png";
                        mediaNameList.put("thumbnail", Arrays.asList(generatedThumFileName));
                        File thumdestination = new File(thumbDirName + File.separator + generatedThumFileName);
                        ImageIO.write(img, "png", thumdestination);
                        Thumbnailator.createThumbnail(thumdestination, thumdestination, 500, 500);
                    }
                }
            }
            mediaNameList.put("mediaList", list);
        } catch (Exception e) {
            deletePostMedias(list);
            DeleteThumbnailImg(generatedThumFileName);
            log.error("error: " + e.getMessage());
            throw e;

        } finally {
            return mediaNameList;

        }
    }


    public MediaType findMediaType(String fileName) {

        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        List<String> videoExtension = Arrays.asList("mp4", "mov", "mpg", "mpeg", "gif", "rm", "vob");

        if (videoExtension.contains(extension)) {

            return MediaType.VIDEO;

        } else {

            return MediaType.PHOTO;

        }

    }


    @Transactional //혹여나, 실패 시 자동 롤백 하기 위해.
    public void createMediaEntities(List<String> fileNameList, Post post) {

        for (String fileName : fileNameList) {
            MediaType mediaType = findMediaType(fileName);
            Media media = new Media(post, mediaType, fileName);
            mediaRepository.save(media);
        }
    }


    @Transactional
    public void modifyMediaEnities(List<String> preFileNameList, List<String> newFileNameList, Long postId) {
        Post post = postRepository.findById(postId).get();

        for (String m : preFileNameList) {
            Media media = mediaRepository.findByPostIdAndMediaURL(postId, m);
            mediaRepository.delete(media);
        }

        for (String fileName : newFileNameList) {
            MediaType mediaType = findMediaType(fileName);
            Media media = new Media(post, mediaType, fileName);
            mediaRepository.save(media);
        }
    }

    public void deletePostMedias(List<String> fileNames) {
        for (String fileName : fileNames) {
            File savedFile = new File(dirName + File.separator + fileName);
            if (savedFile.exists()) {
                if (savedFile.delete()) {
                    log.info("파일삭제 성공. filename : {}", fileName);
                } else {
                    log.info("파일삭제 실패. filename : {}", fileName);
                }
            } else {
                log.info("파일이 존재하지 않습니다. filename : {}", fileName);
            }
        }
    }


    //썸네일 사진 삭제 메서드
    public void DeleteThumbnailImg(String fileName) {
        File savedThumb = new File(thumbDirName + File.separator + fileName);
        if (savedThumb.exists()) {
            if (savedThumb.delete()) {
                log.info("파일삭제 성공. filename : {}", fileName);
            } else {
                log.info("파일삭제 실패. filename : {}", fileName);
            }
        } else {
            log.info("파일이 존재하지 않습니다. filename : {}", fileName);
        }
    }

    public List<String> preMediaURL(Long postId) {
        List<Media> list = mediaRepository.findAllByPostId(postId);
        List<String> preMediaURL = new LinkedList<>();
        for (Media m : list) {
            preMediaURL.add(m.getMediaURL());
        }
        return preMediaURL;
    }


    public ResponseEntity<FileSystemResource> responseMediaFile(String fileName) throws IOException {

        String directory = dirName + "/" + fileName;
        FileSystemResource fsr = new FileSystemResource(directory);
        HttpHeaders header = new HttpHeaders();
        Path filePath = Paths.get(fileName);
        header.add("Content-Type", Files.probeContentType(filePath));

        return new ResponseEntity<FileSystemResource>(fsr, header, HttpStatus.OK);

    }

}
