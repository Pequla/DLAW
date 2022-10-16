package com.pequla.dlaw.model.backend;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class LinkModel {
    private String uuid;
    private String userId;
    private String guildId;
}
