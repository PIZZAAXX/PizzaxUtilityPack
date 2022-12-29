package pizzaaxx.pizzaxutilitypack;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import pizzaaxx.pizzaxutilitypack.Discord.DiscordHandler;
import pizzaaxx.pizzaxutilitypack.Translations.TranslationsManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public final class PizzaxUtilityPack extends JavaPlugin {

    private final File folder = super.getDataFolder();

    public File getFolder() {
        return folder;
    }

    private DiscordHandler discordHandler;

    public DiscordHandler getDiscordHandler() {
        return discordHandler;
    }

    private TranslationsManager translationsManager;

    public TranslationsManager getTranslationsManager() {
        return translationsManager;
    }

    @Override
    public void onEnable() {

        this.log("Starting plugin.");
        this.log("§8Made by PIZZAX.");
        this.log(" ");

        if (!folder.exists()) {
            if (!folder.mkdir()) {
                this.error("The plugin's folder couldn't be created. Stopping plugin's initialization.");
                return;
            }

            File config = new File(this.folder, "config.yml");

            try {
                config.createNewFile();
            } catch (IOException e) {
                this.error("A configuration file couldn't be created. Stopping plugin's initialization.");
                return;
            }

            this.log("The plugin's folder and a configuration file have been created. Please check the configuration file.");
            this.log("Enter the Discord bot's token into the configuration file.");
            this.warn("Use §6/pupreload§e to reload the configuration and enable the Discord features.");
            return;
        }

        File translations = new File(this.folder, "translations");

        if (!translations.exists()) {

            try {
                translations.createNewFile();
            } catch (IOException e) {
                this.warn("A translations file couldn't be created. Stopping plugin's initialization.");
                return;
            }

            this.warn("No translations file has been found. A new file with default translations has been created.");
        }

        this.translationsManager = new TranslationsManager(this);

        try {
            this.translationsManager.reload();
        } catch (IOException | InvalidConfigurationException e) {
            this.error("Error loading internal default translations. Stopping plugin's initialization.");
            return;
        }

        File config = new File(this.folder, "config.yml");

        try {
            if (config.createNewFile()) {
                this.log("A configuration file has been created.");
                this.log("Enter the Discord bot's token into the configuration file.");
                this.warn("Use §6/pupreload§e to reload the configuration and enable the Discord features.");
            } else {
                discordHandler = new DiscordHandler(this);
                discordHandler.reload();
            }
        } catch (IOException e) {
            this.error("A configuration file couldn't be created. Stopping plugin's initialization.");
        }

    }

    @Override
    public void onDisable() {



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
