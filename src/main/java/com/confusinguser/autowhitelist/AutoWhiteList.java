package com.confusinguser.autowhitelist;

import com.confusinguser.autowhitelist.utils.ApiUtil;
import com.confusinguser.autowhitelist.utils.ConfigStorage;
import com.confusinguser.autowhitelist.utils.DatabaseManager;
import com.confusinguser.autowhitelist.utils.Multithreading;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginDescription;
import net.md_5.bungee.config.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public final class AutoWhiteList extends Plugin {

    public static AutoWhiteList instance = null;
    public static Logger logger = Logger.getLogger("AutoWhiteList");
    public DatabaseManager databaseManager;
    private final ConfigStorage configStorage = new ConfigStorage(getDataFolder());

    public AutoWhiteList(ProxyServer proxy, PluginDescription description) {
        super(proxy, description);
        instance = this;
        databaseManager = new DatabaseManager();
    }

    public AutoWhiteList() {
        super();
        instance = this;
        databaseManager = new DatabaseManager();
    }

    @Override
    public void onEnable() {
        Multithreading.scheduleAtFixedRate(() -> {
            Configuration configuration = configStorage.reloadConfig();
            if (configuration == null) return;
            for (String guildId : configuration.getStringList("guild_IDs")) {
                List<String> members = ApiUtil.getGuildMembers(guildId);
                databaseManager.addToWhitelist(members);
            }
        }, 0, 6, TimeUnit.HOURS);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public ConfigStorage getConfigStorage() {
        return configStorage;
    }
}
