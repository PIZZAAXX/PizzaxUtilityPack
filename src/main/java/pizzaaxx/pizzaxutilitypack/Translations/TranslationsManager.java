package pizzaaxx.pizzaxutilitypack.Translations;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;
import pizzaaxx.pizzaxutilitypack.Configuration.Configuration;
import pizzaaxx.pizzaxutilitypack.PizzaxUtilityPack;

import java.io.IOException;
import java.io.InputStreamReader;

public class TranslationsManager {

    private final PizzaxUtilityPack plugin;

    private Configuration config;

    public TranslationsManager(PizzaxUtilityPack plugin) {
        this.plugin = plugin;
    }

    public void reload() throws IOException, InvalidConfigurationException {
        this.config = new Configuration(plugin, "translations");

        YamlConfiguration defaultConfig = new YamlConfiguration();
        defaultConfig.load(new InputStreamReader(plugin.getFileFromResourceAsStream("defaultConfig.yml")));

        for (String path : defaultConfig.getKeys(true)) {
            if (!defaultConfig.isConfigurationSection(path)) {
                if (!config.contains(path)) {
                    config.set(path, defaultConfig.get(path));
                }
            }
        }

        config.save();
    }

    @Nullable
    public String get(String path) {

        if (!config.contains(path)) {
            return null;
        }

        return config.getString(path);
    }

    public void set(String path, Object value) {
        config.set(path, value);
    }

    public void save() {
        config.save();
    }
}
