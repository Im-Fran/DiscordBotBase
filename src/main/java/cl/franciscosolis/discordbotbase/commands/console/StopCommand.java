package cl.franciscosolis.discordbotbase.commands.console;

import cl.franciscosolis.discordbotbase.modules.ConsoleCommand;

public class StopCommand extends ConsoleCommand {

    @Override
    public String command() {
        return "stop";
    }

    @Override
    public String[] help() {
        return new String[]{"Stops the Bot"};
    }

    @Override
    public void onExecute(String[] args) {
        System.exit(0);
    }
    
}
