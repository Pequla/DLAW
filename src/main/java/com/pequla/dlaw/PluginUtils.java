package com.pequla.dlaw;

import java.util.UUID;

public class PluginUtils {
    public static String cleanUUID(UUID uuid) {
        return cleanUUID(uuid.toString());
    }

    public static String cleanUUID(String uuid) {
        return uuid.replace("-", "");
    }
}
