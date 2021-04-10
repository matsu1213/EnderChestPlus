package jp.azisaba.lgw.ecplus.commands;

import co.aikar.taskchain.TaskChain;
import jp.azisaba.lgw.ecplus.EnderChestPlus;
import jp.azisaba.lgw.ecplus.InventoryData;
import jp.azisaba.lgw.ecplus.InventoryLoader;
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

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        Player p = (Player) sender;

        if (args.length <= 0) {
            sendUsage(p, label);
            return true;
        }

        if (args[0].equals("save")) {
            EnderChestPlus.newChain()
                    .sync(() -> p.sendMessage(Chat.f("&a非同期でセーブしています...")))
                    .asyncFirst(() -> loader.saveAllInventoryData(false))
                    .asyncLast((count) -> {
                        plugin.getLogger().info(Chat.f("{0}人のエンダーチェストを保存しました。", count));
                        p.sendMessage(Chat.f("&a{0}人のエンダーチェストを保存しました。", count));
                    }).execute();
            return true;
        }

        if (args[0].equalsIgnoreCase("open")) {
            if (args.length <= 1) {
                p.sendMessage(Chat.f("&cUUIDかプレイヤー名を指定してください"));
                return true;
            }

            p.sendMessage(Chat.f("&a非同期でデータをロード中です。完了し次第開きます"));
            EnderChestPlus.newChain()
                    .asyncFirst(() -> {
                        try {
                            return UUIDUtils.getUUID(args[1]);
                        } catch (APIException e) {
                            p.sendMessage(Chat.f("&cUUIDの取得に失敗しました。(MojangAPIのレートリミット)"));
                        } catch (InvalidPlayerException e) {
                            p.sendMessage(Chat.f("&cUUIDの取得に失敗しました。(そのMCIDのプレイヤーは存在しません)"));
                        } catch (Exception e) {
                            String className = e.getClass().getName();
                            if (className.contains(".")) {
                                className = className.substring(className.lastIndexOf(".") + 1);
                            }
                            p.sendMessage(Chat.f("&cUUIDの取得に失敗しました。({0})", className));
                        }
                        return null;
                    }).abortIfNull()
                    .storeAsData("uuid")
                    .asyncLast(loader::loadInventoryData)
                    .<UUID>returnData("uuid")
                    .syncLast((uuid) -> {
                        if (p.isOnline()) {
                            InventoryData data = loader.getInventoryData(uuid);
                            p.openInventory(InventoryLoader.getMainInventory(data, 0));
                        }
                    }).execute();
            return true;
        }

        sendUsage(p, label);
        return true;
    }

    private void sendUsage(Player p, String label) {
        p.sendMessage(Chat.f("&e/{0} open <Player/UUID> &7- &aECを開きます", label) + "\n"
        + Chat.f("&e/{0} save &7- &a非同期で全プレイヤーのECをセーブします", label) + "\n");
    }
}
