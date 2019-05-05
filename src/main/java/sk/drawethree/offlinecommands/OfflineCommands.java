package sk.drawethree.offlinecommands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public final class OfflineCommands extends JavaPlugin implements CommandExecutor, Listener {

    private static OfflineCommands instance;
    private static final HashMap<UUID, ArrayList<String>> pendingCommands = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();
        this.loadData();
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        this.saveData();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!pendingCommands.containsKey(e.getPlayer().getUniqueId())) {
            return;
        }

        ArrayList<String> commandsToBeRun = pendingCommands.get(e.getPlayer().getUniqueId());

        if (commandsToBeRun == null || commandsToBeRun.isEmpty()) {
            return;
        }

        for (String cmd : commandsToBeRun) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        }
    }

    private void addPendingCommand(OfflinePlayer player, String command) {
        ArrayList<String> commands;
        if (pendingCommands.containsKey(player.getUniqueId())) {
            commands = pendingCommands.get(player.getUniqueId());
        } else {
            commands = new ArrayList<>();
        }
        commands.add(command);

        pendingCommands.put(player.getUniqueId(), commands);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("offlinecommands")) {
            return false;
        }

        if (!sender.hasPermission("offlinecommands.add")) {
            sender.sendMessage(Message.NO_PERM.getMessageWithPrefix());
            return false;
        }

        if (args.length > 1) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);

            if (player.isOnline()) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), this.getCommandToBeRun(args));
                return false;
            }

            String cmd = this.getCommandToBeRun(args);
            this.addPendingCommand(player, cmd);
            sender.sendMessage(Message.CMD_ADDED.getMessageWithPrefix().replaceAll("%player%", player.getName()).replaceAll("%cmd%", cmd));
            return true;
        }

        return false;
    }

    private String getCommandToBeRun(String[] args) {
        String s = "";
        for (int i = 1; i < args.length; i++) {
            s += args[i] + " ";
        }
        return s;

    }

    private void saveData() {
        this.getConfig().set("data", null);
        for (UUID key : pendingCommands.keySet()) {
            this.getConfig().set("data." + key.toString(), pendingCommands.get(key));
        }
        this.saveConfig();
    }

    private void loadData() {
        for (String key : this.getConfig().getConfigurationSection("data").getKeys(false)) {
            UUID uuid = UUID.fromString(key);
            List<String> commands = this.getConfig().getStringList("data." + key);
            pendingCommands.put(uuid, new ArrayList<>(commands));
        }
    }

    public static OfflineCommands getInstance() {
        return instance;
    }
}
