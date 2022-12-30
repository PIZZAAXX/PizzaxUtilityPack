package pizzaaxx.pizzaxutilitypack.Discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class DiscordUtils {

    @NotNull
    public static MessageEmbed fastEmbed(Color color, String title, @Nullable String description) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(color);
        builder.setTitle(title);
        if (description != null) {
            builder.setDescription(description);
        }

        return builder.build();
    }

    @NotNull
    public static MessageEmbed fastEmbed(Color color, String title) {
        return fastEmbed(color, title, null);
    }

}
