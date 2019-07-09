package jp.azisaba.lgw.ecplus.tasks;

import org.bukkit.scheduler.BukkitRunnable;

import lombok.RequiredArgsConstructor;

import jp.azisaba.lgw.ecplus.EnderChestPlus;
import jp.azisaba.lgw.ecplus.InventoryLoader;
import jp.azisaba.lgw.ecplus.utils.Chat;

@RequiredArgsConstructor
public class AutoSaveTask extends BukkitRunnable {

    private final EnderChestPlus plugin;
    private final InventoryLoader loader;

    @Override
    public void run() {
        int count = loader.saveAllInventoryData(true);

        if ( count <= 0 ) {
            return;
        }

        plugin.getLogger().info(Chat.f("{0}人のエンダーチェストを保存しました。", count));
    }
}
