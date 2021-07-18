package cl.franciscosolis.discordbotbase.utils;

import java.awt.Color;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import cl.franciscosolis.discordbotbase.DiscordBotBase;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

public class CustomEmbedBuilder extends EmbedBuilder {

    public CustomEmbedBuilder() {
        color(new Color(81, 153, 226));
    }

    public CustomEmbedBuilder(String title) {
        if(title != null)
            setAuthor(title, "https://franciscosolis.cl", "https://i.imgur.com/ezAmPKT.png");

        color(new Color(81, 153, 226));
        footer("Made with ❤️ by Fran");
    }

    public CustomEmbedBuilder(String title, boolean footer) {
        if(title != null)
            setAuthor(title, "https://franciscosolis.cl", "https://i.imgur.com/ezAmPKT.png");
        if(footer)
            footer("Made with ❤️ by Fran");

        color(new Color(81, 153, 226));
    }

    public CustomEmbedBuilder(boolean footer) {
        if(footer)
            footer("Made with ❤️ by Fran");

        color(new Color(81, 153, 226));
    }

    public CustomEmbedBuilder footer(String text) {
        setFooter(DiscordBotBase.i.jda.getSelfUser().getName() + " • " + text, "https://i.imgur.com/ezAmPKT.png");

        return this;
    }

    public CustomEmbedBuilder error() {
        color(new Color(180,30,30));

        return this;
    }

    public CustomEmbedBuilder success() {
        color(new Color(50, 200, 50));

        return this;
    }

    public CustomEmbedBuilder warning(){
        color(new Color(255, 130, 0));

        return this;
    }

    public CustomEmbedBuilder text(String text) {
        setDescription(text);

        return this;
    }

    public CustomEmbedBuilder text(String... text) {
        setDescription(String.join("\n", text));

        return this;
    }

    public CustomEmbedBuilder thumbnail(String url) {
        super.setThumbnail(url);

        return this;
    }

    public CustomEmbedBuilder color(Color color) {
        if(color == null)
            return this;

        super.setColor(color);
        return this;
    }

    public CustomEmbedBuilder image(String url) {
        super.setImage(url);

        return this;
    }

    public CustomEmbedBuilder field(String name, String value, boolean inline) {
        super.addField(name, value, inline);

        return this;
    }

    public CustomEmbedBuilder blankField(boolean inline) {
        super.addBlankField(inline);

        return this;
    }

    public String getText() {
        return super.getDescriptionBuilder().toString();
    }

    public Message complete(User user) {
        try {
            return user.openPrivateChannel().complete().sendMessageEmbeds(build()).complete();
        } catch (ErrorResponseException ignore) { }
        return null;
    }

    public void queue(TextChannel textChannel) {
        textChannel.sendMessageEmbeds(build()).queue();
    }

    public void queue(Member member) {
        queue(member.getUser());
    }

    public void queue(User user) {
        try {
            user.openPrivateChannel().queue(c -> c.sendMessageEmbeds(build()).queue());
        } catch (ErrorResponseException ignore) { }
    }

    public void queue(TextChannel textChannel, Consumer<Message> consumer) {
        textChannel.sendMessageEmbeds(build()).queue(consumer);
    }

    public void queueAfter(TextChannel textChannel, int delay, TimeUnit unit) {
        textChannel.sendMessageEmbeds(build()).queueAfter(delay, unit);
    }

    public void queueAfter(TextChannel textChannel, int delay, TimeUnit unit, Consumer<Message> success) {
        textChannel.sendMessageEmbeds(build()).queueAfter(delay, unit, success);
    }

    public void queueAfter(User user, int delay, TimeUnit time) {
        try {
            user.openPrivateChannel().complete().sendMessageEmbeds(build()).queueAfter(delay, time);
        } catch (ErrorResponseException ignore) { }
    }

    public Message reply(Message message) {
        return reply(message, true);
    }

    public Message reply(Message message, boolean mention) {
        return message.replyEmbeds(build()).mentionRepliedUser(mention).complete();
    }

    public void replyTemporary(Message message, int duration, TimeUnit timeUnit) {
        replyTemporary(message, true, duration, timeUnit);
    }

    public void replyTemporary(Message message, boolean mention, int duration, TimeUnit timeUnit) {
        message.replyEmbeds(build()).mentionRepliedUser(mention).queue((msg -> msg.delete().submitAfter(duration, timeUnit)));
    }

    public void sendTemporary(TextChannel textChannel, int duration, TimeUnit timeUnit) {
        queue(textChannel, (msg) -> msg.delete().submitAfter(duration, timeUnit));
    }

    public void sendTemporary(TextChannel textChannel, int duration) {
        sendTemporary(textChannel, duration, TimeUnit.SECONDS);
    }

    public ScheduledFuture<?> sendAfter(TextChannel textChannel, int duration, Consumer<Message> onSuccess) {
        return textChannel.sendMessageEmbeds(build()).queueAfter(duration, TimeUnit.SECONDS, onSuccess);
    }

    public ScheduledFuture<?> sendAfter(TextChannel textChannel, int duration, TimeUnit timeUnit, Consumer<Message> onSuccess) {
        return textChannel.sendMessageEmbeds(build()).queueAfter(duration, timeUnit, onSuccess);
    }

    public static CustomEmbedBuilder err(String message){
        return new CustomEmbedBuilder("Error!").text(message);
    }

    public static CustomEmbedBuilder info(String message){
        return new CustomEmbedBuilder("Information:").text(message);
    }

    public static CustomEmbedBuilder success(String message){
        return new CustomEmbedBuilder("Great!").text(message);
    }

    public static CustomEmbedBuilder warning(String message){
        return new CustomEmbedBuilder("Warning:").text(message);
    }
}
