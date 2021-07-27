package cl.franciscosolis.discordbotbase.modules;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import cl.franciscosolis.discordbotbase.DiscordBotBase;
import cl.franciscosolis.discordbotbase.utils.ProjectUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

public class ModuleManager {

    public static ModuleManager i;
    private final DiscordBotBase bot = DiscordBotBase.i;
    public final LinkedList<Module> modules = new LinkedList<>();
    public final LinkedList<Command> commands = new LinkedList<>();
    public final LinkedList<SlashCommand> slashCommands = new LinkedList<>();
    public final LinkedHashMap<String, Integer> cmd_cache = new LinkedHashMap<>();

    public ModuleManager(){
        i = this;
        this.load();
    }

    public void load(){
        this.modules.clear();
        this.commands.clear();
        this.slashCommands.clear();
        this.cmd_cache.clear();

        DiscordBotBase.config.add("CommandPrefix", "!");
        bot.jda.updateCommands().queue();
        List<CommandData> cmdDataToAdd = new ArrayList<>();
        for(String prefix : bot.packages){
            for(Class<?> clazz : ProjectUtil.getClasses(prefix)){
                // Check if clazz is assignable from Module and is not abstract
                if(Module.class.isAssignableFrom(clazz) && clazz != Module.class && !Modifier.isAbstract(clazz.getModifiers())){
                    try {
                        Module module = ((Module) clazz.getConstructor().newInstance());
                        module.enable();
                        this.modules.add(module);
                    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                        e.printStackTrace();
                    }
                }else if(Command.class.isAssignableFrom(clazz) && clazz != Command.class && !Modifier.isAbstract(clazz.getModifiers())){
                    try{
                        Command cmd = ((Command) clazz.getConstructor().newInstance());
                        cmd.enable();
                        this.commands.add(cmd);
                    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                        e.printStackTrace();
                    }
                }else if(SlashCommand.class.isAssignableFrom(clazz) && clazz != SlashCommand.class && !Modifier.isAbstract(clazz.getModifiers())){
                    try {
                        SlashCommand cmd = ((SlashCommand) clazz.getConstructor().newInstance());
                        cmd.enable();
                        this.slashCommands.add(cmd);
    
                        CommandData cmdData = new CommandData(cmd.command(), (cmd.description() != null ? cmd.description() : "No description provided."))
                            .addOptions(cmd.getOptions())
                            .setDefaultEnabled(cmd.getCommandPrivileges().length == 0);
                        cmdDataToAdd.add(cmdData);
                    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        for(Guild guild : bot.jda.getGuilds()){
            CommandListUpdateAction commands = guild.updateCommands();
            cmdDataToAdd.forEach(commands::addCommands);
            commands.queue(cmds -> {
                cmds.forEach(cmd -> {
                    CommandPrivilege[] commandPrivileges = this.slashCommands.stream().filter(c -> c.command().equals(cmd.getName())).map(SlashCommand::getCommandPrivileges).findFirst().orElse(new CommandPrivilege[0]);
                    if(commandPrivileges.length > 0){
                        guild.updateCommandPrivilegesById(cmd.getId(), commandPrivileges).queue();
                    }
                });
            });
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> this.modules.forEach(Module::onDisable)));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> this.commands.forEach(Command::onDisable)));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> this.slashCommands.forEach(SlashCommand::onDisable)));
    }

    @SubscribeEvent
    public void onSlash(SlashCommandEvent e) {
        long start = System.currentTimeMillis();
        SlashCommand cmd = this.slashCommands.stream().filter(c -> c.command().equalsIgnoreCase(e.getName())).findFirst().orElse(null);
        if(cmd == null || e.getMember() == null || e.getUser().isBot()) return;

        if(cmd.getCooldown() > 0 && cmd.cooldowns.containsKey(e.getUser().getId())){
            Cooldown cooldown = cmd.cooldowns.get(e.getMember().getId());
            if(cooldown.isCooldownRemaining()){
                e.deferReply(true).queue();
                e.reply("**Hey! Slow down!!** There's stil **" + cooldown.getRemainingCooldown() + "** seconds left on your cooldown!").queue();
                return;
            }
            cmd.cooldowns.remove(e.getMember().getId());
        }
        if(cmd.deferReply) e.deferReply(cmd.onlyVisibleToUser).queue();
        if((System.currentTimeMillis() - start) >= 500){
            System.out.println("WARNING: RESPONSE TIME FOR SLASG COMMAND '" + cmd.command() + "' IS HIGHER THAN 500ms!");)
        }
        cmd.onCommand(e.getTextChannel(), e.getMember(), e, start);
    }

    @SubscribeEvent
    public void onChat(MessageReceivedEvent e){
        long start = System.currentTimeMillis();
        if(e.getMember() == null) return;
        if(e.getAuthor().isBot()) return;
        String cmdPrefix = DiscordBotBase.config.getString("CommandPrefix");
        if(e.getMessage().getContentDisplay().startsWith(cmdPrefix)){
            Message message = e.getMessage();
            Member member = e.getMember();
            User author = e.getAuthor();
            String[] data = message.getContentDisplay().split(" ");
            String cmd = (data.length == 0 ? "" : data[0]).replaceFirst(cmdPrefix, "");
            if(!this.cmd_cache.containsKey(cmd)){
                for(int i = 0; i < this.commands.size(); ++i){
                    Command command = this.commands.get(i);
                    if(command.command().toLowerCase().equals(cmd) || Arrays.stream(command.aliases()).anyMatch(it -> it.toLowerCase().equals(cmd))){
                        this.cmd_cache.put(cmd, i);
                    }
                }
            }
            Command command = this.commands.get(this.cmd_cache.getOrDefault(cmd, -1));
            if(command != null){
                if(command.sendTyping){
                    message.getTextChannel().sendTyping().queue();
                }

                if(command.deleteOnExecution){
                    message.delete().submit();
                }

                // Check restricted roles and channels
                LinkedList<Role> restrictedRoles = new LinkedList<>();
                if(command.getRestrictedRoles() != null){
                    Query<Role> roles = command.getRestrictedRoles().query();
                    roles.all().forEach(restrictedRoles::add);
                }

                if(!restrictedRoles.isEmpty() && Collections.disjoint(member.getRoles(), restrictedRoles)){
                    CustomEmbedBuilder.err("You don't have the permissions to use this command!").sendTemporary(message.getTextChannel(), 15);
                    return;
                }

                LinkedList<TextChannel> restrictedChannels = new LinkedList<>();
                if(command.getRestrictedChannels() != null){
                    Query<TextChannel> channels = command.getRestrictedChannels().query();
                    channels.all().forEach(restrictedChannels::add);
                }

                if(!restrictedChannels.isEmpty() && !restrictedChannels.contains(message.getTextChannel())){
                    CustomEmbedBuilder.err("You can't use this command here!").sendTemporary(message.getTextChannel(), 15);
                    return;
                }

                Cooldown cooldown  = command.cooldowns.get(member.getId());
                if(cooldown != null && cooldown.isCooldownRemaining()){
                    CustomEmbedBuilder.err("You still need to wait another **" + cooldown.getRemainingCooldown() + "** seconds before using the command again!").sendTemporary(message.getTextChannel(), 15);
                    return;
                }

                command.cooldowns.remove(member.getId());

                String[] args = data.length == 0 ? new String[0] : Arrays.copyOfRange(data, 1, data.length);
                if((System.currentTimeMillis() - start) >= 500){
                    System.out.println("WARNING: RESPONSE TIME FOR CHAT COMMAND '" + command.command() + "' IS HIGHER THAN 500ms!");)
                }
                command.onExecute(e, message.getTextChannel(), message, author, member, cmd, args, start);
            }
        }
    }
    
}
