package io.github.itzvgcgpmo.grappl4bungee.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import io.github.itzvgcgpmo.grappl4bungee.Main;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class YamlGenerator
{
    public static Configuration config;
    public static Configuration message;

    public void saveDefaultConfig()
    {
        File file = new File(Main.plugin.getDataFolder(), "config.yml");
        ConfigurationProvider cProvider = ConfigurationProvider.getProvider(YamlConfiguration.class);

        if (!Main.plugin.getDataFolder().exists())
        {
            Main.plugin.getDataFolder().mkdir();
        }
        try
        {
            if (!file.exists())
            {
                Files.copy(Main.plugin.getResourceAsStream("config.yml"), file.toPath());
            }
            config = cProvider.load(file);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void saveDefaultMessage()
    {
        File file = new File(Main.plugin.getDataFolder(), "messages.yml");
        ConfigurationProvider cProvider = ConfigurationProvider.getProvider(YamlConfiguration.class);

        if (!Main.plugin.getDataFolder().exists())
        {
            Main.plugin.getDataFolder().mkdir();
        }
        try
        {
            if (!file.exists())
            {
                Files.copy(Main.plugin.getResourceAsStream("messages.yml"), file.toPath());
            }
            message = cProvider.load(file);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
