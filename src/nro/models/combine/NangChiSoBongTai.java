package nro.models.combine;

import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.Service;
import nro.models.utils.Util;

/**
 *
 * @author By Mr Blue
 */
public class NangChiSoBongTai {

    private static final int GOLD_BONG_TAI = 200_000_000;
    private static final int GEM_BONG_TAI = 1_000;
    private static final int GEM_NANG_BT = 1_000;
    private static final int RATIO_BONG_TAI = 50;
    private static final int RATIO_NANG_CAP = 45;

    public static void showInfoCombine(Player player) {
        if (player.combineNew.itemsCombine.size() == 3) {
            Item bongTai = null;
            Item honBongTai = null;
            Item daXanhLam = null;
            for (Item item : player.combineNew.itemsCombine) {
                if (item.isNotNullItem()) {
                    switch (item.template.id) {
                        case 921 ->
                            bongTai = item;
                        case 934 ->
                            honBongTai = item;
                        case 935 ->
                            daXanhLam = item;
                    }
                }
            }
            if (bongTai != null && honBongTai != null && daXanhLam != null) {

                player.combineNew.goldCombine = GOLD_BONG_TAI;
                player.combineNew.gemCombine = GEM_NANG_BT;
                player.combineNew.ratioCombine = RATIO_NANG_CAP;

                String npcSay = "|2|Bông tai Porata [+2]" + "\n\n";
                npcSay += "|2|Tỉ lệ thành công: " + player.combineNew.ratioCombine + "%" + "\n";
                if (daXanhLam.quantity < 1) {
                    npcSay += "|2|Cần 99 " + honBongTai.template.name + "\n";
                    npcSay += "|7|Cần 1 " + daXanhLam.template.name + "\n";
                    npcSay += "|2|Cần: " + player.combineNew.gemCombine + " ngọc\n";
                    npcSay += "|1|+1 Chỉ số ngẫu nhiên";
                    npcSay += "|2|Còn thiếu\n" + (1 - daXanhLam.quantity) + " " + daXanhLam.template.name;
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
                } else if (honBongTai.quantity < 99) {
                    npcSay += "|7|Cần 99 " + honBongTai.template.name + "\n";
                    npcSay += "|2|Cần 1 " + daXanhLam.template.name + "\n";
                    npcSay += "|2|Cần: " + player.combineNew.gemCombine + " ngọc\n";
                    npcSay += "|1|+1 Chỉ số ngẫu nhiên";
                    npcSay += "|2|Còn thiếu\n" + (99 - honBongTai.quantity) + " " + honBongTai.template.name;
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
                } else if (player.inventory.gem >= player.combineNew.gemCombine) {
                    npcSay += "|2|Cần 99 " + honBongTai.template.name + "\n";
                    npcSay += "|2|Cần 1 " + daXanhLam.template.name + "\n";
                    npcSay += "|2|Cần: " + player.combineNew.gemCombine + " ngọc\n";
                    npcSay += "|1|+1 Chỉ số ngẫu nhiên";
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                            "Nâng cấp\n" + player.combineNew.gemCombine + " ngọc", "Từ chối");
                } else if (player.inventory.gem < player.combineNew.gemCombine) {
                    npcSay += "|2|Cần 99 " + honBongTai.template.name + "\n";
                    npcSay += "|2|Cần 1 " + daXanhLam.template.name + "\n";
                    npcSay += "|7|Cần: " + player.combineNew.gemCombine + " ngọc\n";
                    npcSay += "|1|+1 Chỉ số ngẫu nhiên";
                    npcSay += "|2|Còn thiếu\n" + (player.combineNew.gemCombine - player.inventory.gem) + " ngọc xanh";
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
                }
            } else {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                        "Cần 1 Bông tai Porata cấp 2, X99 Mảnh hồn bông tai và 1 Đá xanh lam", "Đóng");
            }
        } else {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Cần 1 Bông tai Porata cấp 2, X99 Mảnh hồn bông tai và 1 Đá xanh lam", "Đóng");
        }
    }

    public static void nangChiSoBongTai(Player player) {
        try {
            if (player.inventory.gem < player.combineNew.gemCombine) {
                Service.gI().sendThongBao(player, "Bạn không đủ ngọc, còn thiếu " + Util.powerToString(player.combineNew.gemCombine - player.inventory.gem) + " ngọc nữa!");
                return;
            }
            player.inventory.gem -= player.combineNew.gemCombine;
            Service.gI().sendMoney(player);
            Item BongTai2 = null,
                    honBongTai = null,
                    daXanhLam = null;
            for (Item item : player.combineNew.itemsCombine) {
                if (item.template.id == 921) {
                    BongTai2 = item;
                }
                if (item.template.id == 934) {
                    honBongTai = item;
                }
                if (item.template.id == 935) {
                    daXanhLam = item;
                }
            }
            if (Util.isTrue(player.combineNew.ratioCombine, 100)) {
                byte[] option = {77, 80, 81, 103, 50, 94, 5};
                byte optionid = 0;
                byte param = 0;
                BongTai2.itemOptions.clear();
                optionid = option[Util.nextInt(0, 6)];
                param = (byte) Util.nextInt(5, 15);
                BongTai2.itemOptions.add(new Item.ItemOption(optionid, param));
                BongTai2.itemOptions.add(new Item.ItemOption(38, 0));
                BongTai2.itemOptions.add(new Item.ItemOption(72, 2));
                CombineService.gI().sendEffectSuccessCombine(player);
                CombineService.gI().baHatMit.npcChat(player, "Chúc mừng con nhé");
            } else {
                CombineService.gI().sendEffectFailCombine(player);

                String[] failMessages = {
                    "Ủa? Tưởng lần này lên chứ!",
                    "Vàng bạc mày to nhờ!",
                    "Vàng bạc mày nhiều nhờ!",
                    "Bảo rồi, hôm nay không hợp để nâng đồ đâu!",
                    "Lại tạch, thôi đừng khóc!",
                    "Gọi là thất bại nhẹ, làm vài lần nữa là lên thôi!",
                    "Còn nhiều đá mà, đập tiếp đi!",
                    "Hên xui mà, lần này không lên thì lần sau vậy!",
                    "Chơi đồ ảo, nhân phẩm thật!"
                };

                String msg = failMessages[Util.nextInt(failMessages.length)];
            }
            InventoryService.gI().subQuantityItemsBag(player, honBongTai, 99);
            InventoryService.gI().subQuantityItemsBag(player, daXanhLam, 1);
            InventoryService.gI().sendItemBags(player);
            CombineService.gI().reOpenItemCombine(player);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
