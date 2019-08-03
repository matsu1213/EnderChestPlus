package jp.azisaba.lgw.ecplus.commands;

import java.io.IOException;
import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import lombok.RequiredArgsConstructor;

import jp.azisaba.lgw.ecplus.EnderChestPlus;
import jp.azisaba.lgw.ecplus.InventoryLoader;
import jp.azisaba.lgw.ecplus.utils.Chat;
import jp.azisaba.lgw.ecplus.utils.UUIDUtils;
import me.kbrewster.exceptions.APIException;
import me.kbrewster.exceptions.InvalidPlayerException;

@RequiredArgsConstructor
public class EnderChestPlusCommand implements CommandExecutor {

    private final EnderChestPlus plugin;
    private final InventoryLoader loader;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if ( !(sender instanceof Player) ) {
            return true;
        }
        Player p = (Player) sender;

        if ( args.length <= 0 ) {
            p.sendMessage(Chat.f("&cUsage: {0}", cmd.getUsage().replace("{LABEL}", label)));
            return true;
        }

        long start = System.currentTimeMillis();
        if ( args[0].equals("save") ) {
            int saved = loader.saveAllInventoryData(false);
            plugin.getLogger().info(Chat.f("{0}人のエンダーチェストを保存しました。", saved));
            p.sendMessage(Chat.f("&a{0}人のエンダーチェストを保存しました。 &7({1}ms)", saved, System.currentTimeMillis() - start));
            return true;
        }
        if ( args[0].equals("saveasync") ) {
            int saved = loader.saveAllInventoryData(true);
            plugin.getLogger().info(Chat.f("{0}人のエンダーチェストを保存しました。", saved));
            p.sendMessage(Chat.f("&a{0}人のエンダーチェストを保存しました。 &7({1}ms)", saved, System.currentTimeMillis() - start));
            return true;
        }

        UUID uuid = null;
        try {
            uuid = UUIDUtils.getUUID(args[0]);
        } catch ( APIException e ) {
            p.sendMessage(Chat.f("&cUUIDの取得に失敗しました。(MojangAPIのレートリミット)"));
            return true;
        } catch ( InvalidPlayerException e ) {
            p.sendMessage(Chat.f("&cUUIDの取得に失敗しました。(そのMCIDのプレイヤーは存在しません)"));
            return true;
        } catch ( IOException e ) {
            p.sendMessage(Chat.f("&cUUIDの取得に失敗しました。({0})", e.getClass().getName()));
            return true;
        }

        if ( uuid == null ) {
            p.sendMessage(Chat.f("&cUUIDの取得に失敗しました。(不明)"));
            return true;
        }

        loader.setLookingAt(p, uuid);
        p.openInventory(InventoryLoader.getMainInventory(loader.getInventoryData(uuid)));
        return true;
    }
}
