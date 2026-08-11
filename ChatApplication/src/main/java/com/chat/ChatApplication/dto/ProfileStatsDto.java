package com.chat.ChatApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileStatsDto {

    private long conversations;

    private long friends;

    private long groups;

    private long filesShared;

}