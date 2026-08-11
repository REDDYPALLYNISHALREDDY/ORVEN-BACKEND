package com.chat.ChatApplication.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrivacySettingsDto {

    private boolean showOnlineStatus;

    private boolean showLastSeen;

    private boolean readReceipts;

}