package jp.azisaba.lgw.ecplus;

import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import jp.azisaba.lgw.ecplus.commands.EnderChestPlusCommand;
import jp.azisaba.lgw.ecplus.commands.ReceiveDroppedCommand;
import jp.azisaba.lgw.ecplus.listeners.BuyInventoryListener;
import jp.azisaba.lgw.ecplus.listeners.DroppedItemListener;
import jp.azisaba.lgw.ecplus.listeners.EnderChestListener;
import jp.azisaba.lgw.ecplus.listeners.LoadInventoryDataListener;
import jp.azisaba.lgw.ecplus.tasks.AutoSaveTask;
import jp.azisaba.lgw.ecplus.utils.Chat;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;

public class EnderChestPlus extends JavaPlugin {

    public static final String enderChestTitlePrefix = Chat.f("&cEnderChest&b+");
    public static final String mainEnderChestTitle = Chat.f("{0} &a- &eMain", enderChestTitlePrefix);
    public static final int MAX_MAIN_INVENTORY_PAGES = 5;
    private static PluginConfig config;
    @Getter
    private static File inventoryDataFile;
    private static TaskChainFactory taskChainFactory;
    private AutoSaveTask saveTask;
    @Getter
    private DropItemContainer dropItemContainer = null;
    @Getter
    private InventoryLoader loader = null;
    @Getter
    @Setter
    private boolean allowOpenEnderChest = true;

    public static PluginConfig getPluginConfig() {
        return config;
    }

    public static <T> TaskChain<T> newChain() {
        return taskChainFactory.newChain();
    }

    public static <T> TaskChain<T> newSharedChain(String name) {
        return taskChainFactory.newSharedChain(name);
    }

    @Override
    public void onEnable() {
        taskChainFactory = BukkitTaskChainFactory.create(this);

        inventoryDataFile = new File(getDataFolder(), "Inventories");
        loader = new InventoryLoader(this);

        dropItemContainer = new DropItemContainer(this);
        dropItemContainer.load();
        saveTask = new AutoSaveTask(this, loader);
        saveTask.runTaskTimer(this, 20 * 60 * 5, 20 * 60 * 5);

        EnderChestPlus.config = new PluginConfig(this);
        EnderChestPlus.config.loadConfig();

        Bukkit.getOnlinePlayers().forEach(p -> newSharedChain("loadInventory")
                .async(() -> loader.loadInventoryData(p))
                .execute());

        Bukkit.getPluginManager().registerEvents(new EnderChestListener(this, loader, dropItemContainer), this);
        Bukkit.getPluginManager().registerEvents(new LoadInventoryDataListener(loader), this);
        Bukkit.getPluginManager().registerEvents(new BuyInventoryListener(loader), this);
        Bukkit.getPluginManager().registerEvents(new DroppedItemListener(dropItemContainer), this);

        Bukkit.getPluginCommand("enderchestplus").setExecutor(new EnderChestPlusCommand(this, loader));
        Bukkit.getPluginCommand("enderchestplus").setPermissionMessage(Chat.f("{0}&c権限がありません！", config.chatPrefix));
        Bukkit.getPluginCommand("receivedropped").setExecutor(new ReceiveDroppedCommand(dropItemContainer));
        Bukkit.getPluginCommand("receivedropped").setPermissionMessage(Chat.f("{0}&c権限がありません！", config.chatPrefix));

        Bukkit.getLogger().info(getName() + " enabled.");
    }

    @Override
    public void onDisable() {

        saveTask.cancel();

        dropItemContainer.save();

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
                        player.sendMessage(Chat.f("&cインベントリにもエンダーチェストにもアイテムが入らなかったため、地面にドロップしました。"));
                    }
                }
                player.closeInventory();
            }
        });

        loader.saveAllInventoryData(false);

        Bukkit.getLogger().info(getName() + " disabled.");
    }

    public void reloadPluginConfig() {

        reloadConfig();

        EnderChestPlus.config = new PluginConfig(this);
        EnderChestPlus.config.loadConfig();
    }
}
