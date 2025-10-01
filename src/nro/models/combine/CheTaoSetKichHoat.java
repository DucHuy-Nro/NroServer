package nro.models.combine;

import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.item.Item.ItemOption;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.RewardService;
import nro.models.services.Service;
import nro.models.utils.Util;

/**
 *
 * @author By Mr Blue
 */
public class CheTaoSetKichHoat {

    private static final int COST_DAP_DO_KICH_HOAT = 100_000_000;

    public static void showInfoCombine(Player player) {
        if (player.combineNew.itemsCombine.size() == 1 || player.combineNew.itemsCombine.size() == 2) {
            Item dhd = null, dtl = null;
            for (Item item : player.combineNew.itemsCombine) {
                if (item.isNotNullItem()) {
                    if (item.template.id >= 650 && item.template.id <= 662) {
                        dhd = item;
                    } else if (item.template.id >= 555 && item.template.id <= 567) {
                        dtl = item;
                    }
                }
            }
            if (dhd != null) {
                String npcSay = "|6|" + dhd.template.name + "\n";
                for (ItemOption io : dhd.itemOptions) {
                    npcSay += "|2|" + io.getOptionString() + "\n";
                }
                if (dtl != null) {
                    npcSay += "|6|" + dtl.template.name + "\n";
                    for (ItemOption io : dtl.itemOptions) {
                        npcSay += "|2|" + io.getOptionString() + "\n";
                    }
                }
                npcSay += "Ngươi có muốn chuyển hóa thành\n";
                npcSay += "|1|" + getNameItemC0(dhd.template.gender, dhd.template.type)
                        + " (ngẫu nhiên kích hoạt)\n|7|Tỉ lệ thành công " + (dtl != null ? "100%" : "40%")
                        + "\n|2|Cần " + Util.numberToMoney(COST_DAP_DO_KICH_HOAT) + " vàng";
                if (player.inventory.gold >= COST_DAP_DO_KICH_HOAT) {
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                            "Cần " + Util.numberToMoney(COST_DAP_DO_KICH_HOAT) + " vàng");
                } else {
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay,
                            "Còn thiếu\n" + Util.numberToMoney(COST_DAP_DO_KICH_HOAT - player.inventory.gold) + " vàng");
                }
            } else {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                        "Ta cần 1 món đồ hủy diệt của ngươi để có thể chuyển hóa 1", "Đóng");
            }
        } else {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Ta cần 1 món đồ hủy diệt của ngươi để có thể chuyển hóa 2", "Đóng");
        }
    }

    public static void CheTaoSetKichHoat(Player player) {
        if (player.combineNew.itemsCombine.size() == 1 || player.combineNew.itemsCombine.size() == 2) {
            Item dhd = null, dtl = null;
            for (Item item : player.combineNew.itemsCombine) {
                if (item.isNotNullItem()) {
                    if (item.template.id >= 650 && item.template.id <= 662) {
                        dhd = item;
                    } else if (item.template.id >= 555 && item.template.id <= 567) {
                        dtl = item;
                    }
                }
            }
            if (dhd != null) {
                if (InventoryService.gI().getCountEmptyBag(player) > 0
                        && player.inventory.gold >= COST_DAP_DO_KICH_HOAT) {
                    player.inventory.gold -= COST_DAP_DO_KICH_HOAT;
                    int tiLe = dtl != null ? 100 : 40;
                    if (Util.isTrue(tiLe, 100)) {
                        CombineService.gI().sendEffectSuccessCombine(player);
                        Item item = ItemService.gI()
                                .createNewItem((short) getTempIdItemC0(dhd.template.gender, dhd.template.type));
                        RewardService.gI().initBaseOptionClothes(item.template.id, item.template.type,
                                item.itemOptions);
                        RewardService.gI().initActivationOption(
                                item.template.gender < 3 ? item.template.gender : player.gender,
                                item.template.type, item.itemOptions);
                        InventoryService.gI().addItemBag(player, item);
                    } else {
                        CombineService.gI().sendEffectFailCombine(player);
                    }
                    InventoryService.gI().subQuantityItemsBag(player, dhd, 1);
                    if (dtl != null) {
                        InventoryService.gI().subQuantityItemsBag(player, dtl, 1);
                    }
                    InventoryService.gI().sendItemBags(player);
                    Service.gI().sendMoney(player);
                    CombineService.gI().reOpenItemCombine(player);
                }
            }
        }
    }

    private static String getNameItemC0(int gender, int type) {
        if (type == 4) {
            return "Rada cấp 1";
        }
        switch (gender) {
            case 0:
                return switch (type) {
                    case 0 ->
                        "Áo vải 3 lỗ";
                    case 1 ->
                        "Quần vải đen";
                    case 2 ->
                        "Găng thun đen";
                    case 3 ->
                        "Giầy nhựa";
                    default ->
                        "";
                };
            case 1:
                return switch (type) {
                    case 0 ->
                        "Áo sợi len";
                    case 1 ->
                        "Quần sợi len";
                    case 2 ->
                        "Găng sợi len";
                    case 3 ->
                        "Giầy sợi len";
                    default ->
                        "";
                };
            case 2:
                return switch (type) {
                    case 0 ->
                        "Áo vải thô";
                    case 1 ->
                        "Quần vải thô";
                    case 2 ->
                        "Găng vải thô";
                    case 3 ->
                        "Giầy vải thô";
                    default ->
                        "";
                };
        }
        return "";
    }

    private static int getTempIdItemC0(int gender, int type) {
        if (type == 4) {
            return 12;
        }
        switch (gender) {
            case 0:
                switch (type) {
                    case 0:
                        return 0;
                    case 1:
                        return 6;
                    case 2:
                        return 21;
                    case 3:
                        return 27;
                }
                break;
            case 1:
                switch (type) {
                    case 0:
                        return 1;
                    case 1:
                        return 7;
                    case 2:
                        return 22;
                    case 3:
                        return 28;
                }
                break;
            case 2:
                switch (type) {
                    case 0:
                        return 2;
                    case 1:
                        return 8;
                    case 2:
                        return 23;
                    case 3:
                        return 29;
                }
                break;
        }
        return -1;
    }

}
