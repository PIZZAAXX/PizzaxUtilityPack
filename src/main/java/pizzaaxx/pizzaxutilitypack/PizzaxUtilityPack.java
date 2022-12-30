package pizzaaxx.pizzaxutilitypack;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import pizzaaxx.pizzaxutilitypack.Discord.DiscordManager;
import pizzaaxx.pizzaxutilitypack.Minecraft.Admin.AdminCommand;
import pizzaaxx.pizzaxutilitypack.Translations.TranslationsManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public final class PizzaxUtilityPack extends JavaPlugin {

    private final File folder = super.getDataFolder();

    public File getFolder() {
        return folder;
    }

    private DiscordManager discordManager;

    public DiscordManager getDiscordManager() {
        return discordManager;
    }

    private TranslationsManager translationsManager;

    public TranslationsManager getTranslationsManager() {
        return translationsManager;
    }

    private ConfigManager configManager;

    public ConfigManager getConfigManager() {
        return configManager;
    }

    @Override
    public void onEnable() {

        this.log("Starting plugin.");
        this.log("§8Made by PIZZAX.");
        this.log(" ");

        if (!folder.exists()) {
            folder.mkdir();
        }

        translationsManager = new TranslationsManager(this);
        try {
            translationsManager.reload();
        } catch (IOException | InvalidConfigurationException e) {
            this.error("Error loading translations. Plugin stopped.");
            return;
        }

        configManager = new ConfigManager(this);
        try {
            configManager.reload();
        } catch (IOException | InvalidConfigurationException e) {
            this.error("Error loading configuration. Plugin stopped.");
            return;
        }

        discordManager = new DiscordManager(this);
        discordManager.reload();

        getCommand("pizzaxup").setExecutor(new AdminCommand(this));

    }

    @Override
    public void onDisable() {

        if (discordManager.getBot() != null) {
            discordManager.getBot().shutdownNow();
        }

    }

    public void log(@NotNull Object object) {
        Bukkit.getConsoleSender().sendMessage("§f[§cPIZZAX's §9UP§f] §7>> §r" + object);
    }

    public void warn(@NotNull Object object) {
        Bukkit.getConsoleSender().sendMessage("§f[§cPIZZAX's §9UP§f] §7>> §e" + object);
    }

    public void error(@NotNull Object object) {
        Bukkit.getConsoleSender().sendMessage("§f[§cPIZZAX's §9UP§f] §7>> §c" + object);
    }

    @NotNull
    public InputStream getFileFromResourceAsStream(String fileName) {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName);

        if (inputStream == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {
            return inputStream;
        }

    }

}
