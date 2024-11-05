package com.pequla.dlaw.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class WorldData {
    private String seed;
    private Long time;
    private String type;
}
