package io.github.itzvgcgpmo.grappl4bungee;

import com.daexsys.grappl.server.Host;
import com.daexsys.grappl.server.Server;
import me.vik1395.BungeeAuthAPI.RequestHandler;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.plugin.Command;

import java.util.Arrays;
import java.util.List;


public class CommandGrappl extends Command {
    public CommandGrappl() { super("grappl","grappl4bungee.command.grappl", "gr", "grappl4bungee", "grapple"); }

    public void execute(CommandSender sender, String[] args)
    {
        sender.sendMessage(new ComponentBuilder("Command ran by "+sender.getName()).color(ChatColor.RED).create());
//        sender.sendMessage(new ComponentBuilder(args[0]).color(ChatColor.RED).create());
        if (args.length > 0)
        {
            if (args[0].equalsIgnoreCase("login")) {
                sender.sendMessage(new ComponentBuilder("test login...").create());
                if (Main.login_limit.getOrDefault(sender.getName(), 0) > 3){
                    sender.sendMessage(new ComponentBuilder("rate-limited!").create());
                } else {
                    if (new RequestHandler().checkPassword(sender.getName(), args[1])){
                        Main.login_limit.remove(sender.getName());
                        sender.sendMessage(new ComponentBuilder("correct password!").create());
                    } else {
                        Main.login_limit.put(sender.getName(), Main.login_limit.getOrDefault(sender.getName(), 0)+1);
                        sender.sendMessage(new ComponentBuilder("invalid password!").create());
                        sender.sendMessage(new ComponentBuilder(Main.login_limit.get(sender.getName())+"").create());
                    }
                }
                return;
            }
            if (args[0].equalsIgnoreCase("hosts") || args[0].equalsIgnoreCase("h")) {
                sender.sendMessage(new ComponentBuilder(Server.hosts.size()+" host(s)").create());
                return;
            }

            else if(args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("l")) {
                String output = Server.hosts.size()+" host(s):";

                for (int i = 0; i < Server.hosts.size(); i++) {
                    if(i != 0) {
                        output += " - ";
                    }
                    Host host = Server.hosts.get(i);

                    output += host.getAddress() + ":" + host.getPortNumber();
                }
                sender.sendMessage(new ComponentBuilder(output).create());
                return;
            }
//          currently bugs out and causes connection loop when trying to debind
//            else if(args[0].equalsIgnoreCase("debind") || args[0].equalsIgnoreCase("d")) {
//                if (args.length > 1){
//                    try {
//                        Server.getHost(Integer.parseInt(args[1])).closeHost();
//                        sender.sendMessage(new ComponentBuilder("successful debind").create());
//                    } catch (Exception er) {
//                        sender.sendMessage(new ComponentBuilder("An error occoured.").color(ChatColor.RED).create());
//                    }
//                } else {
//                    sender.sendMessage(new ComponentBuilder("How can i debind something, without something to debind?").color(ChatColor.RED).create());
//                }
//                return;
//            }
//            else if(args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("r")) {
//                Server.log("attempting reload");
//                return;
//            }
        }
        sender.sendMessage(new ComponentBuilder("GrapplServer command list:").color(ChatColor.YELLOW).create());
        List<List<String>> cmdList = Arrays.asList(Arrays.asList("hosts", "h", "Amount of hostings"),
                Arrays.asList("list", "l", "List all hostings"),
                Arrays.asList("debind <host>", "d", "Debind (remove) a hosting"),
                Arrays.asList("reload", "r", "Reload (restart) GrapplServer"));
        for(List cmd: cmdList) {
            sender.sendMessage(new ComponentBuilder("("+cmd.get(1)+") "+cmd.get(0)+" "+cmd.get(2)).color(ChatColor.YELLOW).create());
        }
    }
}