package com.dope.breaking.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class MediaService {

    //디렉토리는 추후 AWS내의 디렉토리로 변경
    private String dirName = "/Users/gimmin-u/Desktop/testImgFolder";

    public List<String> uploadMedias(List<MultipartFile> medias) throws Exception{

        List<String> fileNameList = new ArrayList<String>();

        try{

            File folder =  new File(dirName);

            if (!folder.exists()){
                folder.mkdirs();
            }

            for (MultipartFile media : medias){

                String fileName = media.getOriginalFilename();
                String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
                String generatedFileName = UUID.randomUUID().toString()+"."+extension;

                fileNameList.add(generatedFileName);

                File destination =  new File(dirName + File.separator + generatedFileName);
                media.transferTo(destination);

            }

        }catch(Exception e){

            log.error("error: "+e.getMessage());

        }finally{

            return fileNameList;

        }

    }

}
