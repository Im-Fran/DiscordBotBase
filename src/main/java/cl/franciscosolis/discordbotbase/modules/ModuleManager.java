package cl.franciscosolis.discordbotbase.modules;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import cl.franciscosolis.discordbotbase.DiscordBotBase;
import cl.franciscosolis.discordbotbase.objects.Cooldown;
import cl.franciscosolis.discordbotbase.objects.query.Query;
import cl.franciscosolis.discordbotbase.utils.CustomEmbedBuilder;
import cl.franciscosolis.discordbotbase.utils.ProjectUtil;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

public class ModuleManager {

    public static ModuleManager i;
    private final DiscordBotBase bot = DiscordBotBase.i;
    public final LinkedList<Module> modules = new LinkedList<>();
    public final LinkedList<Command> commands = new LinkedList<>();
    public final LinkedHashMap<String, Integer> cmd_cache = new LinkedHashMap<>();

    public ModuleManager(){
        i = this;
        this.load();
    }

    public void load(){
        this.modules.clear();
        this.commands.clear();
        this.cmd_cache.clear();

        DiscordBotBase.config.add("CommandPrefix", "!");
        for(String prefix : bot.packages){
            try{
                for(Class<?> clazz : ProjectUtil.getClasses(prefix)){
                    // Check if clazz is assignable from Module and is not abstract
                    if(Module.class.isAssignableFrom(clazz) && clazz != Module.class && !Modifier.isAbstract(clazz.getModifiers())){
                        Module module = ((Module) clazz.getConstructor().newInstance());
                        module.enable();
                        this.modules.add(module);
                    }else if(Command.class.isAssignableFrom(clazz) && clazz != Command.class && !Modifier.isAbstract(clazz.getModifiers())){
                        Command cmd = ((Command) clazz.getConstructor().newInstance());
                        cmd.enable();
                        this.commands.add(cmd);
                    }
                }
            }catch(Exception e){
                continue;
            }
        }
        this.bot.jda.addEventListener(this.modules);
        this.bot.jda.addEventListener(this.commands);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> this.modules.forEach(Module::onDisable)));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> this.commands.forEach(Command::onDisable)));
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
                long time = System.currentTimeMillis() - start;
                command.onExecute(e, message.getTextChannel(), message, author, member, cmd, args, time);
            }
        }
    }
    
}
