package com.chat.ChatApplication.service.impl;

import com.chat.ChatApplication.service.CloudinaryService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Override
    public String uploadImage(MultipartFile file) {

        try {

            Map<?, ?> uploadResult = cloudinary.uploader().upload(

                    file.getBytes(),

                    ObjectUtils.asMap(
                            "folder",
                            "chat-app/profile-images"
                    )

            );

            return uploadResult.get("secure_url").toString();

        }

        catch (Exception e) {

            throw new RuntimeException("Image upload failed");

        }

    }

}