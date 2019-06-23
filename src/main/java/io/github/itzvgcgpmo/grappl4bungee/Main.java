package io.github.itzvgcgpmo.grappl4bungee;

import io.github.itzvgcgpmo.grappl4bungee.Utils.YamlGenerator;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class Main extends Plugin {
    Timer timer = new Timer();
    public static HashMap<String, Integer> login_limit = new HashMap<>();
    public static Plugin plugin;
    Thread grapplThread = new Thread(new Runnable() {
        @Override
        public void run() {
            while(true) {
                try {
                    com.daexsys.grappl.server.Server.startServer();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    });
    public static String authwith;
    public static Boolean req_auth, srv_restr;
    public static String df_srvnm, srv_motd, log_pref, srv_add, srv_del, gr_start, hst_conn, hst_hosting, hst_close;
    public void onEnable()
    {
        plugin = this;
        YamlGenerator yg = new YamlGenerator();
        yg.saveDefaultConfig();
        loadYaml();
        PluginManager plmng = getProxy().getPluginManager();
        plmng.registerListener(this, new ListenerClass());
        plmng.registerCommand(this, new CommandGrappl());
        // https://github.com/makzk/Grappl/tree/f93af91800dc0ec752a9cf887992f12ee1c4e967
        grapplThread.start();
        // every hour, decrement login limits by 1
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                for(String currentKey : login_limit.keySet()) {
                    Integer num = login_limit.get(currentKey);
                    if (num <= 0) {
                        login_limit.remove(currentKey);
                    } else {
                        login_limit.replace(currentKey, num-1);
                    }
                }
            }
        }, 60*60*1000, 60*60*1000);
    }
    private void loadYaml()
    {
        authwith = YamlGenerator.config.getString("AuthWith");
        req_auth = YamlGenerator.config.getBoolean("Require Authentication");
        srv_restr = YamlGenerator.config.getBoolean("Servers Restricted");

        log_pref = YamlGenerator.message.getString("log_pref");
        df_srvnm = YamlGenerator.message.getString("df_srvnm");
        srv_motd = YamlGenerator.message.getString("srv_motd");
        srv_add = YamlGenerator.message.getString("srv_add");
        srv_del = YamlGenerator.message.getString("srv_del");
        gr_start = YamlGenerator.message.getString("gr_start");
        hst_conn = YamlGenerator.message.getString("hst_conn");
        hst_hosting = YamlGenerator.message.getString("hst_hosting");
        hst_close = YamlGenerator.message.getString("hst_close");
    }

}