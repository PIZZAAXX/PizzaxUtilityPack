package pizzaaxx.pizzaxutilitypack.Discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;
import net.dv8tion.jda.internal.requests.restaction.CommandCreateActionImpl;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import pizzaaxx.pizzaxutilitypack.Configuration.Configuration;
import pizzaaxx.pizzaxutilitypack.PizzaxUtilityPack;
import pizzaaxx.pizzaxutilitypack.Translations.TranslationsManager;

import java.util.*;

public class DiscordHandler {

    private final PizzaxUtilityPack plugin;
    private final TranslationsManager translations;

    private JDA bot;

    public JDA getBot() {
        return bot;
    }

    public DiscordHandler(@NotNull PizzaxUtilityPack plugin) {
        this.plugin = plugin;
        this.translations = plugin.getTranslationsManager();
    }

    public void reload() {

        Configuration config = new Configuration(plugin, "config");

        if (!config.contains("token")) {
            plugin.error("Please enter the Discord bot's token into the configuration file. No Discord features have been enabled.");
            return;
        }

        String token = config.getString("token");

        JDABuilder jdaBuilder = JDABuilder.createDefault(token);
        jdaBuilder.addEventListeners(

        );
        jdaBuilder.setStatus(OnlineStatus.ONLINE);

        try {
            bot = jdaBuilder.build().awaitReady();
        } catch (InterruptedException e) {
            plugin.error("An error occurred while starting the Discord bot.");
            return;
        }

        // --- COMMANDS ---

        Map<String, String> actualCommandNames = new HashMap<>();
        Set<String> neededCommands = new HashSet<>();
        Set<String> editedCommands = new HashSet<>();

        neededCommands.add(translations.get("findcolor.commandName"));
        actualCommandNames.put(translations.get("findcolor.commandName"), "findcolor");
        neededCommands.add(translations.get("pattern.commandName"));
        actualCommandNames.put(translations.get("pattern.commandName"), "pattern");

        bot.retrieveCommands().queue(
                commands -> {
                    for (Command command : commands) {
                        if (!neededCommands.contains(command.getName())) {
                            command.delete().queue();
                        } else {
                            switch (actualCommandNames.get(command.getName())) {
                                case "findcolor": {

                                    

                                    break;
                                }
                                case "pattern": {



                                    break;
                                }
                            }
                        }

                        for (String commandName : neededCommands) {
                            if (!editedCommands.contains(commandName)) {
                                // CREATE COMMAND
                            }
                        }
                    }
                }
        );
    }
}
