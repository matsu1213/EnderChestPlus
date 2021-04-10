package jp.azisaba.lgw.ecplus.commands;

import jp.azisaba.lgw.ecplus.DropItemContainer;
import jp.azisaba.lgw.ecplus.DropItemContainer.DropItem;
import jp.azisaba.lgw.ecplus.utils.Chat;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

@RequiredArgsConstructor
public class ReceiveDroppedCommand implements CommandExecutor {

    private final DropItemContainer dropItemContainer;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Chat.f("&cこのコマンドはプレイヤーのみ有効です"));
            return true;
        }

        Player p = (Player) sender;

        if (args.length <= 0) {
            return true;
        }

        String id = args[0];
        DropItem itemData = dropItemContainer.getItemData(id);

        if (itemData == null) {
            p.sendMessage(Chat.f("&cアイテムが見つかりませんでした。すでに受け取っていませんか？"));
            return true;
        }

        if (!p.getUniqueId().equals(itemData.getOwner())) {
            p.sendMessage(Chat.f("&c本人以外がアイテムを受け取ることはできません"));
            return true;
        }

        Inventory inv = dropItemContainer.getInventory(id);

        p.openInventory(inv);
        return true;
    }
}
