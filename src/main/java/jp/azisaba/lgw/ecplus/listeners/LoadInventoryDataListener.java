package jp.azisaba.lgw.ecplus.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import lombok.RequiredArgsConstructor;

import jp.azisaba.lgw.ecplus.InventoryLoader;

@RequiredArgsConstructor
public class LoadInventoryDataListener implements Listener {

    private final InventoryLoader loader;

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        // 非同期で読み込みを行う
        new Thread() {
            @Override
            public void run() {
                loader.loadInventoryData(p);
            }
        }.start();
    }
}
