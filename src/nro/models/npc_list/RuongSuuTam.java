package nro.models.npc_list;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.npc.Npc;
import nro.models.player.Inventory;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.utils.Util;

/**
 *
 * @author By Mr Blue
 */
public class RuongSuuTam extends Npc {

    public RuongSuuTam(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            List<String> menu = new ArrayList<>(Arrays.asList(
                    "Mở rương",
                    "Nâng cấp\nrương",
                    "Lấy đồ\nra",
                    "Từ chối"));

            String[] menus = menu.toArray(new String[0]);

            createOtherMenu(player, ConstNpc.BASE_MENU,
                    "Vàng bạc châu báu gì cứ yên tâm giao hết cho tôi", menus);
        }

    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (this.mapId == 102) {
                if (player.idMark.isBaseMenu()) {
                    switch (select) {
                        case 0:
                            InventoryService.gI().sendItemSuuTam(player);
                            break;
                        case 1: {
                            int opened = player.inventory.suuTamOpenCount;
                            int goldRequire = 500_000_000 * (int) Math.pow(2, opened);
                            int gemRequire = 10_000 * (int) Math.pow(2, opened);

                            if (player.inventory.itemsSuuTam.size() >= Inventory.MAX_ITEMS_SUU_TAM) {
                                Service.gI().sendThongBaoOK(player, "Rương sưu tầm của bạn đã đạt tối đa");
                                break;
                            }

                            if (player.inventory.gold < goldRequire || player.inventory.gem < gemRequire) {
                                Service.gI().sendThongBaoOK(player, "Bạn cần " + Util.numberToMoney(goldRequire)
                                        + " vàng và " + Util.numberToMoney(gemRequire) + " ngọc để mở ô tiếp theo");
                                break;
                            }

                            player.inventory.gold -= goldRequire;
                            player.inventory.gem -= gemRequire;

                            player.inventory.itemsSuuTam.add(ItemService.gI().createItemNull());
                            player.inventory.suuTamOpenCount++;

                            Service.gI().sendThongBaoOK(player, "Bạn đã mở rộng thêm 1 ô rương sưu tầm");
                            break;
                        }
                        case 2: { // Lấy vật phẩm
                            if (player.inventory.itemsSuuTam.isEmpty()) {
                                Service.gI().sendThongBao(player, "Rương sưu tầm trống.");
                                return;
                            }

                            int index = InventoryService.gI().getIndexItemSuuTam(player);

                            if (index == -1 || index >= player.inventory.itemsSuuTam.size()) {
                                Service.gI().sendThongBao(player, "Vị trí vật phẩm không hợp lệ.");
                                return;
                            }

                            if (InventoryService.gI().getCountEmptyBag(player) == 0) {
                                Service.gI().sendThongBao(player, "Hành trang không đủ chỗ trống.");
                                return;
                            }

                            Item item = player.inventory.itemsSuuTam.remove(index);
                            InventoryService.gI().addItemBag(player, item);
                            InventoryService.gI().sendItemBags(player);
                            Service.gI().sendThongBao(player, "Bạn đã lấy vật phẩm thành công!");
                            break;
                        }
                    }
                }
            }
        }
    }
}
