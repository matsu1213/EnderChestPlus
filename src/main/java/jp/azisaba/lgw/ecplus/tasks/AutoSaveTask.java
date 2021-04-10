package jp.azisaba.lgw.ecplus.tasks;

import jp.azisaba.lgw.ecplus.EnderChestPlus;
import jp.azisaba.lgw.ecplus.InventoryLoader;
import jp.azisaba.lgw.ecplus.utils.Chat;
import lombok.RequiredArgsConstructor;
import org.bukkit.scheduler.BukkitRunnable;

@RequiredArgsConstructor
public class AutoSaveTask extends BukkitRunnable {

    private final EnderChestPlus plugin;
    private final InventoryLoader loader;

    @Override
    public void run() {
        EnderChestPlus.newChain()
                .asyncFirst(() -> loader.saveAllInventoryData(true))
                .syncLast(input -> plugin.getLogger().info(Chat.f("{0}人のエンダーチェストを保存しました。", input)))
                .execute();
    }
}
