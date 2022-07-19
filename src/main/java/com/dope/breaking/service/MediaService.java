package com.dope.breaking.service;

import com.dope.breaking.domain.media.Media;
import com.dope.breaking.domain.media.MediaType;
import com.dope.breaking.domain.media.UploadType;
import com.dope.breaking.domain.post.Post;
import com.dope.breaking.exception.CustomInternalErrorException;
import com.dope.breaking.repository.MediaRepository;
import com.dope.breaking.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import javax.imageio.ImageIO;
import java.awt.*;
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

    private final String dirName = System.getProperty("user.dir") + "/src/main/resources/static";

    private final String basicProfileDir = "profile.png";

    public String getBasicProfileDir() {
        return basicProfileDir;
    }

    public String getDirName() {
        return dirName;
    }


    public List<String> uploadMedias(List<MultipartFile> medias, UploadType uploadType) {

        List<String> fileNameList = new ArrayList<String>();

        try {
            File folder = new File(dirName + uploadType.getDirName());

            if (!folder.exists()) {
                folder.mkdirs();
            }

            for (MultipartFile media : medias) {
                String fileName = media.getOriginalFilename();
                String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
                String generateFileName = null;
                MediaType mediaType = findMediaType(extension); //파일 확장자를 고려함.
                if (mediaType.equals(MediaType.PHOTO)) {
                    generateFileName = UUID.randomUUID().toString() + "." + extension;
                } else {
                    generateFileName = UUID.randomUUID().toString() + ".mp4";
                }
                String destinationPath = dirName + uploadType.getDirName()  + File.separator  + generateFileName;
                File destination = new File(destinationPath);
                media.transferTo(destination);
                fileNameList.add(destinationPath);
            }
        } catch (CustomInternalErrorException | IOException e) {

            log.error("error: " + e.getMessage());
            throw new CustomInternalErrorException(e.getMessage());

        }
        return fileNameList;

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
    public void modifyMediaEntities(List<String> preFileNameList, List<String> newFileNameList, Long postId) {
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

    public void deleteMedias(List<String> fileNames) {
        for (String fileName : fileNames) {
            File savedFile = new File(fileName);
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
    public void deleteThumbnailImg(String fileName) {
        File savedThumb = new File(fileName);
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

    public String makeThumbnail(String mediaUrl) throws IOException, JCodecException { //파일 주소를 받는다.
        File originalMediaPath = new File(mediaUrl); //전체 주소 경로를 받음
        File thumbnailFolder = new File(dirName + File.separator + UploadType.THUMBNAIL_POST_MEDIA.getDirName());

        if (!thumbnailFolder.exists()) {
            thumbnailFolder.mkdirs();
        }
        String generateThumbFileName = null;

        String extension = mediaUrl.substring(mediaUrl.lastIndexOf(".") + 1);
        MediaType mediaType = findMediaType(extension);

        final int tWidth = 400;
        final int tHeight = 300;

        try {
            if (mediaType.equals(MediaType.PHOTO)) {
                generateThumbFileName = "s_" + UUID.randomUUID().toString() + "." + extension;
                String thumbDestinationPath = dirName + UploadType.THUMBNAIL_POST_MEDIA.getDirName() + File.separator  + generateThumbFileName;
                File thumbDestination = new File(thumbDestinationPath);
                BufferedImage oImage = ImageIO.read(originalMediaPath);
                BufferedImage tImage = new BufferedImage(tWidth, tHeight, BufferedImage.TYPE_3BYTE_BGR);
                Graphics2D graphic = tImage.createGraphics();
                Image image = oImage.getScaledInstance(tWidth, tHeight, Image.SCALE_SMOOTH);
                graphic.drawImage(image, 0, 0, tWidth, tHeight, null);
                graphic.dispose();
                ImageIO.write(tImage, "png", thumbDestination);

                return thumbDestinationPath;
            } else {
                FrameGrab grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(originalMediaPath));
                double startSec = 1;
                grab.seekToSecondPrecise(startSec);
                Picture picture = grab.getNativeFrame();
                BufferedImage oImage = AWTUtil.toBufferedImage(picture);
                generateThumbFileName = "s_" + UUID.randomUUID().toString() + ".png";
                String thumbDestinationPath = dirName + UploadType.THUMBNAIL_POST_MEDIA.getDirName() + File.separator + generateThumbFileName;
                File thumdestination = new File(thumbDestinationPath);
                BufferedImage tImage = new BufferedImage(tWidth, tHeight, BufferedImage.TYPE_3BYTE_BGR); // 썸네일이미지
                Graphics2D graphic = tImage.createGraphics();
                Image image = oImage.getScaledInstance(tWidth, tHeight, Image.SCALE_SMOOTH);
                graphic.drawImage(image, 0, 0, tWidth, tHeight, null);
                graphic.dispose();
                ImageIO.write(tImage, "png", thumdestination);

                return thumbDestinationPath;
            }
        } catch (JCodecException| IllegalArgumentException e) {
            e.getMessage();
            log.info("미디어 포맷을 읽을 수 없습니다.");
            return null;
        }
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
