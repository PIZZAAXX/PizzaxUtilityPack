package pizzaaxx.pizzaxutilitypack.Discord.Commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;
import pizzaaxx.pizzaxutilitypack.Discord.DiscordUtils;
import pizzaaxx.pizzaxutilitypack.PizzaxUtilityPack;
import pizzaaxx.pizzaxutilitypack.Translations.TranslationsManager;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class FindColorListener extends ListenerAdapter {

    private final PizzaxUtilityPack plugin;
    private final TranslationsManager translations;

    private final List<String> allowedFileExtensions = Arrays.asList("png", "jpeg", "jpg");

    private final Map<BufferedImage, Color> colors = new HashMap<>();

    public FindColorListener(@NotNull PizzaxUtilityPack plugin) throws IOException {
        this.plugin = plugin;
        this.translations = plugin.getTranslationsManager();

        for (File textureFile : plugin.getResourceFolderFiles("textures")) {

            InputStream stream = Files.newInputStream(textureFile.toPath());
            BufferedImage texture = ImageIO.read(stream);

            int sumR = 0, sumG = 0, sumB = 0;
            for (int x = 0; x < texture.getWidth(); x++) {
                for (int y = 0; y < texture.getHeight(); y++) {
                    Color pixel = new Color(texture.getRGB(x, y));
                    sumR += pixel.getRed();
                    sumG += pixel.getGreen();
                    sumB += pixel.getBlue();
                }
            }
            int num = texture.getWidth() * texture.getHeight();
            Color color = new Color(sumR / num, sumG / num, sumB / num);

            BufferedImage resizedTexture = new BufferedImage(256, 256, 1);
            AffineTransform at = new AffineTransform();
            at.scale(16.0, 16.0);
            AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
            resizedTexture = scaleOp.filter(texture, resizedTexture);

            colors.put(resizedTexture, color);
        }
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

            Map<BufferedImage, Double> distances = new HashMap<>();
            for (Map.Entry<BufferedImage, Color> entry : colors.entrySet()) {
                distances.put(entry.getKey(), this.colorDistance(color, entry.getValue()));
            }

            List<Map.Entry<BufferedImage, Double>> textureEntries = new ArrayList<>(distances.entrySet());
            textureEntries.sort(Map.Entry.comparingByValue());

            BufferedImage result = new BufferedImage(
                    768, 216, 1
            );
            Graphics2D graphics = result.createGraphics();

            graphics.drawImage(textureEntries.get(0).getKey(), 0, 0, null);
            graphics.drawImage(textureEntries.get(1).getKey(), 0, 0, null);
            graphics.drawImage(textureEntries.get(2).getKey(), 0, 0, null);

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try {
                ImageIO.write(result, "png", os);
            } catch (IOException e) {
                e.printStackTrace();
            }
            InputStream is = new ByteArrayInputStream(os.toByteArray());

            String hex = String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());

            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(color);
            embed.setTitle(translations.get("embedTitle").replace("$color$", hex));
            embed.setImage("attachment://img.png");

            event.replyFiles(
                    FileUpload.fromData(is, "img.png")
            ).addEmbeds(embed.build()).queue(
                    msg -> msg.deleteOriginal().queueAfter(20, TimeUnit.MINUTES)
            );
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

    public double colorDistance(@NotNull Color c1, @NotNull Color c2) {
        int red1 = c1.getRed();
        int red2 = c2.getRed();
        int rMean = (red1 + red2) >> 1;
        int r = red1 - red2;
        int g = c1.getGreen() - c2.getGreen();
        int b = c1.getBlue() - c2.getBlue();
        return Math.sqrt((((512+rMean)*r*r)>>8) + 4*g*g + (((767-rMean)*b*b)>>8));
    }

}
