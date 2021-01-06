package com.confusinguser.autowhitelist.utils;

import com.confusinguser.autowhitelist.AutoWhiteList;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class ConfigStorage {

    private Configuration config;
    private final File dataFolder;

    public ConfigStorage(File dataFolder) {
        this.dataFolder = dataFolder;
    }

    public Configuration reloadConfig() {
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(dataFolder, "config.yml"));
            return config;
        } catch (IOException exception) {
            resetConfig();
            return reloadConfig();
        }
    }

    public void resetConfig() {
        if (!dataFolder.exists())
            dataFolder.mkdir();

        File file = new File(dataFolder, "config.yml");

        if (!file.exists()) {
            try (InputStream in = AutoWhiteList.class.getResourceAsStream("config.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Configuration getConfig() {
        if (config == null) return reloadConfig();
        return config;
    }
}
