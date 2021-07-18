package cl.franciscosolis.discordbotbase.modules;

import cl.franciscosolis.discordbotbase.DiscordBotBase;
import net.dv8tion.jda.api.JDA;

public abstract class ConsoleCommand {

    public final DiscordBotBase bot = DiscordBotBase.i;
    public final JDA jda = bot.jda;

    public abstract String command();

    public String[] aliases(){
        return new String[0];
    }
    
    public abstract String[] help();

    public abstract void onExecute(String[] args);

    public void onEnable(){

    }

    public void onDisable(){
        
    }
    
}
