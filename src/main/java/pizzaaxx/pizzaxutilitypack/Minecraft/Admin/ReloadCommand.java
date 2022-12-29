package pizzaaxx.pizzaxutilitypack.Minecraft.Admin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import pizzaaxx.pizzaxutilitypack.PizzaxUtilityPack;

public class ReloadCommand implements CommandExecutor {

    private final PizzaxUtilityPack plugin;

    public ReloadCommand(PizzaxUtilityPack plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {



        return true;
    }
}
