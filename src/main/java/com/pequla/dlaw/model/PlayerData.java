package com.pequla.dlaw.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PlayerData {
    private String id;
    private String name;
    private String displayName;
    private Long firstPlayed;
    private Long lastPlayed;
}
