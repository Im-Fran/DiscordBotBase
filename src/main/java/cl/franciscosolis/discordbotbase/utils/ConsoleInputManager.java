package cl.franciscosolis.discordbotbase.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Optional;

import cl.franciscosolis.discordbotbase.DiscordBotBase;
import cl.franciscosolis.discordbotbase.modules.ConsoleCommand;

public class ConsoleInputManager {

    public final LinkedList<ConsoleCommand> commands = new LinkedList<>();

    public ConsoleInputManager(){
        for(String prefix : DiscordBotBase.i.packages){
            try{
                for(Class<?> clazz : ProjectUtil.getClasses(prefix)){
                    // Check if clazz is assignable from ConsoleCommand.
                    if(ConsoleCommand.class.isAssignableFrom(clazz) && !Modifier.isAbstract(clazz.getModifiers())){
                        ConsoleCommand command = (ConsoleCommand) clazz.getConstructor().newInstance();
                        command.onEnable();
                        commands.add(command);
                        System.out.println("Loaded Console Command [" + command.command() + "]");
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        // Add shutdown hook that loops over every console command and call the method onDisable
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for(ConsoleCommand command : commands){
                command.onDisable();
            }
        }));
    }

    public void initHandler(){
        while(true){
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                // Parse console input to string
                String line = (reader).readLine();
                if(line == null) continue;
                String[] data = line.split(" ");
                String cmd = data.length == 0 ? "" : data[0].toLowerCase();
                String[] args = data.length == 0 ? new String[0] : Arrays.copyOfRange(data, 1, data.length);
                // Filter commands and find the one that matches 'cmd'
                Optional<ConsoleCommand> optional = this.commands.stream().filter(it -> it.command().equals(cmd) || Arrays.stream(it.aliases()).anyMatch(alias -> alias.toLowerCase().equals(cmd))).findFirst();
                if(optional.isPresent()){
                    optional.get().onExecute(args);
                }else{
                    System.out.println("Unknown command");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
}
