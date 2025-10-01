package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.server.Manager;
import nro.models.services.Service;
import nro.models.services_func.Input;
import nro.models.shop.ShopService;

/**
 *
 * @author By Mr Blue
 *
 */
public class ChiChi extends Npc {

    public ChiChi(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            List<String> menu = new ArrayList<>(Arrays.asList(
                    "Đổi\nThưởng",
                    "Top\nBóng Master",
                    "Cửa hàng",
                    "Đóng"));

            String[] menus = menu.toArray(new String[0]);

            createOtherMenu(player, ConstNpc.BASE_MENU,
                    "Bạn muốn hỏi chi?", menus);
        }

    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            int soLuong = 0;
            if (this.mapId == 5) {
                if (player.idMark.isBaseMenu()) {
                    switch (select) {
                        case 0: // đổi thưởng
                            createOtherMenu(player, ConstNpc.VO_XEN_BO_HUNG, "Bạn có đổi quà gì nè\n1)99 vỏ xên lấy quà thường\n2)99 vỏ xên và 2 túi đựng lấy quà VIP",
                                    "Tùy chọn 1",
                                    "Tùy chọn 2",
                                    "Đóng");
                            break;
                        case 1:
                            createOtherMenu(player, ConstNpc.PHAO_BONG_VIP,
                                    "Sự kiện đua Top Bóng Master nhận quà khủng\n Kết thúc và trao giải sau (....)\nHạn chót nhận giải: (15 ngày nữa)\nĐến gặp ChiChi để nhận giải nhé\nChi tiết xem tại diễn đàn, Fanpage",
                                    "Top 100\nBóng Master",
                                    "Xem điểm",
                                    "Đóng");
                            break;
                        case 2:
                            ShopService.gI().opendShop(player, "SHOP_CHI_CHI", false);
                            break;
                    }
                } else if (player.idMark.getIndexMenu() == ConstNpc.PHAO_BONG_VIP) {
                    switch (select) {
                        case 0:
                            Service.gI().showListTop(player, Manager.Topsukien);
                            break;
                        case 1:
                            Service.gI().sendThongBao(player, "Bạn có " + player.point_sukien + " điểm Bóng Master.");
                            break;
                    }
                }
            }
        }
    }
}
