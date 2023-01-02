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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    public List<InputStream> getStreamsFromFolder(String folder) throws URISyntaxException, IOException {
        List<Path> paths = this.getPathsFromResourceJAR(folder);
        if (paths == null) {
            paths = new ArrayList<>();
        }

        List<InputStream> streams = new ArrayList<>();

        for (Path path : paths) {
            String filePathInJAR = path.toString();
            if (filePathInJAR.startsWith("/")) {
                filePathInJAR = filePathInJAR.substring(1, filePathInJAR.length());
            }

            InputStream is = this.getFileFromResourceAsStream(filePathInJAR);
            streams.add(is);
        }

        return streams;
    }

    private List<Path> getPathsFromResourceJAR(String folder)
            throws URISyntaxException, IOException {

        List<Path> result;

        // get path of the current running JAR
        String jarPath = getClass().getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI()
                .getPath();

        // file walks JAR
        URI uri = URI.create("jar:file:" + jarPath);
        try (FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
            result = Files.walk(fs.getPath(folder))
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());
        }

        return result;
    }

}
