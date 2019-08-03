package jp.azisaba.lgw.ecplus.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;

import me.kbrewster.exceptions.APIException;
import me.kbrewster.exceptions.InvalidPlayerException;
import me.kbrewster.mojangapi.MojangAPI;

/**
 *
 * プレイヤー名をUUIDに変換するメソッド。 <br>
 * 投票したときに送られてくるデータがプレイヤー名なので、それをUUIDに保存し、正確に投票報酬を割り振るために作成されました。
 *
 * @author siloneco
 *
 */
public class UUIDUtils {

    // 調べたUUIDを保存しておくHashMap。何度も問い合わせるとエラーになるため
    private static HashMap<String, UUID> uuidCache = new HashMap<String, UUID>();

    /**
     *
     * プレイヤー名からUUIDを取得します。優先度は以下の通りです <br>
     * <br>
     * 1. プレイヤーがオンラインの場合はそのプレイヤーから取得 <br>
     * 2. オフラインプレイヤーとしてキャッシュに保存してある場合はそこから取得 <br>
     * 3. MojangAPIに問い合わせて取得
     *
     * @param name UUIDを取得したいプレイヤー名
     * @return そのプレイヤーのUUID、取得できなければエラーを吐いてnullを返す
     * @throws APIException           MojangAPIの規制に引っかかった場合に発生する例外
     * @throws InvalidPlayerException プレイヤーが存在しない、無効なMCIDが要求されたときに発生する例外
     * @throws IOException            何らかの割込みによってキャンセルされた場合に発生する例外
     */
    public static UUID getUUID(String name) throws APIException, InvalidPlayerException, IOException {
        // 1度調べたことのあるユーザーなら取得してreturn
        if ( uuidCache.containsKey(name) ) {
            return uuidCache.get(name);
        }

        UUID uuid = null;

        // プレイヤーがオンラインの場合はそのプレイヤーからUUIDを取得
        if ( uuid == null && Bukkit.getPlayerExact(name) != null ) {
            uuid = Bukkit.getPlayerExact(name).getUniqueId();
        }
        // オフラインプレイヤーから検索
        if ( uuid == null && getUUIDFromOfflinePlayer(name) != null ) {
            uuid = getUUIDFromOfflinePlayer(name);
        }

        // まだ取得できていない場合はMojangAPIを使用して取得
        if ( uuid == null ) {
            // MojangAPIからUUIDを取得
            uuid = MojangAPI.getUUID(name);
        }

        // 成功しており、Cacheに保存されていない場合は保存
        if ( uuid != null && !uuidCache.containsKey(name.toLowerCase()) ) {
            uuidCache.put(name.toLowerCase(), uuid);
        }

        return uuid;
    }

    /**
     *
     * オフラインプレイヤーからUUIDを取得するメソッド。
     *
     * @param name UUIDを取得したいプレイヤーのUUID
     * @return そのプレイヤーのUUID、取得できなければreturn
     */
    @SuppressWarnings("deprecation")
    private static UUID getUUIDFromOfflinePlayer(String name) {
        // オフラインプレイヤーから検索しヒットしたらreturn
        if ( Bukkit.getOfflinePlayer(name) != null ) {
            return Bukkit.getOfflinePlayer(name).getUniqueId();
        }

        // 取得できなかったらnullを返す
        return null;
    }
}
