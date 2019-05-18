package jp.azisaba.lgw.ecplus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.ChatColor;

public class InventoryLoader {

	private final HashMap<UUID, InventoryData> invs = new HashMap<>();
	private final HashMap<Player, UUID> adminLookingAt = new HashMap<>();

	public InventoryLoader(EnderChestPlus plugin) {
	}

	public void loadInventoryData(Player p) {
		if (!invs.containsKey(p.getUniqueId())) {
			InventoryData data = new InventoryData(p);
			invs.put(p.getUniqueId(), data);
		}
	}

	public InventoryData getInventoryData(Player p) {
		if (invs.containsKey(p.getUniqueId())) {
			return invs.get(p.getUniqueId());
		}
		return null;
	}

	public InventoryData getInventoryData(UUID uuid) {
		if (invs.containsKey(uuid)) {
			return invs.get(uuid);
		}

		InventoryData data = new InventoryData(uuid);
		invs.put(uuid, data);
		return data;
	}

	public void saveAllInventoryData() {
		invs.values().forEach(data -> {
			data.save();
		});
	}

	public void setLookingAt(Player p, UUID uuid) {
		if (uuid == null) {
			if (adminLookingAt.containsKey(p)) {
				adminLookingAt.remove(p);
			}
			return;
		}

		adminLookingAt.put(p, uuid);
	}

	public UUID getLookingAt(Player p) {
		if (adminLookingAt.containsKey(p)) {
			return adminLookingAt.get(p);
		}
		return null;
	}

	public static Inventory getMainInventory(InventoryData data) {
		Inventory mainInv = Bukkit.createInventory(null, 9 * 6, EnderChestPlus.mainEnderChestTitle);

		for (int i = 0; i < mainInv.getSize(); i++) {
			Inventory inv = data.getInventory(i);
			if (inv != null) {
				double percentage = getPercentage(inv);
				ItemStack item = null;

				if (percentage < 0.333) {
					item = getLowPane();
				} else if (percentage < 0.666) {
					item = getMidiumPane();
				} else {
					item = getHighPane();
				}

				ItemHelper.setDisplayName(item,
						ChatColor.GREEN + "ページ" + ChatColor.YELLOW + (i + 1) + ChatColor.GREEN + "を開く");
				ItemHelper.setLore(item, getLore(inv, 5));
				mainInv.setItem(i, item);
			} else {
				ItemStack item = getBuyPane(i);
				mainInv.setItem(i, item);
			}
		}

		return mainInv;
	}

	public static Inventory getBuyInventory(int page) {
		Inventory inv = Bukkit.createInventory(null, 9 * 1,
				EnderChestPlus.enderChestTitlePrefix + ChatColor.GREEN + " - " + ChatColor.RED + "Unlock Page "
						+ (page + 1));
		ItemStack confirm = ItemHelper.createItem(Material.STAINED_GLASS_PANE, 5, ChatColor.GREEN + "確定");
		ItemStack cancel = ItemHelper.createItem(Material.STAINED_GLASS_PANE, 14, ChatColor.RED + "キャンセル");
		ItemStack sign = ItemHelper.createItem(Material.SIGN, 0,
				ChatColor.GREEN + "ページ" + ChatColor.YELLOW + (page + 1) + ChatColor.GREEN + "を購入しますか？");

		inv.setItem(0, cancel);
		inv.setItem(1, cancel);
		inv.setItem(2, cancel);
		inv.setItem(3, cancel);
		inv.setItem(4, sign);
		inv.setItem(5, confirm);
		inv.setItem(6, confirm);
		inv.setItem(7, confirm);
		inv.setItem(8, confirm);

		return inv;
	}

	private static ItemStack lowPane = null, midiumPane = null, highPane = null;

	public static ItemStack getBuyPane(int page) {
		ItemStack buyPane = ItemHelper.createItem(Material.STAINED_GLASS_PANE, 15,
				ChatColor.YELLOW + "クリックでページ" + ChatColor.GREEN + (page + 1) + ChatColor.YELLOW + "を購入する");

		List<String> lore = new ArrayList<>(Arrays.asList(ChatColor.GOLD + "解禁コスト" + ChatColor.GREEN + ":"));
		if (0 <= page && page < 18) {
			lore.add(ChatColor.GRAY + "  - " + ChatColor.RED + "なし");
		} else if (18 <= page && page < 27) {
			lore.add(ChatColor.GRAY + "  - " + ChatColor.GREEN + ChatColor.BOLD + "エメラルドブロック "
					+ ChatColor.GRAY + "x32");
		} else if (27 <= page && page < 36) {
			lore.add(ChatColor.GRAY + "  - " + ChatColor.GREEN + ChatColor.BOLD + "エメラルドブロック "
					+ ChatColor.GRAY + "x64");
		} else if (36 <= page && page < 45) {
			lore.add(ChatColor.GRAY + "  - " + ChatColor.GREEN + ChatColor.BOLD + "エメラルドブロック "
					+ ChatColor.GRAY + "x64");
			lore.add(ChatColor.GRAY + "  - " + ChatColor.AQUA + ChatColor.BOLD + "ダイヤモンドブロック " + ChatColor.GRAY
					+ "x10");
		} else if (45 <= page && page < 54) {
			lore.add(ChatColor.GRAY + "  - " + ChatColor.AQUA + ChatColor.BOLD + "ダイヤモンドブロック " + ChatColor.GRAY
					+ "x32");
		}

		ItemHelper.setLore(buyPane, lore);

		return buyPane;
	}

	public static ItemStack getLowPane() {
		if (lowPane == null) {
			lowPane = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 5);
		}
		return lowPane;
	}

	public static ItemStack getMidiumPane() {
		if (midiumPane == null) {
			midiumPane = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 4);
		}
		return midiumPane;
	}

	public static ItemStack getHighPane() {
		if (highPane == null) {
			highPane = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 14);
		}
		return highPane;
	}

	private static double getPercentage(Inventory inv) {
		int total = inv.getSize();
		int empty = 0;
		for (int i = 0; i < inv.getSize(); i++) {
			if (inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR) {
				empty++;
			}
		}

		return (double) (total - empty) / (double) total;
	}

	private static List<String> getLore(Inventory inv, int lines) {
		List<String> lore = new ArrayList<>();

		for (int i = 0; i < inv.getSize(); i++) {
			ItemStack item = inv.getItem(i);
			if (item == null || item.getType() == Material.AIR) {
				continue;
			}

			String msg = ChatColor.RESET + "";
			if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
				msg += item.getItemMeta().getDisplayName();
			} else {
				msg += item.getType().toString();
			}

			if (item.getAmount() > 1) {
				msg += ChatColor.GRAY + " x" + item.getAmount();
			}

			lore.add(msg);
		}

		if (lore.size() >= lines) {
			lore.set(lines - 1, ChatColor.GRAY + "(その他" + (lore.size() - (lines - 1)) + "アイテム)");
			lore = lore.subList(0, lines);
		}

		return lore;
	}
}
