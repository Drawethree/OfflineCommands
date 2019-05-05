package sk.drawethree.offlinecommands;

import org.bukkit.ChatColor;

public enum Message {

    PREFIX,
    NO_PERM,
    CMD_ADDED;
    private String message;

    Message() {
        this.message = ChatColor.translateAlternateColorCodes('&', OfflineCommands.getInstance().getConfig().getString("messages." + this.name().toLowerCase()));
    }

    public String getMessage() {
        return message;
    }

    public String getMessageWithPrefix() {
        return Message.PREFIX.getMessage() + this.message;
    }
}
