package jp.azisaba.lgw.ecplus.commands;

import jp.azisaba.lgw.ecplus.EnderChestPlus;
import jp.azisaba.lgw.ecplus.InventoryLoader;
import jp.azisaba.lgw.ecplus.tasks.WaitLoadingTask;
import jp.azisaba.lgw.ecplus.utils.Chat;
import jp.azisaba.lgw.ecplus.utils.UUIDUtils;
import lombok.RequiredArgsConstructor;
import me.kbrewster.exceptions.APIException;
import me.kbrewster.exceptions.InvalidPlayerException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.UUID;

@RequiredArgsConstructor
public class EnderChestPlusCommand implements CommandExecutor {

    private final EnderChestPlus plugin;
    private final InventoryLoader loader;
    private final WaitLoadingTask loadingTask;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        Player p = (Player) sender;

        if (args.length <= 0) {
            p.sendMessage(Chat.f("&cUsage: {0}", cmd.getUsage().replace("{LABEL}", label)));
            return true;
        }

        long start = System.currentTimeMillis();
        if (args[0].equals("save")) {
            int saved = loader.saveAllInventoryData(false);
            plugin.getLogger().info(Chat.f("{0}人のエンダーチェストを保存しました。", saved));
            p.sendMessage(Chat.f("&a{0}人のエンダーチェストを保存しました。 &7({1}ms)", saved, System.currentTimeMillis() - start));
            return true;
        }
        if (args[0].equals("saveasync")) {
            int saved = loader.saveAllInventoryData(true);
            plugin.getLogger().info(Chat.f("{0}人のエンダーチェストを保存しました。", saved));
            p.sendMessage(Chat.f("&a{0}人のエンダーチェストを保存しました。 &7({1}ms)", saved, System.currentTimeMillis() - start));
            return true;
        }

        p.sendMessage(Chat.f("&a非同期でデータをロード中です。完了し次第開きます"));

        new Thread() {
            @Override
            public void run() {
                UUID uuid = null;
                try {
                    uuid = UUIDUtils.getUUID(args[0]);
                } catch (APIException e) {
                    p.sendMessage(Chat.f("&cUUIDの取得に失敗しました。(MojangAPIのレートリミット)"));
                    return;
                } catch (InvalidPlayerException e) {
                    p.sendMessage(Chat.f("&cUUIDの取得に失敗しました。(そのMCIDのプレイヤーは存在しません)"));
                    return;
                } catch (IOException e) {
                    p.sendMessage(Chat.f("&cUUIDの取得に失敗しました。({0})", e.getClass().getName()));
                    return;
                }

                if (uuid == null) {
                    p.sendMessage(Chat.f("&cUUIDの取得に失敗しました。(不明)"));
                    return;
                }

                loadingTask.addQueue(p, uuid);
                loader.loadInventoryData(uuid);
            }
        }.start();
        return true;
    }
}
