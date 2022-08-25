package com.dope.breaking.domain.media;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum UploadType {

    ORIGNAL_PROFILE_IMG("originalProfileImgDirName" , "/originalProfileImg"),

    COMPRESSED_PROFILE_IMG("compressedProfileImgDirName", "/compressedProfileImg"),
    ORIGINAL_POST_MEDIA("originalPostMediaDirName","/originalPostMedia"),
    THUMBNAIL_POST_MEDIA("thumbnailDirName" ,"/thumbnailPostMedia"),

    POST_MEDIA_DOWNLOAD("postMediaDownload", "/postMediaDownload");

    private final String uploadDivision;
    private final String DirName;

}
