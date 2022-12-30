package pizzaaxx.pizzaxutilitypack.Discord.Commands;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import pizzaaxx.pizzaxutilitypack.Discord.DiscordUtils;
import pizzaaxx.pizzaxutilitypack.PizzaxUtilityPack;
import pizzaaxx.pizzaxutilitypack.Translations.TranslationsManager;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class FindColorListener extends ListenerAdapter {

    private final PizzaxUtilityPack plugin;
    private final TranslationsManager translations;

    private final List<String> allowedFileExtensions = Arrays.asList("png", "jpeg", "jpg");

    public FindColorListener(@NotNull PizzaxUtilityPack plugin) throws IOException {
        this.plugin = plugin;
        this.translations = plugin.getTranslationsManager();


    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals(translations.get("findcolor.commandName"))) {
            Color color = null;
            String subcommand = event.getSubcommandName();
            if (subcommand == null) {
                error(event, "errorOccurred");
                return;
            }
            if (event.getSubcommandName().equals(translations.get("findcolor.hexSubcommandName"))) {

                OptionMapping option = event.getOption(translations.get("findcolor.hexParamName"));
                if (option == null) {
                    error(event, "errorOccurred");
                    return;
                }

                String code = option.getAsString().toUpperCase().replace("#", "");

                if (!code.matches("[A-F0-9]{6}")) {
                    error(event, "validHex");
                    return;
                }

                color = Color.decode( "#"+ code);

            } else if ((event.getSubcommandName().equals(translations.get("findcolor.fileSubcommandName")))) {

                OptionMapping option = event.getOption(translations.get("findcolor.fileParamName"));
                if (option == null) {
                    error(event, "errorOccurred");
                    return;
                }

                Message.Attachment attachment = option.getAsAttachment();

                if (!allowedFileExtensions.contains(attachment.getFileExtension())) {
                    error(event, "nonImageFile");
                    return;
                }

                InputStream stream;
                try {
                    stream = attachment.getProxy().download().get();

                } catch (InterruptedException | ExecutionException e) {
                    error(event, "errorOccured");
                    return;
                }

                BufferedImage input;
                try {
                    input = ImageIO.read(stream);
                } catch (IOException e) {
                    error(event, "errorOccurred");
                    return;
                }

                if (input.getHeight() * input.getWidth() > 4000000) {
                    error(event, "imageTooLarge");
                    return;
                }

                int sumR = 0, sumG = 0, sumB = 0;
                for (int x = 0; x < input.getWidth(); x++) {
                    for (int y = 0; y < input.getHeight(); y++) {
                        Color pixel = new Color(input.getRGB(x, y));
                        sumR += pixel.getRed();
                        sumG += pixel.getGreen();
                        sumB += pixel.getBlue();
                    }
                }
                int num = input.getWidth() * input.getHeight();
                color = new Color(sumR / num, sumG / num, sumB / num);
            }

            if (color == null) {
                error(event, "errorOccurred");
                return;
            }


        }
    }

    private void error(@NotNull SlashCommandInteractionEvent event, String translationName) {
        event.replyEmbeds(DiscordUtils.fastEmbed(
                Color.RED,
                translations.get(translationName)
        )).queue(
                msg -> msg.deleteOriginal().queueAfter(20, TimeUnit.SECONDS)
        );
    }

}
