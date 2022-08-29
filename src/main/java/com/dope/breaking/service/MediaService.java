package com.dope.breaking.service;

import com.dope.breaking.domain.media.Media;
import com.dope.breaking.domain.media.MediaType;
import com.dope.breaking.domain.media.UploadType;
import com.dope.breaking.domain.post.Post;
import com.dope.breaking.exception.CustomInternalErrorException;
import com.dope.breaking.repository.MediaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;


import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class MediaService {

    private final MediaRepository mediaRepository;

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
            File folder = new File(MAIN_DIR_NAME + SUB_DIR_NAME + uploadType.getDirName());

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


    @Transactional //혹여나, 실패 시 자동 롤백 하기 위해.
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
        File savedThumb = new File(MAIN_DIR_NAME + fileName);
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

    public void deleteWatermarkNickname(String fileName) {
        File nicknameTextImagePath = new File(fileName);
        if (nicknameTextImagePath.exists()) {
            if (nicknameTextImagePath.delete()) {
                log.info("파일삭제 성공. filename : {}", nicknameTextImagePath);
            } else {
                log.info("파일삭제 실패. filename : {}", nicknameTextImagePath);
            }
        }
    }


    public String compressImage(String originalProfileImgURL) {

        String fullPath = MAIN_DIR_NAME + originalProfileImgURL;
        File originalMediaPath = new File(fullPath);
        String compressedProfileImgFolderName = SUB_DIR_NAME + UploadType.COMPRESSED_PROFILE_IMG.getDirName();
        File compressedProfileImgFolder = new File(MAIN_DIR_NAME + compressedProfileImgFolderName);

        if (!compressedProfileImgFolder.exists()) {
            compressedProfileImgFolder.mkdirs();
        }

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

        if (Math.min(width, height) >= 500) {
            if (width < height) {
                height = Math.round(height * 500 / width);
                width = 500;
            } else {
                width = Math.round(width * 500 / height);
                height = 500;
            }
        }

        BufferedImage tImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);

        Graphics2D graphic = tImage.createGraphics();
        Image image = oImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        graphic.drawImage(image, 0, 0, width, height, null);
        graphic.dispose();
        try {
            ImageIO.write(tImage, extension, destination);
        } catch (IOException e) {
            throw new CustomInternalErrorException(e.getMessage());
        }

        return compressedImageURL;
    }


    public List<String> makeWatermarkMedia(List<String> mediaURLList, String watermarkImageURL, UploadType uploadType) throws IOException {


        List<String> watermarkMedia = new LinkedList<>();

        File folder = new File(MAIN_DIR_NAME + SUB_DIR_NAME + File.separator + uploadType.getDirName());

        //폴더가 존재하는지 확인 -> 없다면 생성
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String watermarkDestinationPath = null;

        BufferedImage nicknameTextImage = ImageIO.read(new File(watermarkImageURL));


        for (String mediaURL : mediaURLList) {

            String extension = mediaURL.substring(mediaURL.lastIndexOf(".") + 1);
            MediaType mediaType = findMediaType(extension);


            String[] pathArray = mediaURL.split("/");

            String FullPath = MAIN_DIR_NAME + mediaURL;
            File originalMediaPath = new File(FullPath); //전체 주소 경로를 받음
            try {
                if (mediaType.equals(MediaType.PHOTO)) {

                    String generateWatermarkName = "w_" + pathArray[pathArray.length - 1]; //w_ 접두사로 이미지파일 생성
                    watermarkDestinationPath = SUB_DIR_NAME + UploadType.ORIGINAL_POST_MEDIA.getDirName() + File.separator + generateWatermarkName;
                    File thumbDestination = new File(MAIN_DIR_NAME + watermarkDestinationPath);
                    BufferedImage oImage = ImageIO.read(originalMediaPath);
                    int originalHeight = oImage.getHeight();
                    int originalWidth = oImage.getWidth();
                    Thumbnails.of(oImage)
                            .size(originalWidth, originalHeight)
                            .watermark(Positions.BOTTOM_RIGHT, nicknameTextImage, 1f)
                            .outputQuality(1.0f)
                            .toFile(thumbDestination);


                    watermarkMedia.add(watermarkDestinationPath);
                } else {
                    //MacOS
                    FFmpeg fFmpeg = new FFmpeg("/usr/local/bin/ffmpeg");
                    FFprobe fFprobe = new FFprobe("/usr/local/bin/ffprobe");

                    //UbuntuOS
//                FFmpeg fFmpeg = new FFmpeg("/usr/bin/ffmpeg");
//                FFprobe fFprobe = new FFprobe("/usr/bin/ffprobe");


                    String videofile = originalMediaPath.getPath();
                    String generateWatermarkName = "w_" + pathArray[pathArray.length - 1];
                    watermarkDestinationPath = SUB_DIR_NAME + UploadType.ORIGINAL_POST_MEDIA.getDirName() + File.separator + generateWatermarkName;
                    FFmpegBuilder fFmpegBuilder = new FFmpegBuilder()
                            .setInput(videofile)
                            .addInput(watermarkImageURL)
                            .addExtraArgs("-filter_complex", "overlay=x=main_w-overlay_w-(main_w*0.01):y=main_h-overlay_h-(main_h*0.01)")
                            .addOutput(MAIN_DIR_NAME + watermarkDestinationPath)
                            .addExtraArgs("-preset", "ultrafast")
                            .done();

                    FFmpegExecutor executor = new FFmpegExecutor(fFmpeg, fFprobe);
                    executor.createJob(fFmpegBuilder).run();

                    watermarkMedia.add(watermarkDestinationPath);
                }
            } catch (IllegalArgumentException e) {
                e.getMessage();
                log.info("미디어 포맷을 읽을 수 없습니다.");
                return null;
            }
        }

        return watermarkMedia;

    }


    public String makeThumbnail(String mediaUrl, String watermarkImageURL) throws IOException { //파일 주소를 받는다.
        String FullPath = MAIN_DIR_NAME + mediaUrl;
        File originalMediaPath = new File(FullPath); //전체 주소 경로를 받음
        File thumbnailFolder = new File(MAIN_DIR_NAME + SUB_DIR_NAME + File.separator + UploadType.THUMBNAIL_POST_MEDIA.getDirName());

        if (!thumbnailFolder.exists()) {
            thumbnailFolder.mkdirs();
        }
        String generateThumbFileName = null;

        String extension = mediaUrl.substring(mediaUrl.lastIndexOf(".") + 1);
        MediaType mediaType = findMediaType(extension);

        BufferedImage nicknameTextImage = ImageIO.read(new File(watermarkImageURL));


        try {
            if (mediaType.equals(MediaType.PHOTO)) {

                generateThumbFileName = "s_" + UUID.randomUUID().toString() + "." + extension;
                String thumbDestinationPath = SUB_DIR_NAME + UploadType.THUMBNAIL_POST_MEDIA.getDirName() + File.separator + generateThumbFileName;
                File thumbDestination = new File(MAIN_DIR_NAME + thumbDestinationPath);
                BufferedImage oImage = ImageIO.read(originalMediaPath);

                Thumbnails.of(oImage)
                        .size(400, 300)
                        .watermark(Positions.BOTTOM_RIGHT, nicknameTextImage, 0.5f)
                        .outputQuality(1.0f)
                        .toFile(thumbDestination);

                return thumbDestinationPath;
            } else {
                //MacOS
                FFmpeg fFmpeg = new FFmpeg("/usr/local/bin/ffmpeg");
                FFprobe fFprobe = new FFprobe("/usr/local/bin/ffprobe");

                //UbuntuOS
//                FFmpeg fFmpeg = new FFmpeg("/usr/bin/ffmpeg");
//                FFprobe fFprobe = new FFprobe("/usr/bin/ffprobe");


                String videofile = originalMediaPath.getPath();
                generateThumbFileName = "s_" + UUID.randomUUID().toString() + ".png";
                String thumbDestinationPath = SUB_DIR_NAME + UploadType.THUMBNAIL_POST_MEDIA.getDirName() + File.separator + generateThumbFileName;
                FFmpegBuilder fFmpegBuilder = new FFmpegBuilder()
                        .setInput(videofile)
                        .addOutput(MAIN_DIR_NAME + thumbDestinationPath)
                        .addExtraArgs("-ss", "00:00:01")
                        .addExtraArgs("-preset", "ultrafast")
                        .setFrames(1)
                        .done();

                FFmpegExecutor executor = new FFmpegExecutor(fFmpeg, fFprobe);
                executor.createJob(fFmpegBuilder).run();

                File thumbDestination = new File(MAIN_DIR_NAME + thumbDestinationPath);


                BufferedImage oImage = ImageIO.read(thumbDestination);

                Thumbnails.of(oImage)
                        .size(400, 300)
                        .watermark(Positions.BOTTOM_RIGHT, nicknameTextImage, 1.0f)
                        .outputQuality(1.0f)
                        .toFile(thumbDestination);


                return thumbDestinationPath;
            }
        } catch (IllegalArgumentException e) {
            e.getMessage();
            log.info("미디어 포맷을 읽을 수 없습니다.");
            return null;
        }
    }


    public String makeWatermarkNickname(String nickname) throws IOException {
        try {
            //워터마크 이미지 생성
            String nicknameText = nickname != null ? "@" + nickname : "@Breaking";
            File nicknameTextImagePath = new File(MAIN_DIR_NAME + SUB_DIR_NAME + File.separator + UUID.randomUUID() + ".png");
            Font font = new Font(null, Font.PLAIN, 50);
            FontRenderContext frc = new FontRenderContext(null, true, true);
            Rectangle2D bounds = font.getStringBounds(nicknameText, frc);
            int backgroundWidth = (int) bounds.getWidth();
            int backgroundHeight = (int) bounds.getHeight();
            BufferedImage nicknameTextImage = new BufferedImage(backgroundWidth, backgroundHeight, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2D = nicknameTextImage.createGraphics();
            g2D.setColor(Color.WHITE);
            g2D.fillRect(0, 0, backgroundWidth, backgroundHeight);
            g2D.setColor(Color.BLACK);
            g2D.setFont(font);
            g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2D.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            g2D.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
            g2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

            g2D.drawString(nicknameText, (float) bounds.getX(), (float) -bounds.getY());
            g2D.dispose();

            ImageIO.write(nicknameTextImage, "PNG", nicknameTextImagePath);
            return nicknameTextImagePath.getPath();
        } catch (IOException e) {
            log.info(e.getMessage());
        }
        throw new CustomInternalErrorException("워터마크를 생성할 수 없습니다.");
    }


    public ResponseEntity<FileSystemResource> responseMediaFile(String fileName) throws IOException {

        String directory = MAIN_DIR_NAME + SUB_DIR_NAME + UploadType.POST_MEDIA_DOWNLOAD.getDirName() + File.separator + fileName;
        FileSystemResource fsr = new FileSystemResource(directory);
        HttpHeaders header = new HttpHeaders();
        Path filePath = Paths.get(fileName);
        header.add("Content-Type", Files.probeContentType(filePath));
        header.setContentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM);
        header.add("Content-Disposition", "attachment; filename=" + fsr.getFilename());

        return new ResponseEntity<FileSystemResource>(fsr, header, HttpStatus.OK);

    }


    public void responseAllMediaFile(List<String> mediaURL, HttpServletResponse httpServletResponse) throws IOException {

        httpServletResponse.setContentType("application/zip");
        httpServletResponse.setHeader("Content-Disposition", "attachment; filename=download.zip");


        for (String fileName : mediaURL) {
            ZipOutputStream zipOutputStream = new ZipOutputStream(httpServletResponse.getOutputStream());
            FileSystemResource fileSystemResource = new FileSystemResource(MAIN_DIR_NAME + SUB_DIR_NAME + UploadType.POST_MEDIA_DOWNLOAD.getDirName() + File.separator + fileName);
            ZipEntry zipEntry = new ZipEntry(fileSystemResource.getFilename());
            zipEntry.setSize(fileSystemResource.contentLength());
            zipEntry.setTime(System.currentTimeMillis());

            zipOutputStream.putNextEntry(zipEntry);

            StreamUtils.copy(fileSystemResource.getInputStream(), zipOutputStream);
            zipOutputStream.closeEntry();
            zipOutputStream.finish();

        }
    }

}
