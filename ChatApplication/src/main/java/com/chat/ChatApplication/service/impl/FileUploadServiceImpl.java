package com.chat.ChatApplication.service.impl;

import com.chat.ChatApplication.service.FileUploadService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl
        implements FileUploadService {

    private final Cloudinary cloudinary;

    @Override
    public String uploadFile(MultipartFile file) {

        try {

            System.out.println("=================================");
            System.out.println("Uploading : " + file.getOriginalFilename());
            System.out.println("Type      : " + file.getContentType());
            System.out.println("Size      : " + file.getSize());
            System.out.println("=================================");

            Map<?, ?> uploadResult = cloudinary.uploader().upload(

                    file.getBytes(),

                    ObjectUtils.asMap(

                            "resource_type", "auto",

                            "use_filename", true,

                            "unique_filename", true

                    )

            );

            System.out.println("Cloudinary Upload Success");
            System.out.println(uploadResult);

            return uploadResult.get("secure_url").toString();

        } catch (Exception e) {

            System.out.println("========== CLOUDINARY ERROR ==========");
            e.printStackTrace();
            System.out.println("Message : " + e.getMessage());
            System.out.println("======================================");

            throw new RuntimeException(
                    "File upload failed : " + e.getMessage(),
                    e
            );

        }

    }

}