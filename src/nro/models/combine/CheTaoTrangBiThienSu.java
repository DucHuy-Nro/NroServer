package nro.models.combine;

import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.item.Item.ItemOption;
import java.util.ArrayList;
import java.util.Arrays;
import nro.models.player.Player;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.services.InventoryService;
import nro.models.utils.Util;

/**
 *
 * @author By Mr Blue
 */
public class CheTaoTrangBiThienSu {

    public static void showInfoCombine(Player player) {
        if (player.combineNew.itemsCombine.size() != 4) {
            Service.gI().sendThongBao(player, "Thiếu vật phẩm, vui lòng thêm vào");
            return;
        }

        if (player.combineNew.itemsCombine.stream().filter(i -> i.isNotNullItem() && i.isCongThucVip()).count() != 1) {
            Service.gI().sendThongBao(player, "Thiếu Công Thức Vip");
            return;
        }
        if (player.combineNew.itemsCombine.stream().filter(i -> i.isNotNullItem() && i.isManhTS() && i.quantity >= 999).count() != 1) {
            Service.gI().sendThongBao(player, "Thiếu Mảnh Thiên Sứ");
            return;
        }
        if (player.combineNew.itemsCombine.stream().filter(i -> i.isNotNullItem() && i.isDaNangCap1()).count() != 1) {
            Service.gI().sendThongBao(player, "Thiếu Đá Nâng Cấp");
            return;
        }
        if (player.combineNew.itemsCombine.stream().filter(i -> i.isNotNullItem() && i.isDaMayMan()).count() != 1) {
            Service.gI().sendThongBao(player, "Thiếu Đá May Mắn");
            return;
        }

        Item mTS = null, daNC = null, daMM = null, CtVip = null;
        for (Item item : player.combineNew.itemsCombine) {
            if (!item.isNotNullItem()) {
                continue;
            }
            if (item.isManhTS()) {
                mTS = item;
            } else if (item.isDaNangCap1()) {
                daNC = item;
            } else if (item.isDaMayMan()) {
                daMM = item;
            } else if (item.isCongThucVip()) {
                CtVip = item;
            }
        }

        if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
            Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
            return;
        }

        if (player.inventory.gold < 10_000_000) {
            Service.gI().sendThongBao(player, "Không đủ vàng để thực hiện");
            return;
        }

        player.inventory.gold -= 10_000_000;

        int tilemacdinh = 40;
        int tileLucky = 5;

        if (daNC != null) {
            tilemacdinh += (daNC.template.id - 1073);
        }
        if (daMM != null) {
            tileLucky += tileLucky * (daMM.template.id - 1078);
        }

        boolean success = Util.nextInt(0, 100) < tilemacdinh;
        if (success) {
            Item itemTS;
            try {
                Item itemCtVip = player.combineNew.itemsCombine.stream().filter(i -> i.isNotNullItem() && i.isCongThucVip()).findFirst().get();
                Item itemManh = player.combineNew.itemsCombine.stream().filter(i -> i.isNotNullItem() && i.isManhTS()).findFirst().get();

                short[][] itemIds = {
                    {1048, 1051, 1054, 1057, 1060},
                    {1049, 1052, 1055, 1058, 1061},
                    {1050, 1053, 1056, 1059, 1062}
                };

                int genderIndex = itemCtVip.template.gender > 2 ? player.gender : itemCtVip.template.gender;
                int manhTypeIndex = itemManh.typeIdManh();

                itemTS = ItemService.gI().DoThienSu(itemIds[genderIndex][manhTypeIndex], itemCtVip.template.gender);

                int randomCheck = Util.nextInt(0, 50);
                if (randomCheck <= tileLucky) {
                    if (randomCheck >= tileLucky - 3) {
                        tileLucky = 3;
                    } else if (randomCheck >= tileLucky - 10) {
                        tileLucky = 2;
                    } else {
                        tileLucky = 1;
                    }

                    ArrayList<ItemOption> bonusOptions = new ArrayList<>();
                    ArrayList<Integer> listOptionBonus = new ArrayList<>(Arrays.asList(
                            44, 45, 46, 197, 198, 199, 200, 201, 202, 203, 204
                    ));

                    for (int j = 0; j < tileLucky; j++) {
                        int index = Util.nextInt(0, listOptionBonus.size());
                        int optionId = listOptionBonus.get(index);
                        int value = Util.nextInt(1, 3);
                        bonusOptions.add(new ItemOption(optionId, value));
                        listOptionBonus.remove(index);
                    }
                    itemTS.itemOptions.add(new ItemOption(41, bonusOptions.size()));

                    itemTS.itemOptions.addAll(bonusOptions);
                }

                InventoryService.gI().addItemBag(player, itemTS);
                CombineService.gI().sendEffectSuccessCombine(player);
            } catch (Exception e) {
                e.printStackTrace();
                Service.gI().sendThongBao(player, "Có lỗi xảy ra, hãy thử lại");
            }
        } else {
            CombineService.gI().sendEffectFailCombine(player);
        }

        if (CtVip != null) {
            InventoryService.gI().subQuantityItemsBag(player, CtVip, 1);
        }
        if (daNC != null) {
            InventoryService.gI().subQuantityItemsBag(player, daNC, 1);
        }
        if (daMM != null) {
            InventoryService.gI().subQuantityItemsBag(player, daMM, 1);
        }
        if (mTS != null) {
            InventoryService.gI().subQuantityItemsBag(player, mTS, 999);
        }

        InventoryService.gI().sendItemBags(player);
        Service.gI().sendMoney(player);
        CombineService.gI().reOpenItemCombine(player);
    }

    public static void CheTaoTS(Player player) {
        if (player.combineNew.itemsCombine.size() != 4) {
            Service.gI().sendThongBao(player, "Thiếu đồ");
            return;
        }
        if (player.inventory.gold < 500_000_000) {
            Service.gI().sendThongBao(player, "Không đủ vàng để thực hiện");
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(player) < 1) {
            Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
            return;
        }
        Item itemTL = player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isDHD()).findFirst().get();
        Item itemManh = player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isManhTS() && item.quantity >= 5).findFirst().get();

        player.inventory.gold -= 500_000_000;
        CombineService.gI().sendEffectSuccessCombine(player);
        short[][] itemIds = {{1048, 1051, 1054, 1057, 1060}, {1049, 1052, 1055, 1058, 1061}, {1050, 1053, 1056, 1059, 1062}}; // thứ tự td - 0,nm - 1, xd - 2

        Item itemTS = ItemService.gI().DoThienSu(itemIds[itemTL.template.gender > 2 ? player.gender : itemTL.template.gender][itemManh.typeIdManh()], itemTL.template.gender);
        InventoryService.gI().addItemBag(player, itemTS);

        InventoryService.gI().subQuantityItemsBag(player, itemTL, 1);
        InventoryService.gI().subQuantityItemsBag(player, itemManh, 99);
        InventoryService.gI().sendItemBags(player);
        Service.gI().sendMoney(player);
        Service.gI().sendThongBao(player, "Bạn đã nhận được " + itemTS.template.name);
        player.combineNew.itemsCombine.clear();
        CombineService.gI().reOpenItemCombine(player);
    }

}
