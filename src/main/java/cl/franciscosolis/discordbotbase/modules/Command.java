package cl.franciscosolis.discordbotbase.modules;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import cl.franciscosolis.discordbotbase.DiscordBotBase;
import cl.franciscosolis.discordbotbase.objects.Cooldown;
import cl.franciscosolis.discordbotbase.objects.Requirement;
import cl.franciscosolis.discordbotbase.objects.query.DefinedQuery;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class Command {

    public final LinkedHashMap<String, Cooldown> cooldowns = new LinkedHashMap<>();
    public final DiscordBotBase bot = DiscordBotBase.i;
    private boolean enabled = false;

    public boolean deleteOnExecution = true;
    public final String identifier;

    public Command(){
        this.identifier = "[" + this.command() + " Command]";
    }

    public abstract String command();

    public String[] aliases(){
        return new String[0];
    }

    public void enable(){
        Requirement[] failed = Arrays.stream(this.getRequirements()).filter(it -> !it.check()).toArray(Requirement[]::new);
        if (failed.length > 0) {
            System.out.println("Failed requirements for command " + identifier + ": ");
            for (Requirement r : failed) {
                System.out.println("- " + r.getUnmatchMessage());
            }
        }else{
            this.onEnable();
            this.enabled = true;
            System.out.println("Command " + identifier + " enabled");
        }
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public DefinedQuery<Role> getRestrictedRoles(){
        return null;
    }

    public DefinedQuery<TextChannel> getRestrictedChannels(){
        return null;
    }

    public Requirement[] getExtraRequirements(){
        return new Requirement[0];
    }

    public Requirement[] getRequirements(){
        LinkedList<Requirement> list = new LinkedList<>();
        DefinedQuery<Role> roles = this.getRestrictedRoles();
        DefinedQuery<TextChannel> channels = this.getRestrictedChannels();
        Requirement[] extra = this.getExtraRequirements();
        if (roles != null) {
            list.add(new Requirement(roles, 1, "No Roles found which are suitable for running this Command (Missing Restricted Roles)"));
        }

        if (channels != null) {
            list.add(new Requirement(channels, 1, "No Channels found which are suitable for running this Command (Missing Restricted Channels)"));
        }
        
        if (extra != null) {
            list.addAll(Arrays.asList(extra));
        }

        return list.toArray(new Requirement[0]);
    }

    public void onEnable(){

    }

    public void onDisable(){

    }

    public abstract void onExecute(MessageReceivedEvent event, TextChannel textChannel, Message message, User author, Member member, String alias, String[] args, long time);
    
}
