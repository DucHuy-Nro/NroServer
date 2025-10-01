package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.minigame.ChonAiDay_Gem;
import nro.models.minigame.ChonAiDay_Gold;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.ItemTimeService;
import nro.models.services.RockPaperScissorsService;
import nro.models.services.Service;
import nro.models.services_func.Input;
import nro.models.utils.Util;
import nro.models.services_func.MiniGame;

public class LyTieuNuong extends Npc {

    public class ConstMiniGame {

        public static final byte MENU_CHINH = 0;
        public static final byte MENU_KEO_BUA_BAO = 1;
        public static final byte MENU_CON_SO_MAY_MAN_VANG = 2;
        public static final byte MENU_CON_SO_MAY_MAN_NGOC = 3;
        public static final byte MENU_CHON_AI_DAY = 4;

        public static final byte MENU_PLAY_KEO_BUA_BAO = 5;

        public static final byte MENU_LUCKY_NUMBER = 6;
        public static final byte MENU_PLAY_LUCKY_NUMBER_GOLD = 7;
        public static final byte MENU_PLAY_LUCKY_NUMBER_GEM = 8;

        public static final byte MENU_PLAY_DECISION_MAKER_GOLD = 9;
        public static final byte MENU_PLAY_DECISION_MAKER_RUBY = 10;
        public static final byte MENU_PLAY_DECISION_MAKER_GEM = 11;
        public static final byte MENU_WAIT_NEW_GAME = 12;
    }

