package cl.franciscosolis.discordbotbase.modules;

import java.util.Arrays;

import cl.franciscosolis.discordbotbase.DiscordBotBase;
import cl.franciscosolis.discordbotbase.objects.Requirement;

public class Module {

    public final DiscordBotBase bot = DiscordBotBase.i;
    private boolean enabled = false;

    public final String identifier;

    public Module(String identifier){
        this.identifier = identifier;
    }

    public void enable(){
        Requirement[] failed = Arrays.stream(this.getRequirements()).filter(it -> !it.check()).toArray(Requirement[]::new);
        if (failed.length > 0) {
            System.out.println("Failed requirements for module " + identifier + ": ");
            for (Requirement r : failed) {
                System.out.println("- " + r.getUnmatchMessage());
            }
        }else{
            this.onEnable();
            this.enabled = true;
            System.out.println("Module " + identifier + " enabled");
        }
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public Requirement[] getRequirements(){
        return new Requirement[0];
    }

    public void onEnable(){

    }

    public void onDisable(){

    }
    
}
