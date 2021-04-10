package jp.azisaba.lgw.ecplus.tasks;

import jp.azisaba.lgw.ecplus.InventoryData;
import jp.azisaba.lgw.ecplus.InventoryLoader;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class WaitLoadingTask extends BukkitRunnable {

    private final InventoryLoader loader;

    private final HashMap<UUID, UUID> waiting = new HashMap<>();

    public void addQueue(Player p, UUID target) {
        waiting.put(p.getUniqueId(), target);
    }

    @Override
    public void run() {
        // 誰も待機していない場合はreturn
        if (waiting.size() <= 0) {
            return;
        }

        List<UUID> removes = new ArrayList<>();

        for (UUID uuid : waiting.keySet()) {
            // プレイヤーを取得
            Player waitingPlayer = Bukkit.getPlayer(uuid);
            // nullの場合は後でMapから削除
            if (waitingPlayer == null) {
                removes.add(uuid);
                continue;
            }

            // 対象プレイヤーのエンチェスを取得
            InventoryData data = loader.getInventoryData(waiting.get(uuid));
            // nullの場合はcontinue
            if (data == null) {
                continue;
            }

            // プレイヤーと開くエンチェスのUUIDが違う場合はlookingを指定
            if (uuid != waiting.get(uuid)) {
                loader.setLookingAt(waitingPlayer, waiting.get(uuid));
            }

            // インベントリを開く
            waitingPlayer.openInventory(InventoryLoader.getMainInventory(data, 0));
            // 削除リストに追加
            removes.add(uuid);
        }

        // 削除リストに値があるなら削除する
        if (removes.size() > 0) {
            removes.forEach(uuid -> waiting.remove(uuid));
        }
    }
}