    public LyTieuNuong(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            createOtherMenu(player, ConstNpc.BASE_MENU, "Mini game.",
                    "Kéo\nBúa\nBao",
                    "Con số\nmay mắn\nthỏi vàng",
                    "Con số\nmay mắn\nngọc xanh",
                    "Chọn ai đây",
                    "Đóng");
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (this.mapId == 5) {
                switch (player.idMark.getIndexMenu()) {
                    case ConstNpc.BASE_MENU:
                        handleBaseMenu(player, select);
                        break;
                    case ConstNpc.KEO_BUA_BAO:
                        handleRockPaperScissorsBet(player, select);
                        break;
                    case LyTieuNuong.ConstMiniGame.MENU_PLAY_KEO_BUA_BAO:
                        handleRockPaperScissorsPlay(player, select);
                        break;
                    case ConstNpc.CON_SO_MAY_MAN_NGOC_XANH:
                        xửLýConSoMayManGem(player, select);
                        break;
                    case ConstNpc.CON_SO_MAY_MAN_VANG:
                        xửLýConSoMayManGold(player, select);
                        break;
                    case ConstNpc.CHON_AI_DAY:
                        xửLýChonAiDay(player, select);
                        break;
                    case ConstNpc.CHON_AI_DAY_VANG:
                        xửLýChonAiDayVang(player, select);
                        break;
                    case ConstNpc.CHON_AI_DAY_NGOC:
                        xửLýChonAiDayGem(player, select);
                        break;
                    case ConstNpc.UPDATE_CHON_AI_DAY_NGOC:
                        handleUpdateChonAiDayNgoc(player, select);
                        break;
                }
            }
        }
    }

    private void handleBaseMenu(Player player, int select) {
        switch (select) {
            case 0: // Kéo, Búa, Bao
                createOtherMenu(player, ConstNpc.KEO_BUA_BAO, "Hãy chọn mức cược.",
                        "1 Tr vàng",
                        "5 Tr vàng",
                        "10 Tr vàng");
                break;
            case 1: // Con số may mắn vàng
                xửLýLựaChọnMiniGame_Gold(player);
                break;
            case 2: // Con số may mắn ngọc xanh
                xửLýLựaChọnMiniGame_Gem(player);
                break;
            case 3: // Chọn ai đây
                createOtherMenu(player, ConstNpc.CHON_AI_DAY, "Trò chơi Chọn Ai Đây đang được diễn ra, nếu bạn tin tưởng mình đang tràn đầy "
                        + "may mắn thì có thể tham gia thử", "Thể lệ", "Chọn\nVàng", "Chọn\nngọc xanh");
                break;
        }
    }

    private void handleRockPaperScissorsBet(Player player, int select) {
        long betAmount = switch (select) {
            case 0 ->
                1_000_000L;
            case 1 ->
                5_000_000L;
            case 2 ->
                10_000_000L;
            default ->
                0L;
        };

        if (betAmount > 0) {
            if (player.inventory.gold < betAmount) {
                Service.gI().sendThongBao(player, "Bạn không đủ vàng để chơi!");
                return;
            }
            player.idMark.setMoneyKeoBuaBao((int) betAmount);

            player.idMark.setTimePlayKeoBuaBao(System.currentTimeMillis() + (15 * 1000));
            ItemTimeService.gI().sendTextTimeKeoBuaBao(player, 15);

            createOtherMenu(player, LyTieuNuong.ConstMiniGame.MENU_PLAY_KEO_BUA_BAO,
                    "Mức vàng cược: " + Util.numberToMoney(betAmount) + " vàng\n"
                    + "Hãy chọn Kéo, Búa hoặc Bao\n"
                    + "Thời gian 15 giây bắt đầu",
                    "Kéo", "Búa", "Bao", "Đổi\nmức cược", "Nghỉ chơi");
        }
    }

    private void handleRockPaperScissorsPlay(Player player, int select) {
        if (select < 3) {
            RockPaperScissorsService.play(this, player, select);
        } else if (select == 3) {
            player.idMark.setMoneyKeoBuaBao(0);
            player.idMark.setTimePlayKeoBuaBao(0);
            createOtherMenu(player, ConstNpc.KEO_BUA_BAO, "Hãy chọn mức cược mới.",
                    "1 Tr vàng",
                    "5 Tr vàng",
                    "10 Tr vàng");
        } else if (select == 4) { // Nghỉ chơi
            player.idMark.setMoneyKeoBuaBao(0);
            player.idMark.setTimePlayKeoBuaBao(0);
            Service.gI().sendThongBao(player, "Bạn đã nghỉ chơi.");
        }
    }

    private void xửLýLựaChọnMiniGame_Gem(Player player) {
        String KQ = String.valueOf(MiniGame.gI().MiniGame_S1_Gem.result);
        String Money = String.valueOf(MiniGame.gI().MiniGame_S1_Gem.rewardAmount);
        String second = String.valueOf(MiniGame.gI().MiniGame_S1_Gem.second);
        String number = MiniGame.gI().MiniGame_S1_Gem.strNumber((int) player.id);
        StringBuilder previousResults = new StringBuilder("Lịch sử: ");
        if (MiniGame.gI().MiniGame_S1_Gem.dataKQ_CSMM != null && !MiniGame.gI().MiniGame_S1_Gem.dataKQ_CSMM.isEmpty()) {
            int maxResultsToShow = Math.min(10, MiniGame.gI().MiniGame_S1_Gem.dataKQ_CSMM.size());
            for (int i = MiniGame.gI().MiniGame_S1_Gem.dataKQ_CSMM.size() - maxResultsToShow; i < MiniGame.gI().MiniGame_S1_Gem.dataKQ_CSMM.size(); i++) {
                previousResults.append(MiniGame.gI().MiniGame_S1_Gem.dataKQ_CSMM.get(i));
                if (i < MiniGame.gI().MiniGame_S1_Gem.dataKQ_CSMM.size() - 1) {
                    previousResults.append(", ");
                }
            }
        } else {
            previousResults.append("Chưa có");
        }
        String npcSay = ""
                + "Kết quả giải trước: " + KQ + "\n"
                + previousResults.toString() + "\n"
                + "Tổng giải thưởng: " + Money + " ngọc\n"
                + "<" + second + ">giây\n"
                + (number != null && !number.isEmpty() ? "Các số bạn chọn: " + number : "Bạn chưa chọn số nào.");
        String[] Menus = {
            "Cập nhật",
            "1 Số\n" + MiniGame.gI().MiniGame_S1_Gem.cost + " ngọc xanh",
            "Ngẫu nhiên\n1 số lẻ\n" + MiniGame.gI().MiniGame_S1_Gem.cost + " ngọc xanh",
            "Ngẫu nhiên\n1 số chẵn\n" + MiniGame.gI().MiniGame_S1_Gem.cost + " ngọc xanh",
            "Hướng\ndẫn\nthêm",
            "Đóng"
        };
        createOtherMenu(player, ConstNpc.CON_SO_MAY_MAN_NGOC_XANH, npcSay, Menus);
    }

    private void xửLýLựaChọnMiniGame_Gold(Player player) {
        String KQ = String.valueOf(MiniGame.gI().MiniGame_S1_Gold.result);
        String Money = Util.mumberToBlue(MiniGame.gI().MiniGame_S1_Gold.rewardAmount);
        String second = String.valueOf(MiniGame.gI().MiniGame_S1_Gold.second);
        String number = MiniGame.gI().MiniGame_S1_Gold.strNumber((int) player.id);
        StringBuilder previousResults = new StringBuilder("Lịch sử: ");
        if (MiniGame.gI().MiniGame_S1_Gold.dataKQ_CSMM != null && !MiniGame.gI().MiniGame_S1_Gold.dataKQ_CSMM.isEmpty()) {
            int maxResultsToShow = Math.min(10, MiniGame.gI().MiniGame_S1_Gold.dataKQ_CSMM.size());
            for (int i = MiniGame.gI().MiniGame_S1_Gold.dataKQ_CSMM.size() - maxResultsToShow; i < MiniGame.gI().MiniGame_S1_Gold.dataKQ_CSMM.size(); i++) {
                previousResults.append(MiniGame.gI().MiniGame_S1_Gold.dataKQ_CSMM.get(i));
                if (i < MiniGame.gI().MiniGame_S1_Gold.dataKQ_CSMM.size() - 1) {
                    previousResults.append(", ");
                }
            }
        } else {
            previousResults.append("Chưa có");
        }
        String npcSay = ""
                + "Kết quả giải trước: " + KQ + "\n"
                + previousResults.toString() + "\n"
                + "Tổng giải thưởng: " + Money + " thỏi vàng\n"
                + "<" + second + ">giây\n"
                + (number != null && !number.isEmpty() ? "Các số bạn chọn: " + number : "Bạn chưa chọn số nào.");
        String[] Menus = {
            "Cập nhật",
            "1 Số\n" + MiniGame.gI().MiniGame_S1_Gold.cost + " thỏi vàng",
            "Ngẫu nhiên\n1 số lẻ\n" + MiniGame.gI().MiniGame_S1_Gold.cost + " thỏi vàng",
            "Ngẫu nhiên\n1 số chẵn\n" + MiniGame.gI().MiniGame_S1_Gold.cost + " thỏi vàng",
            "Hướng\ndẫn\nthêm",
            "Đóng"
        };
        createOtherMenu(player, ConstNpc.CON_SO_MAY_MAN_VANG, npcSay, Menus);
    }

    private void xửLýConSoMayManGem(Player player, int select) {
        switch (select) {
            case 0:
                xửLýLựaChọnMiniGame_Gem(player);
                break;
            case 1:
                Input.gI().createFormConSoMayMan_Gem(player);
                break;
            case 2:
                MiniGame.gI().MiniGame_S1_Gem.ramdom1SoLe(player);
                break;
            case 3:
                MiniGame.gI().MiniGame_S1_Gem.ramdom1SoChan(player);
                break;
            case 4:
                createOtherMenu(player, 1, "Thời gian từ 8h đến hết 21h59 hằng ngày\n"
                        + "Mỗi lượt được chọn 10 con số từ 0 đến 99\n"
                        + "Thời gian mỗi lượt là 5 phút.", "Đồng ý");
                break;
        }
    }

    private void xửLýConSoMayManGold(Player player, int select) {
        switch (select) {
            case 0:
                xửLýLựaChọnMiniGame_Gold(player);
                break;
            case 1:
                Input.gI().createFormConSoMayMan_Gold(player);
                break;
            case 2:
                MiniGame.gI().MiniGame_S1_Gold.ramdom1SoLe(player);
                break;
            case 3:
                MiniGame.gI().MiniGame_S1_Gold.ramdom1SoChan(player);
                break;
            case 4:
                createOtherMenu(player, 1, "Thời gian từ 8h đến hết 21h59 hằng ngày\n"
                        + "Mỗi lượt được chọn 10 con số từ 0 đến 99\n"
                        + "Thời gian mỗi lượt là 5 phút.", "Đồng ý");
                break;
        }
    }

    private void xửLýChonAiDay(Player player, int select) {
        String timeGold = ((ChonAiDay_Gold.gI().lastTimeEnd - System.currentTimeMillis()) / 1000) + " giây";
        String timeGem = ((ChonAiDay_Gem.gI().lastTimeEnd - System.currentTimeMillis()) / 1000) + " giây";

        if (((ChonAiDay_Gold.gI().lastTimeEnd - System.currentTimeMillis()) / 1000) < 0) {
            ChonAiDay_Gold.gI().lastTimeEnd = System.currentTimeMillis() + 300000;
        }
        if (((ChonAiDay_Gem.gI().lastTimeEnd - System.currentTimeMillis()) / 1000) < 0) {
            ChonAiDay_Gem.gI().lastTimeEnd = System.currentTimeMillis() + 300000;
        }

        switch (select) {
            case 0:
                createOtherMenu(player, ConstNpc.IGNORE_MENU, "Mỗi lượt chơi có 6 giải thưởng\n"
                        + "Được chọn tối đa 10 lần mỗi giải\n"
                        + "Thời gian 1 lượt chọn là 5 phút\n"
                        + "Khi hết giờ, hệ thống sẽ ngẫu nhiên chọn ra 1 người may mắn\n"
                        + "của từng giải và trao thưởng.\n"
                        + "Lưu ý: Nếu tham gia bằng Ngọc Xanh hoặc Hồng ngọc thì người thắng sẽ nhận thưởng là hồng ngọc.", "OK");
                break;
            case 1:
                createOtherMenu(player, ConstNpc.CHON_AI_DAY_VANG, "Tổng giải thưởng: " + Util.numberToMoney(ChonAiDay_Gold.gI().goldNormar) + " vàng, cơ hội trúng của bạn là: " + player.percentGold(0) + "%\n"
                        + "Tổng giải VIP: " + Util.numberToMoney(ChonAiDay_Gold.gI().goldVip) + " vàng, cơ hội trúng của bạn là: " + player.percentGold(1) + "%\n"
                        + "Thời gian còn lại: " + timeGold, "Cập nhập", "Thường\n1 triệu\nvàng", "VIP\n10 triệu\nvàng", "Đóng");
                break;
            case 2:
                createOtherMenu(player, ConstNpc.CHON_AI_DAY_NGOC, "Tổng giải thưởng: " + Util.numberToMoney(ChonAiDay_Gem.gI().gemNormar) + " hồng ngọc, cơ hội trúng của bạn là: " + player.percentGem(0) + "%\nTổng giải VIP: " + Util.numberToMoney(ChonAiDay_Gem.gI().gemVip) + " hồng ngọc, cơ hội trúng của bạn là: " + player.percentGem(1) + "%\n Thời gian còn lại: " + timeGem, "Cập nhập", "Thường\n10 ngọc\nxanh", "VIP\n100 ngọc\nxanh", "Đóng");
                break;
        }
    }

    private void xửLýChonAiDayVang(Player player, int select) {
        String time = ((ChonAiDay_Gold.gI().lastTimeEnd - System.currentTimeMillis()) / 1000) + " giây";
        switch (select) {
            case 0:
                createOtherMenu(player, ConstNpc.CHON_AI_DAY_VANG, "Tổng giải thường: " + Util.numberToMoney(ChonAiDay_Gold.gI().goldNormar) + " vàng, cơ hội trúng của bạn là: " + player.percentGold(0) + "%\nTổng giải VIP: " + Util.numberToMoney(ChonAiDay_Gold.gI().goldVip) + " vàng, cơ hội trúng của bạn là: " + player.percentGold(1) + "%\n Thời gian còn lại: " + time, "Cập nhập", "Thường\n1 triệu\nvàng", "VIP\n10 triệu\nvàng", "Đóng");
                break;
            case 1:
                xửLýThuong1TrieuVang(player);
                break;
            case 2:
                xửLýVIP10TrieuVang(player);
                break;
        }
    }

    private void xửLýChonAiDayGem(Player player, int select) {
        String time = ((ChonAiDay_Gem.gI().lastTimeEnd - System.currentTimeMillis()) / 1000) + " giây";
        switch (select) {
            case 0:
                createOtherMenu(player, ConstNpc.CHON_AI_DAY_NGOC, "Tổng giải thường: " + Util.numberToMoney(ChonAiDay_Gem.gI().gemNormar) + " hồng ngọc, cơ hội trúng của bạn là: " + player.percentGem(0) + "%\nTổng giải VIP: " + Util.numberToMoney(ChonAiDay_Gem.gI().gemVip) + " hồng ngọc, cơ hội trúng của bạn là: " + player.percentGem(1) + "%\n Thời gian còn lại: " + time, "Cập nhập", "Thường\n10 ngọc\nxanh", "VIP\n100 ngọc\nxanh", "Đóng");
                break;
            case 1:
                xửLýThuong10NgocXanh(player);
                break;
            case 2:
                xửLýVIP100NgocXanh(player);
                break;
        }
    }

    private void handleUpdateChonAiDayNgoc(Player player, int select) {
        if (select == 0) {
            createOtherMenu(player, ConstNpc.UPDATE_CHON_AI_DAY_NGOC, "Thời gian từ 8h đến hết 21h59 hằng ngày\n"
                    + "Mỗi lượt được chọn 10 con số từ 0 đến 99\n"
                    + "Thời gian mỗi lượt là 5 phút", "Cập nhật", "Đóng");
        }
    }

    private void xửLýThuong1TrieuVang(Player player) {
        try {
            String time = ((ChonAiDay_Gold.gI().lastTimeEnd - System.currentTimeMillis()) / 1000) + " giây";
            if (((ChonAiDay_Gold.gI().lastTimeEnd - System.currentTimeMillis()) / 1000) < 0) {
                ChonAiDay_Gold.gI().lastTimeEnd = System.currentTimeMillis() + 300000;
            }
            if (player.inventory.gold >= 1_000_000) {
                player.inventory.gold -= 1_000_000;
                Service.gI().sendMoney(player);
                player.goldNormar += 1_000_000;
                ChonAiDay_Gold.gI().goldNormar += 1_000_000;
                ChonAiDay_Gold.gI().addPlayerNormar(player);
                createOtherMenu(player, ConstNpc.CHON_AI_DAY_VANG, "Tổng giải thường: " + Util.numberToMoney(ChonAiDay_Gold.gI().goldNormar) + " vàng, cơ hội trúng của bạn là: " + player.percentGold(0) + "%\nTổng giải VIP: " + Util.numberToMoney(ChonAiDay_Gold.gI().goldVip) + " vàng, cơ hội trúng của bạn là: " + player.percentGold(1) + "%\n Thời gian còn lại: " + time, "Cập nhập", "Thường\n1 triệu\nvàng", "VIP\n10 triệu\nvàng", "Đóng");
            } else {
                Service.gI().sendThongBao(player, "Bạn không đủ vàng");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void xửLýVIP10TrieuVang(Player player) {
        try {
            String time = ((ChonAiDay_Gold.gI().lastTimeEnd - System.currentTimeMillis()) / 1000) + " giây";
            if (((ChonAiDay_Gold.gI().lastTimeEnd - System.currentTimeMillis()) / 1000) < 0) {
                ChonAiDay_Gold.gI().lastTimeEnd = System.currentTimeMillis() + 300000;
            }
            if (player.inventory.gold >= 10_000_000) {
                player.inventory.gold -= 10_000_000;
                Service.gI().sendMoney(player);
                player.goldVIP += 10_000_000;
                ChonAiDay_Gold.gI().goldVip += 10_000_000;
                ChonAiDay_Gold.gI().addPlayerVIP(player);
                createOtherMenu(player, ConstNpc.CHON_AI_DAY_VANG, "Tổng giải thường: " + Util.numberToMoney(ChonAiDay_Gold.gI().goldNormar) + " vàng, cơ hội trúng của bạn là: " + player.percentGold(0) + "%\nTổng giải VIP: " + Util.numberToMoney(ChonAiDay_Gold.gI().goldVip) + " vàng, cơ hội trúng của bạn là: " + player.percentGold(1) + "%\n Thời gian còn lại: " + time, "Cập nhập", "Thường\n1 triệu\nvàng", "VIP\n10 triệu\nvàng", "Đóng");
            } else {
                Service.gI().sendThongBao(player, "Bạn không đủ vàng");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void xửLýThuong10NgocXanh(Player player) {
        try {
            String time = ((ChonAiDay_Gem.gI().lastTimeEnd - System.currentTimeMillis()) / 1000) + " giây";
            if (((ChonAiDay_Gem.gI().lastTimeEnd - System.currentTimeMillis()) / 1000) < 0) {
                ChonAiDay_Gem.gI().lastTimeEnd = System.currentTimeMillis() + 300000;
            }
            if (player.inventory.gem >= 10) {
                player.inventory.gem -= 10;
                Service.gI().sendMoney(player);
                player.gemNormar += 10;
                ChonAiDay_Gem.gI().gemNormar += 10;
                ChonAiDay_Gem.gI().addPlayerNormar(player);
                createOtherMenu(player, ConstNpc.CHON_AI_DAY_NGOC, "Tổng giải thường: " + Util.numberToMoney(ChonAiDay_Gem.gI().gemNormar) + " hồng ngọc, cơ hội trúng của bạn là: " + player.percentGem(0) + "%\nTổng giải VIP: " + Util.numberToMoney(ChonAiDay_Gem.gI().gemVip) + " hồng ngọc, cơ hội trúng của bạn là: " + player.percentGem(1) + "%\n Thời gian còn lại: " + time, "Cập nhập", "Thường\n10 ngọc\nxanh", "VIP\n100 ngọc\nxanh", "Đóng");
            } else {
                Service.gI().sendThongBao(player, "Bạn không đủ ngọc xanh");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void xửLýVIP100NgocXanh(Player player) {
        try {
            String time = ((ChonAiDay_Gem.gI().lastTimeEnd - System.currentTimeMillis()) / 1000) + " giây";
            if (((ChonAiDay_Gem.gI().lastTimeEnd - System.currentTimeMillis()) / 1000) < 0) {
                ChonAiDay_Gem.gI().lastTimeEnd = System.currentTimeMillis() + 300000;
            }
            if (player.inventory.gem >= 100) {
                player.inventory.gem -= 100;
                Service.gI().sendMoney(player);
                player.gemVIP += 100;
                ChonAiDay_Gem.gI().gemVip += 100;
                ChonAiDay_Gem.gI().addPlayerVIP(player);
                createOtherMenu(player, ConstNpc.CHON_AI_DAY_NGOC, "Tổng giải thường: " + Util.numberToMoney(ChonAiDay_Gem.gI().gemNormar) + " hồng ngọc, cơ hội trúng của bạn là: " + player.percentGem(0) + "%\nTổng giải VIP: " + Util.numberToMoney(ChonAiDay_Gem.gI().gemVip) + " hồng ngọc, cơ hội trúng của bạn là: " + player.percentGem(1) + "%\n Thời gian còn lại: " + time, "Cập nhập", "Thường\n10 ngọc\nxanh", "VIP\n100 ngọc\nxanh", "Đóng");
            } else {
                Service.gI().sendThongBao(player, "Bạn không đủ ngọc xanh");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
