package cl.franciscosolis.discordbotbase.modules;

import java.util.Arrays;
import java.util.LinkedHashMap;

import cl.franciscosolis.discordbotbase.DiscordBotBase;
import cl.franciscosolis.discordbotbase.objects.Cooldown;
import cl.franciscosolis.discordbotbase.objects.Requirement;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;

public abstract class SlashCommand {

    public final LinkedHashMap<String, Cooldown> cooldowns = new LinkedHashMap<>();
    public final DiscordBotBase bot = DiscordBotBase.i;
    private boolean enabled = false;
    public boolean onlyVisibleToUser = false, deferReply = false;
    public final String identifier;

    public SlashCommand(){
        this.identifier = "[" + this.command() + " Slash Command]";
    }

    public abstract String command();

    public abstract String description();

    public void enable(){
        Requirement[] failed = Arrays.stream(this.getRequirements()).filter(it -> !it.check()).toArray(Requirement[]::new);
        if (failed.length > 0) {
            System.out.println("Failed requirements for SlashCommand " + identifier + ": ");
            for (Requirement r : failed) {
                System.out.println("- " + r.getUnmatchMessage());
            }
        }else{
            this.onEnable();
            this.enabled = true;
            bot.jda.addEventListener(this);
            System.out.println("SlashCommand " + identifier + " enabled");
        }
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public Requirement[] getRequirements(){
        return new Requirement[0];
    }

    public CommandPrivilege[] getCommandPrivileges(){
        return new CommandPrivilege[0];
    }

    public OptionData[] getOptions(){
        return new OptionData[0];
    }

    public int getCooldown(){
        return 0;
    }

    public void onEnable(){

    }

    public void onDisable(){

    }

    public abstract void onCommand(TextChannel channel, Member member, SlashCommandEvent event, long timeTook);
    
}
