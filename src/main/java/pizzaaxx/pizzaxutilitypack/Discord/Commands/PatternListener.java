package pizzaaxx.pizzaxutilitypack.Discord.Commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.AttachedFile;
import net.dv8tion.jda.api.utils.FileUpload;
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
            List<Integer[]> values;
            try {
                values = this.parse(pattern);
                for (Integer[] value : values) {
                    totalPercentage += value[0];
                }
            } catch (IllegalArgumentException e) {
                event.replyEmbeds(
                        DiscordUtils.fastEmbed(
                                Color.RED,
                                translations.get("pattern.patternError").replace("$section$", e.getMessage())
                        )
                ).queue(
                        msg -> msg.deleteOriginal().queueAfter(10, TimeUnit.SECONDS)
                );
                return;
            }

            BufferedImage image = this.createImage(values, 16, totalPercentage);

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try {
                ImageIO.write(image, "png", os);
            } catch (IOException e) {
                e.printStackTrace();
            }
            InputStream is = new ByteArrayInputStream(os.toByteArray());

            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Color.GREEN);
            builder.setTitle(translations.get("pattern.embedTitle").replace("$size$", "16x16"));
            builder.addField(
                    new MessageEmbed.Field(
                            translations.get("pattern.fieldTitle"),
                            translations.get("pattern.fieldContent").replace("$pattern$", pattern),
                            false
                    )
            );
            builder.setImage("attachment://pattern.png");

            event.replyFiles(
                    FileUpload.fromData(
                            is,
                            "pattern.png"
                    )
            ).addEmbeds(
                    builder.build()
            ).addActionRow(
                    Button.of(
                            ButtonStyle.PRIMARY,
                            "patternGenerateAgain~16",
                            translations.get("pattern.generateAgainButton"),
                            Emoji.fromUnicode("U+1F504")
                    ),
                    Button.of(
                            ButtonStyle.DANGER,
                            "patternDelete",
                            translations.get("pattern.deleteMessage"),
                            Emoji.fromUnicode("U+1F5D1")
                    )
            ).addActionRow(
                    StringSelectMenu.create("patternChangeSize")
                            .addOption("16x16", "16")
                            .addOption("32x32", "32")
                            .addOption("64x64", "64")
                            .setDefaultValues("16")
                            .build()
            ).queue(
                    msg -> msg.deleteOriginal().queueAfter(20, TimeUnit.MINUTES)
            );
        }
    }

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {

        if (event.getSelectMenu().getId().equals("patternChangeSize")) {

            User user = event.getMessage().getInteraction().getUser();
            if (!user.getId().equals(event.getUser().getId())) {
                event.replyEmbeds(
                        DiscordUtils.fastEmbed(
                                Color.RED,
                                translations.get("pattern.nonOriginalUser")
                        )
                ).setEphemeral(true).queue();
                return;
            }

            SelectOption option = event.getInteraction().getSelectedOptions().get(0);
            int size = Integer.parseInt(option.getValue());

            Message message = event.getMessage();

            MessageEmbed embed = message.getEmbeds().get(0);
            MessageEmbed.Field field = embed.getFields().get(0);
            String pattern = field.getValue().replace("`", "");

            List<Integer[]> values = this.parse(pattern);

            int totalPercentage = 0;
            for (Integer[] value : values) {
                totalPercentage += value[0];
            }

            final BufferedImage image = this.createImage(values, size, totalPercentage);

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try {
                ImageIO.write(image, "png", os);
            } catch (IOException e) {
                e.printStackTrace();
            }
            InputStream is = new ByteArrayInputStream(os.toByteArray());

            EmbedBuilder builder = new EmbedBuilder();

            builder.setTitle(
                    translations.get("pattern.embedTitle").replace("$size$", size + "x" + size)
            );

            event.editMessageAttachments(
                    AttachedFile.fromData(is, "pattern.png")
            ).setEmbeds(builder.build()).setComponents(
                    ActionRow.of(
                            Button.of(
                                    ButtonStyle.PRIMARY,
                                    "patternGenerateAgain~" + size,
                                    translations.get("pattern.generateAgainButton"),
                                    Emoji.fromUnicode("U+1F504")
                            ),
                            Button.of(
                                    ButtonStyle.DANGER,
                                    "patternDelete",
                                    translations.get("pattern.deleteMessage"),
                                    Emoji.fromUnicode("U+1F5D1")
                            )
                    ),
                    ActionRow.of(
                            event.getComponent()
                    )
            ).queue(
                    msg -> msg.deleteOriginal().queueAfter(20, TimeUnit.MINUTES)
            );

        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {

        if (event.getButton().getId().equals("patternDelete")) {
            User user = event.getMessage().getInteraction().getUser();
            if (!user.getId().equals(event.getUser().getId())) {
                event.replyEmbeds(
                        DiscordUtils.fastEmbed(
                                Color.RED,
                                translations.get("pattern.nonOriginalUser")
                        )
                ).setEphemeral(true).queue();
                return;
            }
            event.getMessage().delete().queue();
        }

        if (event.getButton().getId().startsWith("patternGenerateAgain")) {

            User user = event.getMessage().getInteraction().getUser();
            if (!user.getId().equals(event.getUser().getId())) {
                event.replyEmbeds(
                        DiscordUtils.fastEmbed(
                                Color.RED,
                                translations.get("pattern.nonOriginalUser")
                        )
                ).setEphemeral(true).queue();
                return;
            }

            int size = Integer.parseInt(event.getButton().getId().split("~")[1]);

            Message message = event.getMessage();

            MessageEmbed embed = message.getEmbeds().get(0);
            MessageEmbed.Field field = embed.getFields().get(0);
            String pattern = field.getValue().replace("`", "");

            List<Integer[]> values = this.parse(pattern);

            int totalPercentage = 0;
            for (Integer[] value : values) {
                totalPercentage += value[0];
            }

            final BufferedImage image = this.createImage(values, size, totalPercentage);

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try {
                ImageIO.write(image, "png", os);
            } catch (IOException e) {
                e.printStackTrace();
            }
            InputStream is = new ByteArrayInputStream(os.toByteArray());

            event.editMessageAttachments(
                    AttachedFile.fromData(is, "pattern.png")
            ).queue(
                    msg -> msg.deleteOriginal().queueAfter(20, TimeUnit.MINUTES)
            );
        }
    }

    @NotNull
    private List<Integer[]> parse(@NotNull String pattern) throws IllegalArgumentException {

        List<Integer[]> values = new ArrayList<>();
        for (String section : pattern.split(",")) {
            if (!section.matches("^(([1-9]\\d?|100)\\%)?([1-9](\\d\\d?)?)(:(1[0-5]|\\d))?")) {
                throw new IllegalArgumentException(section);
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
                throw new IllegalArgumentException(section);
            }

            Integer[] sectionValues = new Integer[3];

            sectionValues[0] = percentage;
            sectionValues[1] = id;
            sectionValues[2] = metadata;

            values.add(sectionValues);
        }
        return values;
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
