package cl.franciscosolis.discordbotbase.commands;

import cl.franciscosolis.discordbotbase.modules.Command;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class PingCommand extends Command {

    public PingCommand(){
        this.deleteOnExecution = false;   
    }

    @Override
    public String command() {
        return "ping";
    }

    @Override
    public void onExecute(MessageReceivedEvent event, TextChannel textChannel, Message message, User author, Member member, String alias, String[] args, long time) {
        message.reply("Pong!\n It took **" + time + "ms**.").submit();
    }
    
}
