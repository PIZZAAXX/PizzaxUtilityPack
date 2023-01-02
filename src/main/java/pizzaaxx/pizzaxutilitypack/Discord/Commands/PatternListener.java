package pizzaaxx.pizzaxutilitypack.Discord.Commands;

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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PatternListener extends ListenerAdapter {

    private final PizzaxUtilityPack plugin;
    private final TranslationsManager translations;
    private final BufferedImage[][] textures;

    public PatternListener(@NotNull PizzaxUtilityPack plugin) throws IOException {
        this.plugin = plugin;
        this.translations = plugin.getTranslationsManager();
        this.textures = new BufferedImage[253][16];
        BufferedImage blank = new BufferedImage(16, 16, 1);
        BufferedImage textures = ImageIO.read(plugin.getFileFromResourceAsStream("textures.png"));
        for (int x = 0; x < 4048; x += 16) {
            for (int y = 0; y < 256; y += 16) {
                BufferedImage subImage = textures.getSubimage(x, y, 16, 16);
                if (subImage.getRGB(0,0) != 65535) {
                    this.textures[x/16][y/16] = subImage;
                }
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {

        if (event.getName().equals(translations.get("pattern.commandName"))) {
            OptionMapping option = event.getOption(translations.get("pattern.patternParamName"));
            if (option == null) {
                error(event, "errorOccurred");
                return;
            }
            String pattern = option.getAsString();

            int totalPercentage = 0;
            List<Integer[]> values = new ArrayList<>();
            for (String section : pattern.split(",")) {
                if (!section.matches("^(([1-9]\\d?|100)\\%)?([1-9](\\d\\d?)?)(:(1[0-5]|\\d))?")) {

                    event.replyEmbeds(
                            DiscordUtils.fastEmbed(
                                    Color.RED,
                                    translations.get("pattern.patternError").replace("$section$", section)
                            )
                    ).queue(
                            msg -> msg.deleteOriginal().queueAfter(10, TimeUnit.SECONDS)
                    );
                    return;
                }

                int percentage, id, metadata;

                if (section.contains("%") && section.contains(":")) {
                    String[] parts1 = section.split("%");
                    String[] parts2 = parts1[1].split(":");
                    percentage = Integer.parseInt(parts1[0]);
                    id = Integer.parseInt(parts2[0]);
                    metadata = Integer.parseInt(parts2[1]);
                } else if (section.contains("%")) {
                    String[] parts = section.split("%");
                    percentage = Integer.parseInt(parts[0]);
                    id = Integer.parseInt(parts[1]);
                    metadata = 0;
                } else if (section.contains(":")) {
                    String[] parts = section.split(":");
                    percentage = 100;
                    id = Integer.parseInt(parts[0]);
                    metadata = Integer.parseInt(parts[1]);
                } else {
                    percentage = 100;
                    id = Integer.parseInt(section);
                    metadata = 0;
                }

                if (id > 253) {
                    event.replyEmbeds(
                            DiscordUtils.fastEmbed(
                                    Color.RED,
                                    translations.get("pattern.invalidID").replace("$section$", section)
                            )
                    ).queue(
                            msg -> msg.deleteOriginal().queueAfter(10, TimeUnit.SECONDS)
                    );
                    return;
                }

                totalPercentage += percentage;

                Integer[] sectionValues = new Integer[3];
                sectionValues[0] = percentage;
                sectionValues[1] = id;
                sectionValues[2] = metadata;

                values.add(sectionValues);
            }

            BufferedImage image = this.createImage(values, 16, totalPercentage);

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try {
                ImageIO.write(image, "png", os);
            } catch (IOException e) {
                e.printStackTrace();
            }
            InputStream is = new ByteArrayInputStream(os.toByteArray());


        }

    }

    @NotNull
    private BufferedImage createImage(List<Integer[]> values, int size, int totalPercentage) {
        BufferedImage image = new BufferedImage(16 * size, 16 * size, 1);
        Graphics2D graphics = image.createGraphics();

        for (int x = 0; x < 16 * size; x += 16) {
            for (int y = 0; y < 16 * size; y += 16) {

                Integer[] pick = new Integer[3];
                double p = Math.random();
                double accumulation = 0.0;
                for (Integer[] value : values) {
                    accumulation += value[0] / (double) totalPercentage;
                    if (accumulation >= p) {
                        pick = value;
                        break;
                    }
                }

                BufferedImage texture = textures[pick[1]][pick[2]];

                if (texture != null) {
                    graphics.drawImage(texture, x, y, null);
                }

            }
        }

        return image;
    }

    private void error(@NotNull SlashCommandInteractionEvent event, String error) {
        event.replyEmbeds(
                DiscordUtils.fastEmbed(
                        Color.RED,
                        translations.get("pattern." + error)
                )
        ).queue(
                msg -> msg.deleteOriginal().queueAfter(10, TimeUnit.SECONDS)
        );
    }
}
