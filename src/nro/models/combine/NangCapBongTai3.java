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
public class NangCapBongTai3 {

    private static final int GEM_BONG_TAI = 50000;
    private static final int RATIO_BONG_TAI = 20;
    private static final int ITEM_BONG_TAI_2 = 921;
    private static final int ITEM_BONG_TAI_3 = 1816;
    private static final int ITEM_MANH_VO = 1815;
    private static final int HON_BONG_TAI = 934;
    private static final int DA_XANH_LAM = 935;

    public static void showInfoCombine(Player player) {
        if (player.combineNew.itemsCombine.size() != 1) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Cần 1 Bông tai Porata cấp 2 để nâng cấp", "Đóng");
            return;
        }

        Item bongTai = player.combineNew.itemsCombine.get(0);
        if (bongTai.template.id != ITEM_BONG_TAI_2) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Vật phẩm không hợp lệ. Cần Bông tai Porata cấp 2.", "Đóng");
            return;
        }

        int soManhVo = countItemInBag(player, ITEM_MANH_VO);
        int soHon = countItemInBag(player, HON_BONG_TAI);
        int soDa = countItemInBag(player, DA_XANH_LAM);

        String npcSay = "|2|Bông tai Porata [+3]\n\n";
        npcSay += "|2|Tỉ lệ thành công: " + RATIO_BONG_TAI + "%\n";
        npcSay += "|2|Cần 20.000 Mảnh Bông Tai Cấp 3\n";
        npcSay += "|2|Cần 99 Hồn Bông Tai\n";
        npcSay += "|2|Cần 2 Đá Xanh Lam\n";
        npcSay += "|2|Cần: " + GEM_BONG_TAI + " ngọc\n";

        if (soManhVo < 20000 || soHon < 99 || soDa < 2 || player.inventory.gem < GEM_BONG_TAI) {
            npcSay += "\n|7|Thiếu nguyên liệu:\n";
            if (soManhVo < 20000) {
                npcSay += "- Còn thiếu " + (20000 - soManhVo) + " Mảnh Bông Tai\n";
            }
            if (soHon < 99) {
                npcSay += "- Còn thiếu " + (99 - soHon) + " Hồn Bông Tai\n";
            }
            if (soDa < 2) {
                npcSay += "- Còn thiếu " + (2 - soDa) + " Đá Xanh Lam\n";
            }
            if (player.inventory.gem < GEM_BONG_TAI) {
                npcSay += "- Còn thiếu " + (GEM_BONG_TAI - player.inventory.gem) + " ngọc\n";
            }
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
            return;
        }

        player.combineNew.gemCombine = GEM_BONG_TAI;
        player.combineNew.ratioCombine = RATIO_BONG_TAI;
        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                "Nâng cấp\n" + GEM_BONG_TAI + " ngọc", "Từ chối");
    }

    public static void nangCapBongTai(Player player) {
        if (player.combineNew.itemsCombine.size() != 1) {
            return;
        }

        int gem = player.combineNew.gemCombine;

        if (player.inventory.gem < gem) {
            Service.gI().sendThongBao(player, "Không đủ ngọc để thực hiện");
            return;
        }

        Item bongTai = player.combineNew.itemsCombine.get(0);
        if (bongTai.template.id != ITEM_BONG_TAI_2) {
            return;
        }

        if (InventoryService.gI().findItemBag(player, ITEM_BONG_TAI_3) != null) {
            Service.gI().sendThongBao(player, "Ngươi đã có bông tai Porata cấp 3 trong hành trang rồi.");
            return;
        }

        int soManhVo = countItemInBag(player, ITEM_MANH_VO);
        int soHon = countItemInBag(player, HON_BONG_TAI);
        int soDa = countItemInBag(player, DA_XANH_LAM);

        if (soManhVo < 20000 || soHon < 99 || soDa < 2) {
            Service.gI().sendThongBao(player, "Không đủ nguyên liệu để nâng cấp");
            return;
        }

        player.inventory.gem -= gem;

        boolean success = Util.isTrue(20, 100);
        if (success) {
            bongTai.template = ItemService.gI().getTemplate(ITEM_BONG_TAI_3);
            bongTai.itemOptions.clear();
            bongTai.itemOptions.add(new Item.ItemOption(72, 3));
            CombineService.gI().sendEffectSuccessCombine(player);
            removeItemFromBag(player, ITEM_MANH_VO, 20000);
            removeItemFromBag(player, HON_BONG_TAI, 99);
            removeItemFromBag(player, DA_XANH_LAM, 2);
        } else {
            CombineService.gI().sendEffectFailCombine(player);
            removeItemFromBag(player, ITEM_MANH_VO, 999);
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
