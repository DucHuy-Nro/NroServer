package nro.models.combine;

import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.utils.Util;

/**
 *
 * @author By Mr Blue
 */
public class NangCapBongTai {

    private static final int GOLD_BONG_TAI = 200_000_000;
    private static final int GEM_BONG_TAI = 1_000;
    private static final int RATIO_BONG_TAI = 50;
    private static final int ITEM_BONG_TAI_1 = 454;
    private static final int ITEM_BONG_TAI_2 = 921;
    private static final int ITEM_MANH_VO = 933;

    public static void showInfoCombine(Player player) {
        if (player.combineNew.itemsCombine.size() != 2) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Cần 1 Bông tai Porata cấp 1 và 9999 Mảnh vỡ bông tai", "Đóng");
            return;
        }

        Item bongTai = null;
        Item manhVo = null;
        for (Item item : player.combineNew.itemsCombine) {
            if (item.template.id == ITEM_BONG_TAI_1) {
                bongTai = item;
            } else if (item.template.id == ITEM_MANH_VO) {
                manhVo = item;
            }
        }

        if (bongTai == null || manhVo == null) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Cần 1 Bông tai Porata cấp 1 và 9999 Mảnh vỡ bông tai", "Đóng");
            return;
        }

        player.combineNew.goldCombine = GOLD_BONG_TAI;
        player.combineNew.gemCombine = GEM_BONG_TAI;
        player.combineNew.ratioCombine = RATIO_BONG_TAI;

        int soManhVo = countItemInBag(player, ITEM_MANH_VO);
        String npcSay = "|2|Bông tai Porata [+2]\n\n";
        npcSay += "|2|Tỉ lệ thành công: " + RATIO_BONG_TAI + "%\n";

        if (soManhVo < 9999) {
            npcSay += "|7|Cần 9999 " + manhVo.template.name + "\n";
            npcSay += "|2|Cần: " + Util.numberToMoney(GOLD_BONG_TAI) + " vàng\n";
            npcSay += "|2|Cần: " + GEM_BONG_TAI + " ngọc\n";
            npcSay += "|7|Thất bại -99 " + manhVo.template.name + "\n";
            npcSay += "Còn thiếu " + (9999 - soManhVo) + " " + manhVo.template.name;
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
        } else if (player.inventory.gem < GEM_BONG_TAI) {
            npcSay += "|2|Cần 9999 " + manhVo.template.name + "\n";
            npcSay += "|7|Cần: " + GEM_BONG_TAI + " ngọc xanh\n";
            npcSay += "|2|Cần: " + Util.numberToMoney(GOLD_BONG_TAI) + " vàng\n";
            npcSay += "|7|Thất bại -99 " + manhVo.template.name + "\n";
            npcSay += "Còn thiếu\n" + (GEM_BONG_TAI - player.inventory.gem) + " ngọc xanh";
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
        } else if (player.inventory.gold < GOLD_BONG_TAI) {
            npcSay += "|2|Cần 9999 " + manhVo.template.name + "\n";
            npcSay += "|2|Cần: " + GEM_BONG_TAI + " ngọc\n";
            npcSay += "|7|Cần: " + Util.numberToMoney(GOLD_BONG_TAI) + " vàng\n";
            npcSay += "|7|Thất bại -99 " + manhVo.template.name + "\n";
            npcSay += "Còn thiếu " + Util.numberToMoney(GOLD_BONG_TAI - player.inventory.gold) + " vàng";
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
        } else {
            npcSay += "|2|Cần 9999 " + manhVo.template.name + "\n";
            npcSay += "|2|Cần: " + GEM_BONG_TAI + " ngọc\n";
            npcSay += "|2|Cần: " + Util.numberToMoney(GOLD_BONG_TAI) + " vàng\n";
            npcSay += "|7|Thất bại -99 " + manhVo.template.name + "\n";
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                    "Nâng cấp\n" + Util.numberToMoney(GOLD_BONG_TAI) + " vàng\n" + GEM_BONG_TAI + " ngọc", "Từ chối");
        }
    }

    public static void nangCapBongTai(Player player) {
        if (player.combineNew.itemsCombine.size() != 2) {
            return;
        }

        int gold = player.combineNew.goldCombine;
        int gem = player.combineNew.gemCombine;

        if (player.inventory.gold < gold) {
            Service.gI().sendThongBao(player, "Không đủ vàng để thực hiện");
            return;
        }

        if (player.inventory.gem < gem) {
            Service.gI().sendThongBao(player, "Không đủ ngọc để thực hiện");
            return;
        }

        Item bongTai = null;
        Item manhVo = null;
        for (Item item : player.combineNew.itemsCombine) {
            if (item.template.id == ITEM_BONG_TAI_1) {
                bongTai = item;
            } else if (item.template.id == ITEM_MANH_VO) {
                manhVo = item;
            }
        }

        if (bongTai == null || manhVo == null) {
            return;
        }

        if (InventoryService.gI().findItemBag(player, ITEM_BONG_TAI_2) != null) {
            Service.gI().sendThongBao(player, "Ngươi đã có bông tai Porata cấp 2 trong hàng trang rồi.");
            return;
        }

        int soManhVo = countItemInBag(player, ITEM_MANH_VO);
        if (soManhVo < 9999) {
            Service.gI().sendThongBao(player, "Không đủ mảnh vỡ bông tai để nâng cấp");
            return;
        }

        player.inventory.gold -= gold;
        player.inventory.gem -= gem;

        boolean success = Util.isTrue(RATIO_BONG_TAI, 100);
        if (success) {
            bongTai.template = ItemService.gI().getTemplate(ITEM_BONG_TAI_2);
            bongTai.itemOptions.clear();
            bongTai.itemOptions.add(new Item.ItemOption(72, 2));
            CombineService.gI().sendEffectSuccessCombine(player);
            removeItemFromBag(player, ITEM_MANH_VO, 9999);
        } else {
            CombineService.gI().sendEffectFailCombine(player);
            removeItemFromBag(player, ITEM_MANH_VO, 99);
        }

        InventoryService.gI().sendItemBags(player);
        Service.gI().sendMoney(player);
        CombineService.gI().reOpenItemCombine(player);
    }

    private static int countItemInBag(Player player, int itemId) {
        int count = 0;
        for (Item item : player.inventory.itemsBag) {
            if (item != null && item.template != null && item.template.id == itemId) {
                count += item.quantity;
            }
        }
        return count;
    }

    public static void removeItemFromBag(Player player, int itemId, int amount) {
        for (int i = 0; i < player.inventory.itemsBag.size(); i++) {
            Item item = player.inventory.itemsBag.get(i);
            if (item != null && item.template.id == itemId) {
                if (item.quantity > amount) {
                    item.quantity -= amount;
                    break;
                } else {
                    amount -= item.quantity;
                    player.inventory.itemsBag.remove(i);
                    i--;
                    if (amount <= 0) {
                        break;
                    }
                }
            }
        }
    }

}
