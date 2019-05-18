package jp.azisaba.lgw.ecplus.commands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import jp.azisaba.lgw.ecplus.InventoryLoader;
import net.md_5.bungee.api.ChatColor;

public class EnderChestPlusCommand implements CommandExecutor {

	private final InventoryLoader loader;

	public EnderChestPlusCommand(InventoryLoader loader) {
		this.loader = loader;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return true;
		}
		Player p = (Player) sender;

		if (args.length <= 0) {
			p.sendMessage(ChatColor.RED + "Usage: " + cmd.getUsage().replace("{LABEL}", label));
			return true;
		}

		Player target = Bukkit.getPlayerExact(args[0]);
		UUID uuid = null;
		try {
			uuid = UUID.fromString(args[0]);
		} catch (Exception e) {
			// pass
		}

		if (target == null && uuid == null) {
			p.sendMessage(ChatColor.RED + "現在オンラインのプレイﾔｰ名かUUIDを入力してください。");
			return true;
		}

		if (target != null) {
			uuid = target.getUniqueId();
		}

		loader.setLookingAt(p, uuid);
		p.openInventory(InventoryLoader.getMainInventory(loader.getInventoryData(uuid)));
		return true;
	}
}
