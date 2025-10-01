package nro.models.combine;

import java.sql.*;
import nro.models.consts.ConstNpc;
import nro.models.player.Player;
import nro.models.map.service.NpcService;
import nro.models.utils.Util;

public class KiemTraGiaoDich {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/ngocrong";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    public static void hienThiLichSuGiaoDich(Player player) {
        String playerNameForQuery = player.name; // ✅ Chỉ dùng tên, không thêm ID
        String query = "SELECT * FROM history_transaction WHERE player_1 = ? OR player_2 = ? ORDER BY time_tran DESC";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, playerNameForQuery);
            stmt.setString(2, playerNameForQuery);

            ResultSet rs = stmt.executeQuery();

            StringBuilder message = new StringBuilder();
            boolean hasResult = false;

            message.append("LỊCH SỬ GIAO DỊCH CỦA BẠN:\n\n");

            while (rs.next()) {
                hasResult = true;
                String player1 = rs.getString("player_1");
                String player2 = rs.getString("player_2");
                String item1 = rs.getString("item_player_1");
                String item2 = rs.getString("item_player_2");

                boolean isPlayer1 = playerNameForQuery.equals(player1);
                String otherPlayer = isPlayer1 ? player2 : player1;

                message.append("➤ Giao dịch với: ").append(otherPlayer).append("\n");

                if (isPlayer1) {
                    message.append("Bạn đưa: ").append(item1).append("\n");
                    message.append("Bạn nhận: ").append(item2).append("\n");
                } else {
                    message.append("Bạn nhận: ").append(item1).append("\n");
                    message.append("Bạn đưa: ").append(item2).append("\n");
                }

                message.append("⏰ ").append(Util.formatTimestamp(rs.getTimestamp("time_tran"))).append("\n\n");
            }

            String dialogMessage = hasResult
                    ? message.toString()
                    : "Không tìm thấy giao dịch nào gần đây.";

            NpcService.gI().createBigMessage(player, ConstNpc.AVATAR, dialogMessage, (byte) 0, "", "");

        } catch (SQLException e) {
            String errorMessage = "Lỗi khi truy xuất lịch sử giao dịch:\n" + e.getMessage();
            NpcService.gI().createBigMessage(player, ConstNpc.AVATAR, errorMessage, (byte) 0, "", "");
            e.printStackTrace();
        } catch (Exception e) {
            String errorMessage = "Lỗi không xác định khi hiển thị lịch sử giao dịch:\n" + e.getMessage();
            NpcService.gI().createBigMessage(player, ConstNpc.AVATAR, errorMessage, (byte) 0, "", "");
            e.printStackTrace();
        }
    }
}
