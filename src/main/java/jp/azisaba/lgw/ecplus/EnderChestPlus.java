package jp.azisaba.lgw.ecplus;

import java.io.File;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import jp.azisaba.lgw.ecplus.commands.EnderChestPlusCommand;
import jp.azisaba.lgw.ecplus.listeners.BuyInventoryListener;
import jp.azisaba.lgw.ecplus.listeners.EnderChestListener;
import jp.azisaba.lgw.ecplus.listeners.LoadInventoryDataListener;
import net.md_5.bungee.api.ChatColor;

public class EnderChestPlus extends JavaPlugin {

	public static final String enderChestTitlePrefix = ChatColor.RED + "EnderChest" + ChatColor.AQUA + "+";
	public static final String mainEnderChestTitle = enderChestTitlePrefix + " " + ChatColor.GREEN + "- "
			+ ChatColor.YELLOW + "Main";
	private static PluginConfig config;
	public static File inventoryDataFile;

	private InventoryLoader loader = null;

	@Override
	public void onEnable() {

		inventoryDataFile = new File(getDataFolder(), "Inventories");
		loader = new InventoryLoader(this);

		EnderChestPlus.config = new PluginConfig(this);
		EnderChestPlus.config.loadConfig();

		if (Bukkit.getOnlinePlayers().size() > 0) {
			Bukkit.getOnlinePlayers().forEach(p -> {
				loader.loadInventoryData(p);
			});
		}

		Bukkit.getPluginManager().registerEvents(new EnderChestListener(this, loader), this);
		Bukkit.getPluginManager().registerEvents(new LoadInventoryDataListener(loader), this);
		Bukkit.getPluginManager().registerEvents(new BuyInventoryListener(loader), this);

		Bukkit.getPluginCommand("enderchestplus").setExecutor(new EnderChestPlusCommand(loader));
		Bukkit.getPluginCommand("enderchestplus").setPermissionMessage(config.chatPrefix + ChatColor.RED + "権限がありません！");

		Bukkit.getLogger().info(getName() + " enabled.");
	}

	@Override
	public void onDisable() {

		Bukkit.getOnlinePlayers().forEach(player -> {
			if (player.getOpenInventory() == null) {
				return;
			}
			if (player.getOpenInventory().getTopInventory() == null) {
				return;
			}
			if (player.getOpenInventory().getTopInventory().getTitle().startsWith(enderChestTitlePrefix)) {
				ItemStack item = player.getOpenInventory().getCursor();
				if (item != null) {
					boolean success = false;
					HashMap<Integer, ItemStack> slot = player.getInventory().addItem(item);

					if (!slot.isEmpty()) {
						slot = player.getOpenInventory().getTopInventory().addItem(item);
						Bukkit.getLogger().info(slot.toString());

						if (slot.isEmpty()) {
							success = true;
						}
					} else {
						success = true;
					}

					if (success) {
						player.getOpenInventory().setCursor(null);
					} else {
						player.sendMessage(ChatColor.RED + "インベントリにもエンチェスにもアイテムが入らなかったため、地面にドロップしました。");
					}
				}
				player.closeInventory();
			}
		});

		loader.saveAllInventoryData();

		Bukkit.getLogger().info(getName() + " disabled.");
	}

	public void reloadPluginConfig() {

		reloadConfig();

		EnderChestPlus.config = new PluginConfig(this);
		EnderChestPlus.config.loadConfig();
	}

	public static PluginConfig getPluginConfig() {
		return config;
	}
}
