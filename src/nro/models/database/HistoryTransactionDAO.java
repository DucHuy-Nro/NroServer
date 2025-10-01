package nro.models.database;

import nro.models.data.LocalManager;
import nro.models.item.Item;
import nro.models.player.Player;
import nro.models.utils.TimeUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import nro.models.item.Item.ItemOption;

public class HistoryTransactionDAO {

    public static void insert(Player pl1, Player pl2,
            int goldP1, int goldP2,
            List<Item> itemP1, List<Item> itemP2,
            List<Item> bag1Before, List<Item> bag2Before,
            List<Item> bag1After, List<Item> bag2After,
            long gold1Before, long gold2Before,
            long gold1After, long gold2After) {

        String player1 = pl1.name;
        String player2 = pl2.name;

        StringBuilder itemPlayer1 = new StringBuilder("Gold: " + goldP1 + ", ");
        StringBuilder itemPlayer2 = new StringBuilder("Gold: " + goldP2 + ", ");

        List<Item> doGD1 = mergeItemList(itemP1);
        List<Item> doGD2 = mergeItemList(itemP2);

        for (Item item : doGD1) {
            if (item.isNotNullItem()) {
                int qty = item.quantityGD > 0 ? item.quantityGD : item.quantity;
                itemPlayer1.append(formatItemWithSao(item))
                        .append(" (x").append(qty).append("), ");
            }
        }

        for (Item item : doGD2) {
            if (item.isNotNullItem()) {
                int qty = item.quantityGD > 0 ? item.quantityGD : item.quantity;
                itemPlayer2.append(formatItemWithSao(item))
                        .append(" (x").append(qty).append("), ");
            }
        }

        String beforeTran1 = formatBagItemList(bag1Before);
        String beforeTran2 = formatBagItemList(bag2Before);
        String afterTran1 = formatBagItemList(bag1After);
        String afterTran2 = formatBagItemList(bag2After);

        try {
            LocalManager.executeUpdate(
                    "INSERT INTO history_transaction "
                    + "(player_1, player_2, item_player_1, item_player_2, "
                    + "bag_1_before_tran, bag_2_before_tran, bag_1_after_tran, bag_2_after_tran, time_tran) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    player1, player2,
                    itemPlayer1.toString(), itemPlayer2.toString(),
                    beforeTran1, beforeTran2,
                    afterTran1, afterTran2,
                    new Timestamp(System.currentTimeMillis())
            );
        } catch (Exception ex) {
            System.err.println("Lỗi insert lịch sử giao dịch:");
            ex.printStackTrace();
        }
    }

    private static List<Item> mergeItemList(List<Item> itemList) {
        List<Item> result = new ArrayList<>();
        for (Item item : itemList) {
            if (item.isNotNullItem()) {
                Item existing = result.stream()
                        .filter(it -> it.template.id == item.template.id
                        && getSaoPhaLe(it) == getSaoPhaLe(item))
                        .findFirst()
                        .orElse(null);
                if (existing == null) {
                    result.add(item);
                } else {
                    existing.quantityGD += item.quantityGD;
                }
            }
        }
        return result;
    }

    private static String formatItemWithSao(Item item) {
        StringBuilder sb = new StringBuilder(item.template.name);
        List<String> saoDetails = new ArrayList<>();

        if (item != null && item.options != null) {
            for (ItemOption opt : item.options) {
                if (opt != null) {
                    int id = opt.optionTemplate.id;
                    if ((id == 102 || id == 107) && opt.param > 0) {
                        saoDetails.add("+" + opt.param + "sao (" + id + ")");
                    }
                }
            }
        }

        if (!saoDetails.isEmpty()) {
            sb.append(" [").append(String.join(", ", saoDetails)).append("]");
        }

        return sb.toString();
    }

    private static int getSaoPhaLe(Item item) {
        int totalSao = 0;
        if (item != null && item.options != null) {
            for (ItemOption opt : item.options) {
                if (opt != null) {
                    int id = opt.optionTemplate.id;
                    if (id == 102 || id == 107) {
                        totalSao += opt.param;
                    }
                }
            }
        }
        return totalSao;
    }

    private static String formatBagItemList(List<Item> items) {
        StringBuilder result = new StringBuilder();
        for (Item item : items) {
            if (item.isNotNullItem()) {
                result.append(formatItemWithSao(item))
                        .append(" (x").append(item.quantity).append("), ");
            }
        }
        if (result.length() >= 2) {
            result.setLength(result.length() - 2);
        }
        return result.toString();
    }

    public static void deleteHistory() {
        try (Connection con = LocalManager.getConnection(); PreparedStatement ps = con.prepareStatement(
                "DELETE FROM history_transaction WHERE time_tran < '"
                + TimeUtil.getTimeBeforeCurrent(3 * 24 * 60 * 60 * 1000, "yyyy-MM-dd") + "'")) {
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
