package pizzaaxx.pizzaxutilitypack.Discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;
import pizzaaxx.pizzaxutilitypack.Configuration.Configuration;
import pizzaaxx.pizzaxutilitypack.Discord.Commands.FindColorListener;
import pizzaaxx.pizzaxutilitypack.PizzaxUtilityPack;
import pizzaaxx.pizzaxutilitypack.Translations.TranslationsManager;

import java.io.IOException;
import java.net.URISyntaxException;

public class DiscordManager {

    private final PizzaxUtilityPack plugin;
    private final TranslationsManager translations;
    private FindColorListener findColorListener;

    public FindColorListener getFindColorListener() {
        return findColorListener;
    }

    private Configuration config;

    private JDA bot;

    public JDA getBot() {
        return bot;
    }

    public DiscordManager(@NotNull PizzaxUtilityPack plugin) {
        this.plugin = plugin;
        this.translations = plugin.getTranslationsManager();
    }

    public void reload() {

        config = plugin.getConfigManager().getConfig();

        if (!config.contains("token") || config.getString("token").equals("Enter token here")) {
            plugin.error("Please enter the Discord bot's token into the configuration file. No Discord features have been enabled.");
            return;
        }

        String token = config.getString("token");

        JDABuilder jdaBuilder = JDABuilder.createDefault(token);
        jdaBuilder.addEventListeners(

        );
        jdaBuilder.setStatus(OnlineStatus.ONLINE);

        try {
            findColorListener = new FindColorListener(plugin);
            jdaBuilder.addEventListeners(findColorListener);
        } catch (IOException | URISyntaxException e) {
            plugin.error("Error starting FindColor command listener. Command won't be enabled.");
        }

        try {
            bot = jdaBuilder.build().awaitReady();
        } catch (InterruptedException e) {
            plugin.error("An error occurred while starting the Discord bot.");
            return;
        }

        plugin.warn("Use §6/pizzaxup updateCommands§e to create Slash Commands or when updating translations or enabling or disabling features.");
    }

    public void updateCommands() {
        bot.retrieveCommands().queue(
                commands -> {
                    for (Command command : commands) {
                        command.delete().queue();
                    }
                }
        );

        if (config.getBoolean("features.findcolor")) {
            bot.upsertCommand(
                    translations.get("findcolor.commandName"),
                    translations.get("findcolor.commandDescription")
            ).addSubcommands(
                    new SubcommandData(
                            translations.get("findcolor.hexSubcommandName"),
                            translations.get("findcolor.hexSubcommandDescription")
                    ).addOption(
                            OptionType.STRING,
                            translations.get("findcolor.hexParamName"),
                            translations.get("findcolor.hexParamDescription"),
                            true
                    ),
                    new SubcommandData(
                            translations.get("findcolor.fileSubcommandName"),
                            translations.get("findcolor.fileSubcommandDescription")
                    ).addOption(
                            OptionType.ATTACHMENT,
                            translations.get("findcolor.fileParamName"),
                            translations.get("findcolor.fileParamDescription"),
                            true
                    )
            ).queue();
        }

        if (config.getBoolean("features.pattern")) {
            bot.upsertCommand(
                    translations.get("pattern.commandName"),
                    translations.get("pattern.commandDescription")
            ).addOption(
                    OptionType.STRING,
                    translations.get("pattern.patternParamName"),
                    translations.get("pattern.patternParamDescription"),
                    true
            ).queue();
        }

    }
}
