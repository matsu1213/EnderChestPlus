package jp.azisaba.lgw.ecplus.listeners;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import jp.azisaba.lgw.ecplus.EnderChestPlus;
import jp.azisaba.lgw.ecplus.InventoryData;
import jp.azisaba.lgw.ecplus.InventoryLoader;

public class EnderChestListener implements Listener {

    private final EnderChestPlus plugin;
    private final InventoryLoader loader;

    public EnderChestListener(EnderChestPlus plugin, InventoryLoader loader) {
        this.plugin = plugin;
        this.loader = loader;
    }

    @EventHandler
    public void onClickEnderChest(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if ( e.getAction() != Action.RIGHT_CLICK_BLOCK ) {
            return;
        }
        Block b = e.getClickedBlock();

        if ( b.getType() != Material.ENDER_CHEST ) {
            return;
        }

        e.setCancelled(true);

        if ( loader.getLookingAt(p) != null ) {
            loader.setLookingAt(p, null);
        }

        Inventory inv = InventoryLoader.getMainInventory(loader.getInventoryData(p));
        p.openInventory(inv);
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void clickInventory(InventoryClickEvent e) {
        if ( !(e.getWhoClicked() instanceof Player) ) {
            return;
        }
        Player p = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();
        Inventory inv = e.getInventory();
        Inventory clickedInv = e.getClickedInventory();

        if ( !inv.getTitle().equals(EnderChestPlus.mainEnderChestTitle) ) {
            return;
        }

        e.setCancelled(true);

        if ( clickedInv == null || !clickedInv.equals(inv) || item == null ) {
            return;
        }

        if ( item.getData().getData() == (byte) 15 ) {
            String pageNumStr = ChatColor.stripColor(item.getItemMeta().getDisplayName());
            pageNumStr = pageNumStr.substring(8, pageNumStr.indexOf("を購入する"));
            int pageNum = Integer.parseInt(pageNumStr);
            p.openInventory(InventoryLoader.getBuyInventory(pageNum - 1));
        } else {
            int invNum = -1;
            try {
                String title = ChatColor.stripColor(item.getItemMeta().getDisplayName());
                title = title.substring(3, title.indexOf("を開く"));
                invNum = Integer.parseInt(title);
            } catch ( Exception ex ) {
                ex.printStackTrace();
                return;
            }

            UUID looking = loader.getLookingAt(p);
            InventoryData data;
            if ( looking != null ) {
                data = loader.getInventoryData(looking);
            } else {
                data = loader.getInventoryData(p);
            }
            p.openInventory(data.getInventory(invNum - 1));
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_HAT, 1, 1);
        }
    }

    @EventHandler
    public void backToMainInventory(InventoryClickEvent e) {
        if ( !(e.getWhoClicked() instanceof Player) ) {
            return;
        }
        Player p = (Player) e.getWhoClicked();
        Inventory inv = e.getInventory();
        Inventory clickedInv = e.getClickedInventory();

        if ( inv.getTitle().equals(EnderChestPlus.mainEnderChestTitle) ) {
            return;
        }
        if ( !inv.getTitle().startsWith(EnderChestPlus.enderChestTitlePrefix) ) {
            return;
        }
        if ( e.getClick() != ClickType.MIDDLE ) {
            return;
        }

        if ( clickedInv == null ) {
            InventoryData data;
            UUID looking = loader.getLookingAt(p);
            if ( looking != null ) {
                data = loader.getInventoryData(looking);
            } else {
                data = loader.getInventoryData(p);
            }
            Inventory mainInv = InventoryLoader.getMainInventory(data);
            p.openInventory(mainInv);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_HAT, 1, 1);
        }
    }

    @EventHandler
    public void nextOrBackInventory(InventoryClickEvent e) {
        if ( !(e.getWhoClicked() instanceof Player) ) {
            return;
        }
        Player p = (Player) e.getWhoClicked();
        Inventory inv = e.getInventory();
        Inventory clickedInv = e.getClickedInventory();

        if ( inv.getTitle().equals(EnderChestPlus.mainEnderChestTitle) ) {
            return;
        }
        if ( !inv.getTitle().startsWith(EnderChestPlus.enderChestTitlePrefix) ) {
            return;
        }
        if ( clickedInv != null ) {
            return;
        }
        if ( e.getClick() != ClickType.LEFT && e.getClick() != ClickType.RIGHT ) {
            return;
        }

        String title = ChatColor.stripColor(inv.getTitle());
        int currentInventory = Integer.parseInt(title.substring(title.indexOf("Page") + 5, title.length())) - 1;
        int addNum = 1;
        if ( e.getClick() == ClickType.LEFT ) {
            addNum = -1;
        }

        InventoryData data;
        UUID looking = loader.getLookingAt(p);
        if ( looking != null ) {
            data = loader.getInventoryData(looking);
        } else {
            data = loader.getInventoryData(p);
        }

        int nextInvNum = currentInventory;
        Inventory nextInv = null;
        while ( nextInvNum >= 0 && nextInvNum <= 54 && nextInv == null ) {
            nextInvNum += addNum;
            nextInv = data.getInventory(nextInvNum);
        }
        if ( nextInv == null ) {
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
            return;
        }
        p.openInventory(nextInv);
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_HAT, 1, 1);
    }

    @EventHandler
    public void onCloseInventory(InventoryCloseEvent e) {
        if ( !(e.getPlayer() instanceof Player) ) {
            return;
        }
        Player p = (Player) e.getPlayer();

        if ( loader.getLookingAt(p) == null ) {
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if ( p.getOpenInventory() == null ) {
                    loader.setLookingAt(p, null);
                }
            }
        }.runTaskLater(plugin, 1);
    }
}
