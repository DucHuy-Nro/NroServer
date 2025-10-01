package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.Service;
import nro.models.services.TaskService;
import nro.models.task.SubTaskMain;
import nro.models.task.TaskMain;

/**
 *
 * @author By Mr Blue
 *
 */
public class Bardock extends Npc {

    public Bardock(int mapId, int status, int cx, int cy, int tempId, int avartar) {
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
                if (taskMain.index == 2) {
                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                            "Tôi tên là Bardock, người Xayda\nHành tinh của tôi vừa bị Fide phá huỷ\nKhông biết tại sao tôi lại thoát chết...\n"
                            + "và xuất hiện tại nơi này\nTôi đang bị thương, cậu có thể giúp tôi hạ đám lính ngoài kia không?",
                            "Ok", "Từ chối");
                    return;
                } else if (taskMain.index == 3) {
                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                            "Sáng ra bờ suối, tối vào hang", "Ok");
                    return;
                } else if (taskMain.index == 8) {
                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                            "Sáng ra bờ suối, tối vào hang", "Ok");
                    return;
                } else if (taskMain.index == 6) {
                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                            "Mơn cậu lần nữa\nHiện tại trong hang không còn gì để ăn\nCậu có thể giúp tôi tìm một ít lương thực được không ?",
                            "Đồng ý", "Từ chối");
                    return;
                } else if (taskMain.index == 7) {
                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                            "Mơn cậu lần nữa\nVới số lương thực này tôi sẽ sớm bình phục\nNgoài kia bọn lính đang ức hiếp cư dân hành tinh này\nMong cậu có thể ra sức lần nữa để cứu họ.",
                            "Đồng ý", "Từ chối");
                    return;
                }
            }
        }
        this.createOtherMenu(player, ConstNpc.BASE_MENU, "Tôi là Bardock, có việc gì sao?", "Ok");
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) {
            return;
        }

        if (player.idMark.isBaseMenu()) {
            if (select == 0 && player.playerTask != null && player.playerTask.taskMain != null) {
                TaskMain taskMain = player.playerTask.taskMain;

                if (taskMain.id == 29) {
                    switch (taskMain.index) {

                        case 2 -> {
                            Service.gI().chat(player, "Ok, cứ để tôi lo");
                            TaskService.gI().checkDoneTaskTalkNpc(player, this);
                        }

                        case 4 -> {
                            Service.gI().chat(player, "Tôi sẽ làm ngay!");
                            if (taskMain.subTasks != null) {
                                for (SubTaskMain stm : taskMain.subTasks) {
                                    stm.count = 0;
                                }
                            }
                            TaskService.gI().checkDoneTaskTalkNpc(player, this);
                        }

                        case 6 -> {
                            if (player.wearingBackItemId == 78) {
                                TaskService.gI().checkDoneTaskTalkNpc(player, this);
                                Service.gI().sendFlagBag(player);
                                Service.gI().chat(player, "Yên tâm, tôi tìm giúp cho");
                            } else {
                                Service.gI().chat(player, "Mang Berry tới đây rồi hẵng nói chuyện");
                            }
                        }

                        case 7 -> {
                            Item item993 = InventoryService.gI().findItemBag(player, 993);
                            if (item993 != null && item993.quantity >= 99) {
                                InventoryService.gI().subQuantityItemsBag(player, item993, 99);
                                InventoryService.gI().sendItemBags(player);

                                TaskService.gI().checkDoneTaskTalkNpc(player, this);

                                Service.gI().sendThongBao(player, "Bạn đã thu thập đủ 99 thức ăn, nhiệm vụ tiếp theo bắt đầu!");
                            } else {
                                Service.gI().sendThongBao(player, "Bạn chưa đủ 99 thức ăn để hoàn thành nhiệm vụ!");
                            }
                        }

                        default -> {
                            Service.gI().sendThongBao(player, "Chưa có xử lý cho nhiệm vụ này!");
                        }
                    }
                }
            }
        }
    }

}
