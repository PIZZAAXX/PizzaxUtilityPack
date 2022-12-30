package pizzaaxx.pizzaxutilitypack;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import pizzaaxx.pizzaxutilitypack.Configuration.Configuration;

import java.io.IOException;
import java.io.InputStreamReader;

public class ConfigManager {

    private final PizzaxUtilityPack plugin;
    private Configuration config;

    public ConfigManager(PizzaxUtilityPack plugin) {
        this.plugin = plugin;
    }

    public void reload() throws IOException, InvalidConfigurationException {
        this.config = new Configuration(plugin, "config");
        config.reload();

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

    public Configuration getConfig() {
        return config;
    }
}
