package io.github.itzvgcgpmo.grappl4bungee;

import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class ListenerClass implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLogin(PreLoginEvent ple)
    {
        System.out.println("PreLoginEvent");
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLogin(PostLoginEvent ple)
    {
        System.out.println("PostLoginEvent");
    }
}
