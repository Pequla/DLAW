package com.pequla.dlaw.model;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ServerStatus {
    private PlayerStatus players;
    private WorldData world;
    private List<PluginData> plugins;
    private String version;
}
