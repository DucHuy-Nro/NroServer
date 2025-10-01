package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.PlayerService;
import nro.models.services.Service;
import nro.models.task.TaskMain;

/**
 *
 * @author Mr Bue
 */
public class Berry extends Npc {

    public Berry(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (!canOpenNpc(player)) {
            return;
        }

        if (player.playerTask != null && player.playerTask.taskMain != null) {
            TaskMain taskMain = player.playerTask.taskMain;

            if (taskMain.id == 29) {
                if (taskMain.index == 6) {
                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                            "Cậu sẽ đưa tôi về chỗ Bardock thật sao ?",
                            "Ok", "Từ chối");
                    return;
                }
            }
        }
        this.createOtherMenu(player, ConstNpc.BASE_MENU, "Đưa em đi đu đưa đi, đưa em đi đu đưa đi", "Từ chối");
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) {
            return;
        }

        if (player.idMark.isBaseMenu()) {
            if (select == 0 && player.playerTask != null && player.playerTask.taskMain != null) {
                TaskMain taskMain = player.playerTask.taskMain;

                if (taskMain.id == 29 && taskMain.index == 6) {
                    Service.gI().chat(player, "Ok, cứ để tôi lo");

                    if (player.wearingBackItemId != 78) {
                        player.wearingBackItemId = 78;

                        PlayerService.gI().sendWearingBackItem(player);

                        Service.gI().sendThongBao(player, "Bạn nhận được berry! hãy đến gặp Bardock");
                    } else {
                        Service.gI().sendThongBao(player, "Bạn đã mang theo berry rồi hãy tìm tới Bardock!");
                    }
                }
            }
        }
    }

}
