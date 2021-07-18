package cl.franciscosolis.discordbotbase;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.UUID;

import cl.franciscosolis.discordbotbase.modules.ModuleManager;
import cl.franciscosolis.discordbotbase.utils.Config;
import cl.franciscosolis.discordbotbase.utils.ConsoleInputManager;
import cl.franciscosolis.discordbotbase.utils.Utils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.requests.GatewayIntent;

public abstract class DiscordBotBase {

    public static DiscordBotBase i;
    public static String apiVersion = "4.0.0-SNAPSHOT";
    public static Config config = new Config(new File("Config.yml"));

    public final LinkedList<String> packages = new LinkedList<>();
    public JDA jda;

    public DiscordBotBase() {
        i = this;
        this.packages.add("cl.franciscosolis.discordbotbase");
    }

    public void init(){
        try{
            config.add("BotToken", "none");
            String token = config.getString("BotToken");
            if(!config.contains("BotToken") || token.equals("none")){
                System.err.println("Please fill in the 'BotToken' field in 'Config.yml'");
            }else{
                if(!Utils.isConnected()) {
                    System.err.println("Please connect the bot to internet!");
                    return;
                }
                System.out.println("Loading JDA");
                this.jda = JDABuilder.create(token, Arrays.asList(GatewayIntent.values()))
                    .setEventManager(new AnnotatedEventManager())
                    .build()
                    .awaitReady();
                System.out.println("Loading Managers...");
                ModuleManager manager = new ModuleManager();
                this.jda.addEventListener(manager);
                ConsoleInputManager console = new ConsoleInputManager();
                System.out.println("===========");
                System.out.println("Loaded Bot API Version: " + UUID.nameUUIDFromBytes(apiVersion.getBytes()).toString().split("-")[0]);
                System.out.println("Loaded Bot Version: " + UUID.nameUUIDFromBytes(getVersion().getBytes()).toString().split("-")[0]);
                System.out.println("===========");
                console.initHandler();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    // Create abstract method that holds the bot version
    public abstract String getVersion();
    
}
