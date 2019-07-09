package jp.azisaba.lgw.ecplus.tasks;

import org.bukkit.scheduler.BukkitRunnable;

import lombok.RequiredArgsConstructor;

import jp.azisaba.lgw.ecplus.InventoryLoader;

@RequiredArgsConstructor
public class AutoSaveTask extends BukkitRunnable {

    private final InventoryLoader loader;

    @Override
    public void run() {
        loader.saveAllInventoryData();
    }
}
