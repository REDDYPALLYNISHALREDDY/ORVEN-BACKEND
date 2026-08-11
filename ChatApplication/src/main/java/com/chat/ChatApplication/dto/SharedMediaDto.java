package com.chat.ChatApplication.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedMediaDto {

    private Long id;

    private String fileName;

    private String fileUrl;

    private String fileType;

}