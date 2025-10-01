package nro.models.services;

import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.utils.Util;

/**
 *
 * @author By Mr Blue
 */
import java.util.Random;
import nro.models.consts.ConstFont;
import nro.models.npc_list.LyTieuNuong;

public class RockPaperScissorsService {

    public static void play(Npc npc, Player player, int playerChoice) {
        if (player.idMark.getTimePlayKeoBuaBao() <= System.currentTimeMillis() || player.idMark.getMoneyKeoBuaBao() == 0) {
            Service.gI().sendThongBao(player, "Thời gian đã hết hoặc bạn chưa cược tiền!");
            return;
        }

        player.idMark.setTimePlayKeoBuaBao(System.currentTimeMillis() + (15 * 1000));
        ItemTimeService.gI().sendTextTimeKeoBuaBao(player, 15);

        long betAmount = player.idMark.getMoneyKeoBuaBao();
        if (player.inventory.gold < betAmount) {
            Service.gI().sendThongBao(player, "Bạn không đủ vàng để chơi!");
            return;
        }

        player.inventory.gold -= betAmount;
        Service.gI().sendMoney(player);

        int machineChoice = new Random().nextInt(3);

        String playerChoiceName = getChoiceName(playerChoice);
        String machineChoiceName = getChoiceName(machineChoice);
        String message;

        if (playerChoice == machineChoice) {
            message = ConstFont.BOLD_BLUE + "Bạn ra cái <" + playerChoiceName + ">\n"
                    + "Tôi ra cái <" + machineChoiceName + ">\n"
                    + ConstFont.BOLD_YELLOW + "Hòa nhau nhé haha";
            player.inventory.gold += betAmount;
            Service.gI().sendMoney(player);
        } else if ((playerChoice == 0 && machineChoice == 2)
                || (playerChoice == 1 && machineChoice == 0)
                || (playerChoice == 2 && machineChoice == 1)) {
            long reward = betAmount * 96 / 100;
            player.inventory.gold += reward + betAmount;
            Service.gI().sendMoney(player);
            message = ConstFont.BOLD_GREEN + "Bạn ra cái <" + playerChoiceName + ">\n"
                    + "Tôi ra cái <" + machineChoiceName + ">\n"
                    + ConstFont.BOLD_DARK + "Bạn thắng rồi huhu\n"
                    + ConstFont.BOLD_GREEN + "Bạn nhận được " + Util.numberToMoney(reward) + " vàng.";
        } else {
            message = ConstFont.BOLD_RED + "Bạn ra cái <" + playerChoiceName + ">\n"
                    + "Tôi ra cái <" + machineChoiceName + ">\n"
                    + ConstFont.BOLD_DARK + "Tôi thắng nhé hihi\n"
                    + ConstFont.BOLD_RED + "Bạn bị trừ " + Util.numberToMoney(betAmount) + " vàng.";
        }

        npc.createOtherMenu(player, LyTieuNuong.ConstMiniGame.MENU_PLAY_KEO_BUA_BAO,
                message,
                "Kéo", "Búa", "Bao", "Đổi\nmức cược", "Nghỉ chơi");
    }

    private static String getChoiceName(int choice) {
        return switch (choice) {
            case 0 ->
                "Kéo";
            case 1 ->
                "Búa";
            case 2 ->
                "Bao";
            default ->
                "Không xác định";
        };
    }
}
