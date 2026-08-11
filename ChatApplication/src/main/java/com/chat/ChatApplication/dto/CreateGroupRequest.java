package com.chat.ChatApplication.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateGroupRequest {

    private String groupName;

    private List<Long> memberIds;

}