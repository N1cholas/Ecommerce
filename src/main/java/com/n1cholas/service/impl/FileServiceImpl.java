package com.n1cholas.service.impl;

import com.google.common.collect.Lists;
import com.n1cholas.service.IFileService;
import com.n1cholas.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service("iFileServer")
public class FileServiceImpl implements IFileService {

    private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    //todo 没有判断file是否存在
    public String upload(MultipartFile file, String path) {
        String filename = file.getOriginalFilename();
        //扩展名
        String fileExtensionName = filename.substring(filename.lastIndexOf(".")+1);
        String uploadFilename = UUID.randomUUID().toString() + "." + fileExtensionName;
        logger.info("开始上传文件，上传文件的文件名为：{},上传路径为：{},新文件名为：{}", filename, path, uploadFilename);

        File fileDir = new File(path);
        if(!fileDir.exists()){
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }

        File targetFile = new File(path, uploadFilename);

        try {
            //文件已经上传tomcat成功
            file.transferTo(targetFile);

            //将targetFile上传到FTP服务器上
            FTPUtil.uploadFile(Lists.newArrayList(targetFile));

            //上传完成之后，删除upload下面的文件
            targetFile.delete();

        } catch (IOException e) {
            logger.error("上传文件异常", e);
            return null;
        }

        return targetFile.getName();
    }
}
