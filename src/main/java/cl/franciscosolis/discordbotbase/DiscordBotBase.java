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
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public abstract class DiscordBotBase {

    public static void main(String[] args) {
        new DiscordBotBase(){
            public String getVersion() {
                return "1.0.0-SNAPSHOT";
            };

            @Override
            public Activity getActivity() {
                return Activity.playing("With tests");
            }

            @Override
            public GatewayIntent[] getEnabledGatewayIntents() {
                return GatewayIntent.values();
            }

            @Override
            public CacheFlag[] getEnabledCacheFlags() {
                return CacheFlag.values();
            }
        }.init();
    }

    public static DiscordBotBase i;
    public static String apiVersion = "4.0.0-SNAPSHOT";
    public static Config config = new Config(new File("Config.yml"));

    public final LinkedList<String> packages = new LinkedList<>();
    public JDA jda;
    public Guild mainGuild;

    public DiscordBotBase() {
        i = this;
        this.packages.add("cl.franciscosolis.discordbotbase.commands");
    }

    public void init(){
        try{
            config.add("SetupMode", true);
            config.add("BotToken", "none");
            config.add("Guild", "000000000000000000");
            if(config.getBoolean("SetupMode")){
                System.err.println("Setup mode enabled. Please disable before continuing.");
            }else{
                String token = config.getString("BotToken");
                if(!Utils.isConnected()) {
                    System.err.println("Please connect the bot to internet!");
                    return;
                }
                System.out.println("Loading JDA");
                this.jda = JDABuilder.createDefault(token)
                    .enableIntents(Arrays.asList(this.getEnabledGatewayIntents()))
                    .disableIntents(Arrays.asList(this.getDisabledGatewayIntents()))
                    .enableCache(Arrays.asList(this.getEnabledCacheFlags()))
                    .disableCache(Arrays.asList(this.getDisabledCacheFlags()))
                    .setMemberCachePolicy(this.getMemberCachePolicy())
                    .setChunkingFilter(this.getChunkingFilter())
                    .setActivity(this.getActivity())
                    .setEventManager(new AnnotatedEventManager())
                    .build()
                    .awaitReady();
                mainGuild = jda.getGuildById(config.getString("Guild"));
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

    public abstract String getVersion();

    public abstract Activity getActivity();

    public GatewayIntent[] getEnabledGatewayIntents(){
        return new GatewayIntent[0];
    }

    public GatewayIntent[] getDisabledGatewayIntents(){
        return new GatewayIntent[0];
    }

    public CacheFlag[] getEnabledCacheFlags(){
        return new CacheFlag[0];
    }

    public CacheFlag[] getDisabledCacheFlags(){
        return new CacheFlag[0];
    }

    public MemberCachePolicy getMemberCachePolicy(){
        return MemberCachePolicy.ALL;
    }

    public ChunkingFilter getChunkingFilter(){
        return ChunkingFilter.ALL;
    }
    
}
