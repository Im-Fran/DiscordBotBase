package cl.franciscosolis.discordbotbase.commands.slash;

import cl.franciscosolis.discordbotbase.modules.SlashCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public class PingSlashCommand extends SlashCommand { 

    public PingSlashCommand() {
        super();
        this.deferReply = true;
    }

    @Override
    public String command() {
        return "ping";
    }

    @Override
    public String description() {
        return "Pong!";
    }

    @Override
    public void onCommand(TextChannel channel, Member member, SlashCommandEvent event, long timeTook) {
        event.getHook().sendMessage("Pong!\nIt took **" + timeTook + "ms**").queue();
    }
    
}
