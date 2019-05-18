package jp.azisaba.lgw.ecplus.listeners;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import jp.azisaba.lgw.ecplus.EnderChestPlus;
import jp.azisaba.lgw.ecplus.InventoryData;
import jp.azisaba.lgw.ecplus.InventoryLoader;
import net.md_5.bungee.api.ChatColor;

public class BuyInventoryListener implements Listener {

	private final InventoryLoader loader;

	public BuyInventoryListener(InventoryLoader loader) {
		this.loader = loader;
	}

	@EventHandler
	public void onClickedConfirmGUI(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player)) {
			return;
		}

		Player p = (Player) e.getWhoClicked();
		Inventory clicked = e.getClickedInventory();
		Inventory opening = e.getInventory();
		ItemStack clickedItem = e.getCurrentItem();

		if (!ChatColor.stripColor(opening.getTitle())
				.startsWith(ChatColor.stripColor(EnderChestPlus.enderChestTitlePrefix) + " - Unlock Page ")) {
			return;
		}

		e.setCancelled(true);

		if (clicked == null || !clicked.equals(opening) || clickedItem == null) {
			return;
		}

		int page;
		try {
			page = Integer.parseInt(ChatColor.stripColor(opening.getTitle())
					.substring(ChatColor.stripColor(opening.getTitle()).lastIndexOf(" ") + 1)) - 1;
		} catch (Exception ex) {
			ex.printStackTrace();
			return;
		}

		if (clickedItem.getType() == Material.SIGN) {
			return;
		}
		int data = getData(clickedItem);

		if (data == 5) {
			boolean success = costPlayer(p, page);

			if (success) {

				UUID looking = loader.getLookingAt(p);
				InventoryData data2;
				if (looking != null) {
					data2 = loader.getInventoryData(looking);
				} else {
					data2 = loader.getInventoryData(p);
				}
				data2.initializeInventory(page);
				p.openInventory(InventoryLoader.getMainInventory(data2));

				p.sendMessage(ChatColor.GREEN + "購入に成功しました！");
				p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BELL, 2, 1);
			} else {
				p.sendMessage(ChatColor.RED + "購入するためのアイテムが足りません！");
				p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
				p.closeInventory();
			}
		} else if (data == 14) {
			UUID looking = loader.getLookingAt(p);
			InventoryData data2;
			if (looking != null) {
				data2 = loader.getInventoryData(looking);
			} else {
				data2 = loader.getInventoryData(p);
			}
			p.openInventory(InventoryLoader.getMainInventory(data2));
			p.playSound(p.getLocation(), Sound.BLOCK_NOTE_HAT, 1, 1);
		}
		return;
	}

	@SuppressWarnings("deprecation")
	private int getData(ItemStack item) {
		return item.getData().getData();
	}

	private boolean costPlayer(Player p, int page) {
		int line = -1;
		if (0 <= page && page < 9) {
			line = 1;
		} else if (9 <= page && page < 18) {
			line = 2;
		} else if (18 <= page && page < 27) {
			line = 3;
		} else if (27 <= page && page < 36) {
			line = 4;
		} else if (36 <= page && page < 45) {
			line = 5;
		} else if (45 <= page && page < 54) {
			line = 6;
		}

		return costPlayerByLine(p, line);
	}

	private boolean costPlayerByLine(Player p, int line) {
		if (!(0 < line && line <= 6)) {
			return false;
		}

		if (line <= 2) {
			return true;
		}

		int emeraldBlockNorma = 0;
		int diamondBlockNorma = 0;

		if (line == 3) {
			emeraldBlockNorma = 32;
		} else if (line == 4) {
			emeraldBlockNorma = 64;
		} else if (line == 5) {
			emeraldBlockNorma = 64;
			diamondBlockNorma = 10;
		} else if (line == 6) {
			diamondBlockNorma = 32;
		}

		ItemStack[] contents = p.getInventory().getContents().clone();
		for (int i = 0; i < 36; i++) {
			ItemStack item = contents[i];
			if (item == null) {
				continue;
			}

			if (item.getType() == Material.DIAMOND_BLOCK) {
				if (diamondBlockNorma <= 0) {
					continue;
				}
				if (item.getAmount() <= diamondBlockNorma) {
					contents[i] = null;
					diamondBlockNorma -= item.getAmount();
				} else {
					item.setAmount(item.getAmount() - diamondBlockNorma);
					contents[i] = item;
					diamondBlockNorma = 0;
				}
			} else if (item.getType() == Material.EMERALD_BLOCK) {
				if (emeraldBlockNorma <= 0) {
					continue;
				}
				if (item.getAmount() <= emeraldBlockNorma) {
					contents[i] = null;
					emeraldBlockNorma -= item.getAmount();
				} else {
					item.setAmount(item.getAmount() - emeraldBlockNorma);
					contents[i] = item;
					emeraldBlockNorma = 0;
				}
			}
		}

		if (emeraldBlockNorma <= 0 && diamondBlockNorma <= 0) {
			p.getInventory().setContents(contents);
			return true;
		}
		return false;
	}
}
