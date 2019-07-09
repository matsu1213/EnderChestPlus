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

import jp.azisaba.lgw.ecplus.utils.Chat;
import jp.azisaba.lgw.ecplus.utils.ItemHelper;

public class InventoryLoader {

    private final HashMap<UUID, InventoryData> invs = new HashMap<>();
    private final HashMap<Player, UUID> adminLookingAt = new HashMap<>();

    public InventoryLoader(EnderChestPlus plugin) {
    }

    public void loadInventoryData(Player p) {
        if ( !invs.containsKey(p.getUniqueId()) ) {
            InventoryData data = new InventoryData(p);
            invs.put(p.getUniqueId(), data);
        }
    }

    public InventoryData getInventoryData(Player p) {
        if ( invs.containsKey(p.getUniqueId()) ) {
            return invs.get(p.getUniqueId());
        }
        return null;
    }

    public InventoryData getInventoryData(UUID uuid) {
        if ( invs.containsKey(uuid) ) {
            return invs.get(uuid);
        }

        InventoryData data = new InventoryData(uuid);
        invs.put(uuid, data);
        return data;
    }

    public void saveAllInventoryData() {

        if ( invs.size() <= 0 ) {
            return;
        }

        for ( UUID uuid : new ArrayList<UUID>(invs.keySet()) ) {
            boolean success = invs.get(uuid).save();

            if ( success && Bukkit.getPlayer(uuid) == null ) {
                invs.remove(uuid);
            }
        }
    }

    public void setLookingAt(Player p, UUID uuid) {
        if ( uuid == null ) {
            if ( adminLookingAt.containsKey(p) ) {
                adminLookingAt.remove(p);
            }
            return;
        }

        adminLookingAt.put(p, uuid);
    }

    public UUID getLookingAt(Player p) {
        if ( adminLookingAt.containsKey(p) ) {
            return adminLookingAt.get(p);
        }
        return null;
    }

    public static Inventory getMainInventory(InventoryData data) {
        Inventory mainInv = Bukkit.createInventory(null, 9 * 6, EnderChestPlus.mainEnderChestTitle);

        for ( int i = 0; i < mainInv.getSize(); i++ ) {
            Inventory inv = data.getInventory(i);
            if ( inv != null ) {
                double percentage = getPercentage(inv);
                ItemStack item = null;

                if ( percentage < 0.333 ) {
                    item = getLowPane();
                } else if ( percentage < 0.666 ) {
                    item = getMidiumPane();
                } else {
                    item = getHighPane();
                }

                ItemHelper.setDisplayName(item, Chat.f("&aページ&e{0}&aを開く", i + 1));
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
        Inventory inv = Bukkit.createInventory(null, 9 * 1, Chat.f("{0}&a - &cUnlock Page {1}", EnderChestPlus.enderChestTitlePrefix, page + 1));
        ItemStack confirm = ItemHelper.createItem(Material.STAINED_GLASS_PANE, 5, Chat.f("&a確定"));
        ItemStack cancel = ItemHelper.createItem(Material.STAINED_GLASS_PANE, 14, Chat.f("&cキャンセル"));
        ItemStack sign = ItemHelper.createItem(Material.SIGN, 0, Chat.f("&aページ&e{0}&aを購入しますか？", page + 1));

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
        ItemStack buyPane = ItemHelper.createItem(Material.STAINED_GLASS_PANE, 15, Chat.f("&eクリックでページ&a{0}&eを購入する", page + 1));

        List<String> lore = new ArrayList<>(Arrays.asList(Chat.f("&6解禁コスト&a:")));
        if ( 0 <= page && page < 18 ) {
            lore.add(Chat.f("&7  - &cなし"));
        } else if ( 18 <= page && page < 27 ) {
            lore.add(Chat.f("&7  - &a&lエメラルドブロック&7x32"));
        } else if ( 27 <= page && page < 36 ) {
            lore.add(Chat.f("&7  - &a&lエメラルドブロック&7x64"));
        } else if ( 36 <= page && page < 45 ) {
            lore.add(Chat.f("&7  - &a&lエメラルドブロック&7x64"));
            lore.add(Chat.f("&7  - &b&lダイヤモンドブロック&7x10"));
        } else if ( 45 <= page && page < 54 ) {
            lore.add(Chat.f("&7  - &b&lダイヤモンドブロック&7x32"));
        }

        ItemHelper.setLore(buyPane, lore);

        return buyPane;
    }

    public static ItemStack getLowPane() {
        if ( lowPane == null ) {
            lowPane = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 5);
        }
        return lowPane;
    }

    public static ItemStack getMidiumPane() {
        if ( midiumPane == null ) {
            midiumPane = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 4);
        }
        return midiumPane;
    }

    public static ItemStack getHighPane() {
        if ( highPane == null ) {
            highPane = new ItemStack(Material.STAINED_GLASS_PANE, 1, (byte) 14);
        }
        return highPane;
    }

    private static double getPercentage(Inventory inv) {
        int total = inv.getSize();
        int empty = 0;
        for ( int i = 0; i < inv.getSize(); i++ ) {
            if ( inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR ) {
                empty++;
            }
        }

        return (double) (total - empty) / (double) total;
    }

    private static List<String> getLore(Inventory inv, int lines) {
        List<String> lore = new ArrayList<>();

        for ( int i = 0; i < inv.getSize(); i++ ) {
            ItemStack item = inv.getItem(i);
            if ( item == null || item.getType() == Material.AIR ) {
                continue;
            }

            String msg = Chat.f("&r");
            if ( item.hasItemMeta() && item.getItemMeta().hasDisplayName() ) {
                msg += item.getItemMeta().getDisplayName();
            } else {
                msg += item.getType().toString();
            }

            if ( item.getAmount() > 1 ) {
                msg += Chat.f("&7 x{0}", item.getAmount());
            }

            lore.add(msg);
        }

        if ( lore.size() >= lines ) {
            lore.set(lines - 1, Chat.f("&7(その他{0}アイテム", lore.size() - (lines - 1)));
            lore = lore.subList(0, lines);
        }

        return lore;
    }
}
