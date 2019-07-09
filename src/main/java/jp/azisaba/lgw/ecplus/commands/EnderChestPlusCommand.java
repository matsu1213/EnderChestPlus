package jp.azisaba.lgw.ecplus.commands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import lombok.RequiredArgsConstructor;

import jp.azisaba.lgw.ecplus.EnderChestPlus;
import jp.azisaba.lgw.ecplus.InventoryLoader;
import jp.azisaba.lgw.ecplus.utils.Chat;

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
            p.sendMessage(Chat.f("&a{0}人のエンダーチェストを保存しました。", saved));

            Bukkit.getLogger().info((System.currentTimeMillis() - start) + "ms");
            return true;
        }
        if ( args[0].equals("saveasync") ) {
            int saved = loader.saveAllInventoryData(true);
            plugin.getLogger().info(Chat.f("{0}人のエンダーチェストを保存しました。", saved));
            p.sendMessage(Chat.f("&a{0}人のエンダーチェストを保存しました。", saved));

            Bukkit.getLogger().info((System.currentTimeMillis() - start) + "ms");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        UUID uuid = null;
        try {
            uuid = UUID.fromString(args[0]);
        } catch ( Exception e ) {
            // pass
        }

        if ( target == null && uuid == null ) {
            p.sendMessage(Chat.f("&c現在オンラインのプレイヤー名かUUIDを入力してください。"));
            return true;
        }

        if ( target != null ) {
            uuid = target.getUniqueId();
        }

        loader.setLookingAt(p, uuid);
        p.openInventory(InventoryLoader.getMainInventory(loader.getInventoryData(uuid)));
        return true;
    }
}
