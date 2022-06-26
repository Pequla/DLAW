package com.pequla.dlaw.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class ServerStatus {

    private PlayerStatus players;
    private WorldData world;
    private List<PluginData> plugins;
    private String version;

}
