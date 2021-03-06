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

    private final String MAIN_DIR_NAME = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF";
    private final String SUB_DIR_NAME = "/static";


    private final String basicProfileDir = "profile.png";

    public String getBasicProfileDir() {
        return basicProfileDir;
    }

    public String getMAIN_DIR_NAME() {
        return MAIN_DIR_NAME;
    }


    public List<String> uploadMedias(List<MultipartFile> medias, UploadType uploadType) {

        List<String> fileNameList = new ArrayList<String>();

        try {
            File folder = new File(MAIN_DIR_NAME + SUB_DIR_NAME +uploadType.getDirName());

            if (!folder.exists()) {
                folder.mkdirs();
            }

            for (MultipartFile media : medias) {
                String fileName = media.getOriginalFilename();
                String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
                String generateFileName = null;
                MediaType mediaType = findMediaType(extension); //?????? ???????????? ?????????.
                if (mediaType.equals(MediaType.PHOTO)) {
                    generateFileName = UUID.randomUUID().toString() + "." + extension;
                } else {
                    generateFileName = UUID.randomUUID().toString() + ".mp4";
                }
                String mediaURL = SUB_DIR_NAME + uploadType.getDirName() + File.separator + generateFileName;
                String destinationPath = MAIN_DIR_NAME + mediaURL;
                File destination = new File(destinationPath);
                media.transferTo(destination);

                fileNameList.add(mediaURL);
            }
        } catch (CustomInternalErrorException | IOException e) {

            log.error("error: " + e.getMessage());
            throw new CustomInternalErrorException(e.getMessage());

        }
        return fileNameList;

    }


    public MediaType findMediaType(String fileName) {

        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        List<String> videoExtension = Arrays.asList("mp4", "mov", "mpg", "mpeg", "rm", "vob");

        if (videoExtension.contains(extension)) {

            return MediaType.VIDEO;

        } else {

            return MediaType.PHOTO;

        }
    }


    @Transactional //?????????, ?????? ??? ?????? ?????? ?????? ??????.
    public void createMediaEntities(List<String> fileNameList, Post post) {

        for (String fileName : fileNameList) {
            MediaType mediaType = findMediaType(fileName);
            Media media = new Media(post, mediaType, fileName);
            mediaRepository.save(media);
        }
    }


    public void deleteMedias(List<String> fileNames) {
        for (String fileName : fileNames) {
            File savedFile = new File(MAIN_DIR_NAME + fileName);
            if (savedFile.exists()) {
                if (savedFile.delete()) {
                    log.info("???????????? ??????. filename : {}", fileName);
                } else {
                    log.info("???????????? ??????. filename : {}", fileName);
                }
            } else {
                log.info("????????? ???????????? ????????????. filename : {}", fileName);
            }
        }
    }


    //????????? ?????? ?????? ?????????
    public void deleteThumbnailImg(String fileName) {
        File savedThumb = new File(MAIN_DIR_NAME + fileName);
        if (savedThumb.exists()) {
            if (savedThumb.delete()) {
                log.info("???????????? ??????. filename : {}", fileName);
            } else {
                log.info("???????????? ??????. filename : {}", fileName);
            }
        } else {
            log.info("????????? ???????????? ????????????. filename : {}", fileName);
        }
    }


    public String compressImage(String originalProfileImgURL) {

        String fullPath = MAIN_DIR_NAME + originalProfileImgURL;
        File originalMediaPath = new File(fullPath);
        String compressedProfileImgFolderName = SUB_DIR_NAME + UploadType.COMPRESSED_PROFILE_IMG.getDirName();
        File compressedProfileImgFolder = new File(MAIN_DIR_NAME + compressedProfileImgFolderName);

        if(!compressedProfileImgFolder.exists()){compressedProfileImgFolder.mkdirs();}

        String extension = originalProfileImgURL.substring(originalProfileImgURL.lastIndexOf(".") + 1);
        MediaType mediaType = findMediaType(extension);

        String compressedImageURL = compressedProfileImgFolderName + File.separator + UUID.randomUUID().toString() + "." + extension;
        File destination = new File(MAIN_DIR_NAME + compressedImageURL);

        BufferedImage oImage = null;
        try {
            oImage = ImageIO.read(originalMediaPath);
        } catch (IOException e) {
            throw new CustomInternalErrorException(e.getMessage());
        }

        int width = oImage.getWidth();
        int height = oImage.getHeight();

        if (Math.min(width,height)>=500){
            if(width<height){
                height = Math.round(height*500/width);
                width = 500;
            }
            else{
                width = Math.round(width*500/height);
                height = 500;
            }
        }
        
        BufferedImage tImage = new BufferedImage(width,height, BufferedImage.TYPE_3BYTE_BGR);

        Graphics2D graphic = tImage.createGraphics();
        Image image = oImage.getScaledInstance(width,height,Image.SCALE_SMOOTH);
        graphic.drawImage(image,0,0,width,height,null);
        graphic.dispose();
        try {
            ImageIO.write(tImage,extension,destination);
        } catch (IOException e) {
            throw new CustomInternalErrorException(e.getMessage());
        }

        return compressedImageURL;
    }

    public String makeThumbnail(String mediaUrl) throws IOException, JCodecException { //?????? ????????? ?????????.
        String FullPath = MAIN_DIR_NAME + mediaUrl;
        File originalMediaPath = new File(FullPath); //?????? ?????? ????????? ??????
        File thumbnailFolder = new File(MAIN_DIR_NAME + SUB_DIR_NAME + File.separator + UploadType.THUMBNAIL_POST_MEDIA.getDirName());

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
                String thumbDestinationPath =  SUB_DIR_NAME + UploadType.THUMBNAIL_POST_MEDIA.getDirName() + File.separator  + generateThumbFileName;
                File thumbDestination = new File(MAIN_DIR_NAME + thumbDestinationPath);
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
                String thumbDestinationPath = SUB_DIR_NAME +UploadType.THUMBNAIL_POST_MEDIA.getDirName() + File.separator + generateThumbFileName;
                File thumdestination = new File(MAIN_DIR_NAME + thumbDestinationPath);
                BufferedImage tImage = new BufferedImage(tWidth, tHeight, BufferedImage.TYPE_3BYTE_BGR); // ??????????????????
                Graphics2D graphic = tImage.createGraphics();
                Image image = oImage.getScaledInstance(tWidth, tHeight, Image.SCALE_SMOOTH);
                graphic.drawImage(image, 0, 0, tWidth, tHeight, null);
                graphic.dispose();
                ImageIO.write(tImage, "png", thumdestination);

                return thumbDestinationPath;
            }
        } catch (JCodecException| IllegalArgumentException e) {
            e.getMessage();
            log.info("????????? ????????? ?????? ??? ????????????.");
            return null;
        }
    }


    public ResponseEntity<FileSystemResource> responseMediaFile(String fileName) throws IOException {

        String directory = MAIN_DIR_NAME + "/" + fileName;
        FileSystemResource fsr = new FileSystemResource(directory);
        HttpHeaders header = new HttpHeaders();
        Path filePath = Paths.get(fileName);
        header.add("Content-Type", Files.probeContentType(filePath));

        return new ResponseEntity<FileSystemResource>(fsr, header, HttpStatus.OK);

    }


}
