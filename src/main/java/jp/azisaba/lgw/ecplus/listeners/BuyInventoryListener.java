package jp.azisaba.lgw.ecplus.listeners;

import jp.azisaba.lgw.ecplus.EnderChestPlus;
import jp.azisaba.lgw.ecplus.InventoryData;
import jp.azisaba.lgw.ecplus.InventoryLoader;
import jp.azisaba.lgw.ecplus.utils.Chat;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

@RequiredArgsConstructor
public class BuyInventoryListener implements Listener {

    private final InventoryLoader loader;

    @EventHandler
    public void onClickedConfirmGUI(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) {
            return;
        }

        Player p = (Player) e.getWhoClicked();
        Inventory clicked = e.getClickedInventory();
        Inventory opening = e.getInventory();
        ItemStack clickedItem = e.getCurrentItem();

        if (!opening.getTitle().startsWith(Chat.f("{0}&a - &cUnlock Page", EnderChestPlus.enderChestTitlePrefix))) {
            return;
        }

        e.setCancelled(true);

        if (clicked == null || !clicked.equals(opening) || clickedItem == null) {
            return;
        }

        int page;
        try {
            page = Integer.parseInt(Chat.r(opening.getTitle()).substring(Chat.r(opening.getTitle()).lastIndexOf(" ") + 1)) - 1;
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        if (clickedItem.getType() == Material.SIGN) {
            return;
        }
        int data = getData(clickedItem);
        int openMainInventoryIndex = page / 54;

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
                p.openInventory(InventoryLoader.getMainInventory(data2, openMainInventoryIndex));

                p.sendMessage(Chat.f("&a購入に成功しました！"));
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BELL, 2, 1);
            } else {
                p.sendMessage(Chat.f("&c購入するためのアイテムが足りません！"));
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
            p.openInventory(InventoryLoader.getMainInventory(data2, openMainInventoryIndex));
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_HAT, 1, 1);
        }
        return;
    }

    @SuppressWarnings("deprecation")
    private int getData(ItemStack item) {
        return item.getData().getData();
    }

    private boolean costPlayer(Player p, int page) {
        int line = page / 9;
        return costPlayerByLine(p, line);
    }

    private boolean costPlayerByLine(Player p, int line) {
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
        } else if (line <= 9) {
            diamondBlockNorma = 64;
        } else {
            emeraldBlockNorma = 64;
            diamondBlockNorma = 64;
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
