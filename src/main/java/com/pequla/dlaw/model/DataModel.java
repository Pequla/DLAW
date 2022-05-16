package com.pequla.dlaw.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataModel {

    private Integer id;
    private String discordId;
    private String uuid;
    private LocalDateTime createdAt;
    private LocalDateTime bannedAt;
    private String bannedBy;
    private String guildId;
}
