package io.illyria.factionsx;

import io.illyria.factionsx.config.Config;
import io.illyria.factionsx.config.Message;
import io.illyria.factionsx.internal.FactionsBootstrap;
import io.illyria.factionsx.utils.ChatUtil;
import io.illyria.factionsx.utils.hooks.Econ;
import io.illyria.factionsx.utils.hooks.PlaceholderAPIHook;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Here is the bukkit implementation of Factions plugin.
 * Anything related to bukkit goes here.
 */

public class BukkitFactionsBootstrap extends JavaPlugin implements FactionsBootstrap {

    private static BukkitFactionsBootstrap bukkitFactionsBootstrap;
    private FactionsX factionsX = new FactionsX(this);

    private Set<String> enabledHooks = new HashSet<>();

    @Override
    public void onEnable() {
        bukkitFactionsBootstrap = this;
        loadConfig();
        loadHooks();
        factionsX.enable();
    }

    @Override
    public void onDisable() {
        // Cancel running Tasks, so that it should be PlugMan-safe
        Bukkit.getServer().getScheduler().cancelTasks(this);
        // Unregister PAPI, so that it should be PlugMan-safe
        if (enabledHooks.contains("PlaceholderAPI")) {
            PlaceholderAPIHook.unreg();
        }
        // Set the saved instance to null, saving memory
        bukkitFactionsBootstrap = null;
        // Clearing the enabledHooks list
        enabledHooks.clear();
        factionsX.disable();
    }

    @Override
    public String getVersion() {
        return Bukkit.getVersion();
    }

    @Override
    public void unload() {

    }

    public void loadConfig() {
        factionsX.getConfigManager().getFileMap().get("config").init();
        factionsX.getConfigManager().getFileMap().get("messages").init();
    }

    private void registerListeners(Listener... listeners) {
        for (Listener listener : listeners) {
            getServer().getPluginManager().registerEvents(listener, bukkitFactionsBootstrap);
        }
    }

    public void loadHooks() {
        Bukkit.getScheduler().runTaskLater(this, () -> {
            // Load hooks with a delay, because plugins sometimes load before us even if
            // they are in the softdepend list, don't know why. This way we're 100% sure.

            // PlaceholderAPI hook - adds placeholders
            if (checkHook("PlaceholderAPI")) {
                new PlaceholderAPIHook(this).register();
            }

            // Economy Hook (Vault), try to hook even if the Econ is disabled in config
            // so that if the user enables it after the plugin is loaded, it will work
            // without restarting the server.
            if (checkHook("Vault")) {
                if (!Econ.setup(this)) {
                    enabledHooks.remove("Vault");
                    if (Config.USE_ECONOMY.getBoolean()) {
                        ChatUtil.error(Message.ERROR_ECON_INVALID.getMessage());
                    } else {
                        ChatUtil.debug(Message.ERROR_ECON_INVALID.getMessage());
                    }
                }
            }

            if (!enabledHooks.isEmpty())
                ChatUtil.sendConsole(Message.PREFIX.getMessage() + "&e" + getName() + " Hooked to: &f" + enabledHooks.toString().replaceAll("\\[\\]", ""));

        }, 2);
    }

    private boolean checkHook(String pluginName) {
        if (Bukkit.getPluginManager().isPluginEnabled(pluginName)) {
            enabledHooks.add(pluginName);
            return true;
        } else {
            ChatUtil.debug(Message.ERROR_HOOK_FAILED.getMessage().replace("{plugin}", pluginName));
        }
        return false;
    }

    public Set<String> getEnabledHooks() {
        return enabledHooks;
    }

    @Override
    public File getBootstrapDataFolder() {
        return this.getDataFolder();
    }

    public FactionsX getFactionsX() {
        return factionsX;
    }

    public static BukkitFactionsBootstrap getInstance() {
        return bukkitFactionsBootstrap;
    }

}
