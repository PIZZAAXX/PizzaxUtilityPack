package pizzaaxx.pizzaxutilitypack.Minecraft.Admin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import pizzaaxx.pizzaxutilitypack.PizzaxUtilityPack;

import java.io.IOException;

public class AdminCommand implements CommandExecutor {

    private final PizzaxUtilityPack plugin;
    private final String PREFIX = "§f[§cPIZZAX's §9UP§f] §7>> §r";

    public AdminCommand(PizzaxUtilityPack plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length > 0) {
            switch (args[0]) {
                case "reload": {

                    try {
                        plugin.getTranslationsManager().reload();
                        plugin.getConfigManager().reload();
                        sender.sendMessage(PREFIX + "Reloading succesful.");
                    } catch (IOException | InvalidConfigurationException e) {
                        sender.sendMessage(PREFIX + "An error has occurred while reloading.");
                    }
                    break;
                }
                case "updateCommands": {

                    try {
                        plugin.getTranslationsManager().reload();
                        plugin.getConfigManager().reload();
                        plugin.getDiscordManager().updateCommands();

                        sender.sendMessage(PREFIX + "Commands have been updated.");
                    } catch (IOException | InvalidConfigurationException e) {
                        sender.sendMessage(PREFIX + "An error has occurred while reloading configurations.");
                    }

                    break;
                }
            }
        }

        return true;
    }
}
