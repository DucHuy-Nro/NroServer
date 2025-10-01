package nro.models.npc_list;

import java.time.LocalDate;
import java.time.LocalDateTime;
import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.map.phoban.BanDoKhoBau;
import nro.models.services_dungeon.TreasureUnderSeaService;
import nro.models.npc.Npc;
import static nro.models.npc.NpcFactory.PLAYERID_OBJECT;
import nro.models.player.Player;
import nro.models.services.ItemService;
import nro.models.services.InventoryService;
import nro.models.map.service.NpcService;
import nro.models.services.RewardService;
import nro.models.services.Service;
import nro.models.shop.ShopService;
import nro.models.services.TaskService;
import nro.models.map.service.ChangeMapService;
import nro.models.services_func.Input;
import nro.models.services.PlayerService;
import nro.models.skill.Skill;
import nro.models.utils.Logger;
import nro.models.utils.SkillUtil;
import nro.models.utils.TimeUtil;
import nro.models.utils.Util;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuyLaoKame extends Npc {

    private static final Map<Integer, Long> lastClickTimes = new HashMap<>();
    private static final long COOLDOWN_TIME = 60 * 1000;

    private static class RewardItem {

        int itemId;
        int quantity;

        public RewardItem(int itemId, int quantity) {
            this.itemId = itemId;
            this.quantity = quantity;
        }
    }

    private static class KOLQuestData {

        int questType;
        int itemId;
        int requiredQuantity;

        List<RewardItem> rewards;
        String description;

        public KOLQuestData(int questType, int itemId, int requiredQuantity, List<RewardItem> rewards, String description) {
            this.questType = questType;
            this.itemId = itemId;
            this.requiredQuantity = requiredQuantity;
            this.rewards = rewards;
            this.description = description;
        }
    }

    private static final Map<Integer, KOLQuestData> KOL_QUESTS = new HashMap<>();

    static {
        KOL_QUESTS.put(1, new KOLQuestData(ConstNpc.KOL_QUEST_TYPE_ITEM_COLLECTION, 1778, 100, Arrays.asList(new RewardItem(1821, 5)), "Nhiệm vụ 1:\nThu thập 100 cuốn chả giò (Quái doanh trại)"));
        KOL_QUESTS.put(2, new KOLQuestData(ConstNpc.KOL_QUEST_TYPE_ITEM_COLLECTION, 1824, 10, Arrays.asList(new RewardItem(1592, 5), new RewardItem(1757, 5)), "Nhiệm vụ 2:\nThu thập 10 chai cuke 2 lít (Boss doanh trại)"));
        KOL_QUESTS.put(3, new KOLQuestData(ConstNpc.KOL_QUEST_TYPE_DUNGEON_COMPLETION, -1, 20, Arrays.asList(new RewardItem(1360, 1)), "Nhiệm vụ 3:\nHoàn thành phó bản Destron Gas cấp 70 trên 20 lần"));
        KOL_QUESTS.put(4, new KOLQuestData(ConstNpc.KOL_QUEST_TYPE_PVP_WINS, -1, 10, Arrays.asList(new RewardItem(1654, 1)), "Nhiệm vụ 4:\nĐánh bại 10 người trong đại hội võ thuật"));
        KOL_QUESTS.put(5, new KOLQuestData(ConstNpc.KOL_QUEST_TYPE_DAILY_QUEST_COMPLETION, -1, 30, Arrays.asList(new RewardItem(1822, 10)), "Nhiệm vụ 5:\nHoàn thành 30 nhiệm vụ siêu khó hàng ngày"));
        KOL_QUESTS.put(6, new KOLQuestData(ConstNpc.KOL_QUEST_TYPE_BOSS_DEFEAT_PARTICIPATION, -1, 5, Arrays.asList(new RewardItem(1797, 1), new RewardItem(1592, 5), new RewardItem(1757, 5)), "Nhiệm vụ 6:\nTham gia hạ gục boss baby 5 lần"));
        KOL_QUESTS.put(7, new KOLQuestData(ConstNpc.KOL_QUEST_TYPE_MONSTER_KILL_COUNT, -1, 100000, Arrays.asList(new RewardItem(1592, 10), new RewardItem(20, 7), new RewardItem(1757, 5)), "Nhiệm vụ 7:\nHạ 100.000 quái (dùng tự động luyện tập)"));
    }

    private static final Map<Integer, KOLQuestData> KOL_VIP_QUESTS = new HashMap<>();

    static {
        KOL_VIP_QUESTS.put(1, new KOLQuestData(ConstNpc.KOL_QUEST_TYPE_ITEM_COLLECTION, 1778, 100, Arrays.asList(new RewardItem(1821, 10)), "Nhiệm vụ 1:\nThu thập 100 cuốn chả giò (Quái doanh trại)"));
        KOL_VIP_QUESTS.put(2, new KOLQuestData(ConstNpc.KOL_QUEST_TYPE_ITEM_COLLECTION, 1824, 10, Arrays.asList(new RewardItem(1592, 10), new RewardItem(1757, 10)), "Nhiệm vụ 2:\nThu thập 10 chai cuke 2 lít (Boss doanh trại)"));
        KOL_VIP_QUESTS.put(3, new KOLQuestData(ConstNpc.KOL_QUEST_TYPE_DUNGEON_COMPLETION, -1, 20, Arrays.asList(new RewardItem(1360, 1)), "Nhiệm vụ 3:\nHoàn thành phó bản Destron Gas cấp 70 trên 20 lần"));
        KOL_VIP_QUESTS.put(4, new KOLQuestData(ConstNpc.KOL_QUEST_TYPE_PVP_WINS, -1, 10, Arrays.asList(new RewardItem(1654, 1)), "Nhiệm vụ 4:\nĐánh bại 10 người trong đại hội võ thuật"));
        KOL_VIP_QUESTS.put(5, new KOLQuestData(ConstNpc.KOL_QUEST_TYPE_DAILY_QUEST_COMPLETION, -1, 30, Arrays.asList(new RewardItem(1822, 20)), "Nhiệm vụ 5:\nHoàn thành 30 nhiệm vụ siêu khó hàng ngày"));
        KOL_VIP_QUESTS.put(6, new KOLQuestData(ConstNpc.KOL_QUEST_TYPE_BOSS_DEFEAT_PARTICIPATION, -1, 5, Arrays.asList(new RewardItem(1797, 1), new RewardItem(1592, 10), new RewardItem(1757, 10)), "Nhiệm vụ 6:\nTham gia hạ gục boss baby 5 lần"));
        KOL_VIP_QUESTS.put(7, new KOLQuestData(ConstNpc.KOL_QUEST_TYPE_MONSTER_KILL_COUNT, -1, 100000, Arrays.asList(new RewardItem(1592, 20), new RewardItem(1757, 10)), "Nhiệm vụ 7:\nHạ 100.000 quái (dùng tự động luyện tập)"));
    }

    public QuyLaoKame(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        Item ruacon = InventoryService.gI().findItemBag(player, 874);
        if (canOpenNpc(player)) {
            ArrayList<String> menuOptions = new ArrayList<>();

            if (!player.canReward) {
                menuOptions.add("Nói\nchuyện");
                menuOptions.add("Đổi điểm\nsự kiện\n[" + player.event.getEventPoint() + "]");
                menuOptions.add("Nhiệm vụ\nKOL");
                menuOptions.add("Kiểm tra lại\nbang hội");

                if (ruacon != null && ruacon.quantity >= 1) {
                    menuOptions.add("Giao\nRùa con");
                }
            } else {
                menuOptions.add("Giao\nLân con");
            }

            player.currentNpcMenuOptions = menuOptions;

            String[] menusArray = menuOptions.toArray(String[]::new);
            if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                this.createOtherMenu(player, ConstNpc.BASE_MENU, "Con muốn hỏi gì nào?", menusArray);
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) {
            return;
        }

        if (player.canReward) {
            RewardService.gI().rewardLancon(player);
            return;
        }

        switch (player.idMark.getIndexMenu()) {
            case ConstNpc.BASE_MENU:
                handleBaseMenu(player, select);
                break;
            case 12:
                handleMenu12(player, select);
                break;
            case 0:
                handleMenu0(player, select);
                break;
            case 4:
                handleMenu4(player, select);
                break;
            case ConstNpc.MENU_OPENED_DBKB:
                handleMenuOpenedDBKB(player, select);
                break;
            case ConstNpc.MENU_OPEN_DBKB:
                handleMenuOpenDBKB(player, select);
                break;
            case ConstNpc.MENU_ACCEPT_GO_TO_BDKB:
                handleMenuAcceptGoToBDKB(player, select);
                break;
            case ConstNpc.KOL_QUEST_MENU:
                handleKOLQuestRewardConfirm(player, select, false);
                break;
            case ConstNpc.KOL_VIP_REWARD_MENU:
                handleKOLQuestRewardConfirm(player, select, true);
                break;
            case 10:
                handleSubMenu(player, 10, select);
                break;
            case 11:
                handleSubMenu1(player, 11, select);
                break;
            case 15:
                handleSubMenuKOL(player, 15, select);
                break;
        }
    }

    private void handleBaseMenu(Player player, int select) {
        List<String> currentBaseMenuOptions = player.currentNpcMenuOptions;

        if (currentBaseMenuOptions == null) {
            System.out.println("DEBUG: currentBaseMenuOptions is NULL. This is the root cause!");
            Service.gI().sendThongBao(player, "Lỗi hệ thống menu. Vui lòng thử lại.");
            return;
        }

        if (select < 0 || select >= currentBaseMenuOptions.size()) {
            Service.gI().sendThongBao(player, "Lựa chọn không hợp lệ. Vui lòng thử lại.");
            player.currentNpcMenuOptions = null;
            return;
        }

        String selectedOptionText = currentBaseMenuOptions.get(select);

        if (selectedOptionText.equals("Nói\nchuyện")) {
            handleTalk(player);
        } else if (selectedOptionText.equals("Đổi điểm\nsự kiện\n[" + player.event.getEventPoint() + "]")) {
            ShopService.gI().opendShop(player, "SHOP_DOI_DIEM", false);
        } else if (selectedOptionText.toLowerCase().equals("nhiệm vụ\nkol")) {
            createOtherMenu(player, 15, "Nhiệm vụ KOL đã diễn ra.\n"
                    + "Mua thêm vé VIP để nhận nhiều phần thưởng hơn",
                    "Nhận quà\nKOL", "Nhận quà\nKOL VIP", "Đóng");
        } else if (selectedOptionText.equals("Nhận\nCá nóc")) {
            nhanCanoc(player);
        } else if (selectedOptionText.equals("Đổi Cá\nlấy thưởng")) {
            createOtherMenu(player, 10, "Ngươi có muốn đổi ?\n"
                    + "|3|Tùy chọn 1: Đổi 1 Cá diêu hồng và 10 ngọc lấy 1 Xô cá xanh\n"
                    + "|3|Tùy chọn 2: Đổi 1 Cá diêu hồng và 5 Tr vàng lấy 1 Xô cá vàng",
                    "Tùy chọn 1", "Tùy chọn 2", "Đóng");
        } else if (selectedOptionText.equals("Dổi quà\nSự kiện")) {
            createOtherMenu(player, 11, "Con hãy chọn món quà sau đây\n"
                    + "1) Hoa hồng vàng hoặc đỏ hạn sử dụng\n"
                    + "2) Cá chà bá hoặc cây nắp ấm hạn sử dụng\n"
                    + "3) Phượng Hoàng Lửa hoặc Rùa Phun Lửa hạn sử dụng\n"
                    + "4) Bong bóng heo, bóng Vịt Vàng , Pic, Poc, King Kong Hè hạn sử dụng\n"
                    + "5) Ván bay té nước hạn sử dụng hoặc Vĩnh viễn\n"
                    + "6) Cờ ngọc rồng 7 sao hạn sử dụng hoặc Vĩnh viễn",
                    "Tùy chọn 1\nx99 Vỏ ốc", "Tùy chọn 2\nx99 Vỏ sò", "Tùy cọn 3\nx99 Con cua", "Tùy chọn 4\nx99 Sao biển", "Tùy chọn 5\nx99 Vỏ ốc,Vỏ sò,Con cua,Sao biển", "Tùy chọn 6\nx99 Vỏ ốc,Vỏ sò,Con cua,Sao biển");
        } else if (selectedOptionText.equals("Kiểm tra lại\nbang hội")) {
            long now = System.currentTimeMillis();
            int cooldown = 60;
            long timeLeft = (player.lastClickTimeOption7 + cooldown * 1000 - now) / 1000;

            if (timeLeft > 0) {
                Service.gI().sendThongBao(player, "Vui lòng chờ " + timeLeft + " giây nữa");
                return;
            }
            player.lastClickTimeOption7 = now;
            Service.gI().sendThongBao(player, "Xong");
        } else if (selectedOptionText.equals("Giao\nRùa con")) {
            handleTradeRuacon(player);
        } else if (selectedOptionText.equals("Giao\nLân con")) {
            RewardService.gI().rewardLancon(player);
        } else {
            Service.gI().sendThongBao(player, "Đã xảy ra lỗi không xác định. Vui lòng thử lại.");
        }

        player.currentNpcMenuOptions = null;
    }

    private void handleSubMenuKOL(Player player, int menuId, int select) {
        switch (menuId) {
            case 15: // menu KOL
                if (select == 0) {
                    handleKOLQuest(player, false, select);
                }
                if (select == 1) {
                    handleKOLQuest(player, false, select);
                }
                break;
        }
    }

    private void handleSubMenu(Player player, int menuId, int select) {
        switch (menuId) {
            case 10: // menu đổi cá
                if (select == 0 || select == 1) {
                    doiCa(player, select);
                }
                break;
        }
    }

    private void handleSubMenu1(Player player, int menuId, int select) {
        switch (menuId) {
            case 11: // menu đổi quà sự kiện
                if (select == 0 || select == 1 || select == 2 || select == 3 || select == 4 || select == 5) {
                    doiQuaSuKien(player, select);
                }
                break;
        }
    }

    private void doiCa(Player player, int select) {
        Item caDieuHong = InventoryService.gI().findItemBag(player, 1004);
        if (caDieuHong == null || caDieuHong.quantity < 1) {
            Service.gI().sendThongBao(player, "Bạn không có Cá diêu hồng!");
            return;
        }

        if (select == 0) {
            if (player.inventory.gem < 10) {
                Service.gI().sendThongBao(player, "Bạn không đủ ngọc!");
                return;
            }
            player.inventory.gem -= 10;
            InventoryService.gI().subQuantityItemsBag(player, caDieuHong, 1);
            InventoryService.gI().addItemBag(player, ItemService.gI().createNewItem((short) 1005)); // Xô cá xanh
            Service.gI().sendThongBao(player, "Bạn nhận được 1 Xô cá xanh!");
        } else if (select == 1) {
            if (player.inventory.gold < 5_000_000) {
                Service.gI().sendThongBao(player, "Bạn không đủ vàng!");
                return;
            }
            player.inventory.gold -= 5_000_000;
            InventoryService.gI().subQuantityItemsBag(player, caDieuHong, 1);
            InventoryService.gI().addItemBag(player, ItemService.gI().createNewItem((short) 1006)); // Xô cá vàng
            Service.gI().sendThongBao(player, "Bạn nhận được 1 Xô cá vàng!");
        }

        PlayerService.gI().sendInfoHpMpMoney(player);
        InventoryService.gI().sendItemBags(player);
    }

    private void doiQuaSuKien(Player player, int select) {
        final int VO_OC = 695;
        final int VO_SO = 696;
        final int CON_CUA = 697;
        final int SAO_BIEN = 698;

        switch (select) {
            case 0: { // Hoa hồng vàng hoặc đỏ
                Item itemCheck = InventoryService.gI().findItemBag(player, VO_OC);
                if (itemCheck == null || itemCheck.quantity < 99) {
                    Service.gI().sendThongBao(player, "Bạn không đủ 99 Vỏ ốc");
                    return;
                }

                InventoryService.gI().subQuantityItemsBag(player, itemCheck, 99);
                short itemId = (short) ((Util.nextInt(0, 1) == 0) ? 954 : 955);
                Item item = ItemService.gI().createNewItem(itemId);

                // Gán option theo từng loại hoa
                switch (itemId) {
                    case 954: // Hoa hồng đỏ
                        item.itemOptions.add(new Item.ItemOption(50, 15));
                        item.itemOptions.add(new Item.ItemOption(77, 12));
                        item.itemOptions.add(new Item.ItemOption(103, 12));
                        item.itemOptions.add(new Item.ItemOption(93, 7));
                        break;
                    case 955: // Hoa hồng vàng
                        item.itemOptions.add(new Item.ItemOption(50, 15));
                        item.itemOptions.add(new Item.ItemOption(77, 12));
                        item.itemOptions.add(new Item.ItemOption(103, 12));
                        item.itemOptions.add(new Item.ItemOption(93, 7));
                        break;
                }

                InventoryService.gI().addItemBag(player, item);
                Service.gI().sendThongBao(player, "Bạn nhận được " + item.template.name + "!");
                break;
            }

            case 1: { // Cá chà bá hoặc Cây nắp ấm
                Item itemCheck = InventoryService.gI().findItemBag(player, VO_SO);
                if (itemCheck == null || itemCheck.quantity < 99) {
                    Service.gI().sendThongBao(player, "Bạn không đủ 99 Vỏ sò");
                    return;
                }

                InventoryService.gI().subQuantityItemsBag(player, itemCheck, 99);
                short itemId = (short) ((Util.nextInt(0, 1) == 0) ? 1113 : 1112);
                Item item = ItemService.gI().createNewItem(itemId);

                switch (itemId) {
                    case 1113: // Cá chà bá
                        item.itemOptions.add(new Item.ItemOption(50, 12));
                        item.itemOptions.add(new Item.ItemOption(77, 11));
                        item.itemOptions.add(new Item.ItemOption(103, 11));
                        item.itemOptions.add(new Item.ItemOption(93, 7));
                        break;
                    case 1112: // Cây nắp ấm
                        item.itemOptions.add(new Item.ItemOption(50, 12));
                        item.itemOptions.add(new Item.ItemOption(77, 11));
                        item.itemOptions.add(new Item.ItemOption(103, 11));
                        item.itemOptions.add(new Item.ItemOption(93, 7));
                        break;
                }

                InventoryService.gI().addItemBag(player, item);
                Service.gI().sendThongBao(player, "Bạn nhận được " + item.template.name + "!");
                break;
            }
            case 2: { // Phượng hoàng hoặc Rùa phun lửa
                Item itemCheck = InventoryService.gI().findItemBag(player, CON_CUA);
                if (itemCheck == null || itemCheck.quantity < 99) {
                    Service.gI().sendThongBao(player, "Bạn không đủ 99 Con cua");
                    return;
                }

                InventoryService.gI().subQuantityItemsBag(player, itemCheck, 99);

                short itemId = (short) ((Util.nextInt(0, 1) == 0) ? 1144 : 1363);
                Item item = ItemService.gI().createNewItem(itemId);

                switch (itemId) {
                    case 1144: // Phượng hoàng
                        item.itemOptions.add(new Item.ItemOption(50, 6));
                        item.itemOptions.add(new Item.ItemOption(77, 6));
                        item.itemOptions.add(new Item.ItemOption(85, 0));
                        item.itemOptions.add(new Item.ItemOption(93, 7));
                        break;
                    case 1363: // Rùa phun lửa
                        item.itemOptions.add(new Item.ItemOption(50, 7));
                        item.itemOptions.add(new Item.ItemOption(77, 7));
                        item.itemOptions.add(new Item.ItemOption(85, 0));
                        item.itemOptions.add(new Item.ItemOption(93, 7));
                        break;
                }

                InventoryService.gI().addItemBag(player, item);
                Service.gI().sendThongBao(player, "Bạn nhận được " + item.template.name + "!");
                break;
            }
            case 3: { // Bong bóng
                Item itemCheck = InventoryService.gI().findItemBag(player, SAO_BIEN);
                if (itemCheck == null || itemCheck.quantity < 99) {
                    Service.gI().sendThongBao(player, "Bạn không đủ 99 Sao biển");
                    return;
                }

                InventoryService.gI().subQuantityItemsBag(player, itemCheck, 99);

                int[] ids = {1142, 1197, 1234, 1235, 1236};
                short itemId = (short) ids[Util.nextInt(0, ids.length - 1)];
                Item item = ItemService.gI().createNewItem(itemId);

                switch (itemId) {
                    case 1142: // Bong bóng Hồng
                        item.itemOptions.add(new Item.ItemOption(50, 8));
                        item.itemOptions.add(new Item.ItemOption(77, 8));
                        item.itemOptions.add(new Item.ItemOption(103, 8));
                        item.itemOptions.add(new Item.ItemOption(93, 7));
                        break;
                    case 1197: // Bong bóng Vàng
                        item.itemOptions.add(new Item.ItemOption(50, 8));
                        item.itemOptions.add(new Item.ItemOption(77, 8));
                        item.itemOptions.add(new Item.ItemOption(103, 8));
                        item.itemOptions.add(new Item.ItemOption(93, 7));
                        break;
                    case 1234: // pic
                        item.itemOptions.add(new Item.ItemOption(50, 20));
                        item.itemOptions.add(new Item.ItemOption(77, 20));
                        item.itemOptions.add(new Item.ItemOption(103, 20));
                        item.itemOptions.add(new Item.ItemOption(93, 7));
                        break;
                    case 1235: // poc
                        item.itemOptions.add(new Item.ItemOption(50, 21));
                        item.itemOptions.add(new Item.ItemOption(77, 21));
                        item.itemOptions.add(new Item.ItemOption(103, 21));
                        item.itemOptions.add(new Item.ItemOption(93, 7));
                        break;
                    case 1236: // king kong
                        item.itemOptions.add(new Item.ItemOption(50, 22));
                        item.itemOptions.add(new Item.ItemOption(77, 22));
                        item.itemOptions.add(new Item.ItemOption(103, 22));
                        item.itemOptions.add(new Item.ItemOption(93, 7));
                        break;
                }

                InventoryService.gI().addItemBag(player, item);
                Service.gI().sendThongBao(player, "Bạn nhận được " + item.template.name + "!");
                break;
            }

            case 4: { // Ván bay
                Item oc = InventoryService.gI().findItemBag(player, VO_OC);
                Item so = InventoryService.gI().findItemBag(player, VO_SO);
                Item cua = InventoryService.gI().findItemBag(player, CON_CUA);
                Item sao = InventoryService.gI().findItemBag(player, SAO_BIEN);

                if (oc == null || oc.quantity < 99
                        || so == null || so.quantity < 99
                        || cua == null || cua.quantity < 99
                        || sao == null || sao.quantity < 99) {
                    Service.gI().sendThongBao(player, "Bạn không đủ 4 loại nguyên liệu (99 mỗi loại)");
                    return;
                }

                InventoryService.gI().subQuantityItemsBag(player, oc, 99);
                InventoryService.gI().subQuantityItemsBag(player, so, 99);
                InventoryService.gI().subQuantityItemsBag(player, cua, 99);
                InventoryService.gI().subQuantityItemsBag(player, sao, 99);

                Item item = ItemService.gI().createNewItem((short) 1563); // Ván bay
                item.itemOptions.add(new Item.ItemOption(50, 8));
                item.itemOptions.add(new Item.ItemOption(77, 8));
                item.itemOptions.add(new Item.ItemOption(85, 0));
                item.itemOptions.add(new Item.ItemOption(231, 0));
                InventoryService.gI().addItemBag(player, item);

                Service.gI().sendThongBao(player, "Bạn nhận được Ván bay!");
                break;
            }
            case 5: { // Cờ ngọc rồng 7 sao
                Item oc = InventoryService.gI().findItemBag(player, VO_OC);
                Item so = InventoryService.gI().findItemBag(player, VO_SO);
                Item cua = InventoryService.gI().findItemBag(player, CON_CUA);
                Item sao = InventoryService.gI().findItemBag(player, SAO_BIEN);

                if (oc == null || oc.quantity < 99
                        || so == null || so.quantity < 99
                        || cua == null || cua.quantity < 99
                        || sao == null || sao.quantity < 99) {
                    Service.gI().sendThongBao(player, "Bạn không đủ 4 loại nguyên liệu");
                    return;
                }

                InventoryService.gI().subQuantityItemsBag(player, oc, 99);
                InventoryService.gI().subQuantityItemsBag(player, so, 99);
                InventoryService.gI().subQuantityItemsBag(player, cua, 99);
                InventoryService.gI().subQuantityItemsBag(player, sao, 99);

                Item item = ItemService.gI().createNewItem((short) 1585); // Cờ ngọc rồng 7 sao
                item.itemOptions.add(new Item.ItemOption(50, 12));
                item.itemOptions.add(new Item.ItemOption(77, 12));
                item.itemOptions.add(new Item.ItemOption(103, 12));
                item.itemOptions.add(new Item.ItemOption(14, 5));
                item.itemOptions.add(new Item.ItemOption(30, 0));
                item.itemOptions.add(new Item.ItemOption(231, 0));
                InventoryService.gI().addItemBag(player, item);

                Service.gI().sendThongBao(player, "Bạn nhận được Cờ ngọc rồng 7 sao!");
                break;
            }

        }
        InventoryService.gI().sendItemBags(player);
    }

    private void handleTalk(Player player) {
        if (player.LearnSkill.Time != -1 && player.LearnSkill.Time <= System.currentTimeMillis()) {
            player.LearnSkill.Time = -1;
            try {
                var curSkill = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(player.LearnSkill.ItemTemplateSkillId), SkillUtil.getSkillByItemID(player, player.LearnSkill.ItemTemplateSkillId).point);
                player.BoughtSkill.add((int) player.LearnSkill.ItemTemplateSkillId);
                SkillUtil.setSkill(player, curSkill);
                var msg = Service.gI().messageSubCommand((byte) 62);
                msg.writer().writeShort(curSkill.skillId);
                player.sendMessage(msg);
                msg.cleanup();
                PlayerService.gI().sendInfoHpMpMoney(player);
            } catch (Exception e) {
                Logger.log(e.toString());
            }
        }

        ArrayList<String> menu = new ArrayList<>();
        menu.add("Nhiệm vụ");
        menu.add("Học\nKỹ năng");
        if (player.clan != null) {
            menu.add("Về khu\nvực bang");
            menu.add("Kho báu\ndưới biển");
            if (player.clan.isLeader(player)) {
                menu.add("Giải tán\nBang hội");
            }
        }
        this.createOtherMenu(player, 0, "Chào con, ta rất vui khi gặp con\nCon muốn làm gì nào ?", menu.toArray(new String[0]));
    }

    private void handleTradeRuacon(Player player) {
        Item ruacon = InventoryService.gI().findItemBag(player, 874);
        if (ruacon != null && ruacon.quantity >= 1) {
            this.createOtherMenu(player, 1, "Cảm ơn cậu đã cứu con rùa của ta\nĐể cảm ơn ta sẽ tặng cậu món quà.", "Nhận quà", "Đóng");
        }
    }

    private void learnSkill(Player player) {
        try {
            String[] subName = ItemService.gI().getTemplate(player.LearnSkill.ItemTemplateSkillId).name.split("");
            byte level = Byte.parseByte(subName[subName.length - 1]);
            Skill curSkill = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(player.LearnSkill.ItemTemplateSkillId), level);
            player.BoughtSkill.add((int) player.LearnSkill.ItemTemplateSkillId);
            SkillUtil.setSkill(player, curSkill);
            var msg = Service.gI().messageSubCommand((byte) 62);
            msg.writer().writeShort(curSkill.skillId);
            player.sendMessage(msg);
            msg.cleanup();
            PlayerService.gI().sendInfoHpMpMoney(player);
        } catch (Exception e) {
            Logger.log(e.toString());
        }
    }

    private void handleMenu12(Player player, int select) {
        switch (select) {
            case 0 -> {
                var time = player.LearnSkill.Time - System.currentTimeMillis();
                var ngoc = 5;
                if (time / 600_000 >= 2) {
                    ngoc += time / 600_000;
                }
                if (player.inventory.gem < ngoc) {
                    Service.gI().sendThongBao(player, "Bạn không có đủ ngọc");
                    return;
                }
                player.inventory.subGem(ngoc);
                player.LearnSkill.Time = -1;
                learnSkill(player);
            }
            case 1 ->
                createOtherMenu(player, 13, "Con có muốn huỷ học kỹ năng này và nhận lại 50% số tiềm năng không ?", "Ok", "Đóng");
        }
    }

    private void handleMenu0(Player player, int select) {
        switch (select) {
            case 0: // Nhiệm vụ
                NpcService.gI().createTutorial(player, tempId, avartar, player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).name);
                break;
            case 1: // Học Kỹ năng
                if (player.LearnSkill.Time != -1) {
                    handleSkillLearning(player);
                } else {
                    ShopService.gI().opendShop(player, "QUY_LAO", false);
                }
                break;
            case 2: // Về khu vực bang
                handleClanMapChange(player);
                break;
            default:
                if (player.clan != null) {
                    if (select == 3) {
                        handleTreasureMap(player);
                    } else if (player.clan.isLeader(player) && select == 4) {
                        handleClanDissolution(player);
                    }
                }
                break;
        }
    }

    private void handleSkillLearning(Player player) {
        if (player.LearnSkill.Time != -1) {
            var ngoc = 5;
            var time = player.LearnSkill.Time - System.currentTimeMillis();
            if (time / 600_000 >= 2) {
                ngoc += time / 600_000;
            }
            String[] subName = ItemService.gI().getTemplate(player.LearnSkill.ItemTemplateSkillId).name.split("");
            byte level = Byte.parseByte(subName[subName.length - 1]);
            createOtherMenu(player, 12,
                    "Con đang học kỹ năng\n" + SkillUtil.findSkillTemplate(SkillUtil.getTempSkillSkillByItemID(player.LearnSkill.ItemTemplateSkillId)).name
                    + " cấp " + level + "\nThời gian còn lại " + TimeUtil.getTime(time),
                    "Học Cấp tốc " + ngoc + " ngọc", "Huỷ", "Bỏ qua");
        } else {
            ShopService.gI().opendShop(player, "QUY_LAO", false);
        }
    }

    private void handleClanMapChange(Player player) {
        if (player.clan != null) {
            ChangeMapService.gI().changeMapNonSpaceship(player, 153, Util.nextInt(100, 200), 432);
        } else {
            Service.gI().sendThongBao(player, "Bạn cần có bang hội để thực hiện chức năng này.");
        }
    }

    private void handleClanDissolution(Player player) {
        if (player.clan != null && player.clan.isLeader(player)) {
            createOtherMenu(player, 4, "Con có chắc muốn giải tán bang hội không?", "Đồng ý", "Từ chối");
        }
    }

    private void handleTreasureMap(Player player) {
        if (player.clan != null && player.clan.BanDoKhoBau != null) {
            this.createOtherMenu(player, ConstNpc.MENU_OPENED_DBKB, "Bang hội con đang ở hang kho báu cấp " + player.clan.BanDoKhoBau.level + "\ncon có muốn đi cùng họ không?", "Top\nBang hội", "Thành tích\nBang", "Đồng ý", "Từ chối");
        } else {
            this.createOtherMenu(player, ConstNpc.MENU_OPEN_DBKB, "Đây là bản đồ kho báu hải tặc tí hon\nCác con cứ yên tâm lên đường\nỞ đây có ta lo\nNhớ chọn cấp độ vừa sức mình nhé", "Top\nBang hội", "Thành tích\nBang", "Chọn\ncấp độ", "Từ chối");
        }
    }

    private void handleMenu4(Player player, int select) {
        if (player.clan != null && player.clan.isLeader(player) && select == 0) {
            Input.gI().createFormGiaiTanBangHoi(player);
        }
    }

    private void handleMenuOpenedDBKB(Player player, int select) {
        if (select == 0) {
            Service.gI().showTopClanBDKB(player);
        } else if (select == 1) {
            Service.gI().showMyTopClanBDKB(player);
        } else if (select == 2) {
            if (player.isAdmin() || player.nPoint.power >= BanDoKhoBau.POWER_CAN_GO_TO_DBKB) {
                if (player.clan.BanDoKhoBau != null) {
                    ChangeMapService.gI().goToDBKB(player);
                } else {
                    this.npcChat(player, "Bang hội của con hiện không có hang kho báu đang hoạt động.");
                }
            } else {
                this.npcChat(player, "Yêu cầu sức mạnh lớn hơn " + Util.numberToMoney(BanDoKhoBau.POWER_CAN_GO_TO_DBKB));
            }
        }
    }

    private void handleMenuOpenDBKB(Player player, int select) {
        if (select == 0) {
            Service.gI().showTopClanBDKB(player);
        } else if (select == 1) {
            Service.gI().showTopClanBDKB(player);
        } else if (select == 2) {
            if (player.clan == null) {
                Service.gI().sendThongBao(player, "Hãy vào bang hội trước");
                return;
            }
            if (player.isAdmin() || player.nPoint.power >= BanDoKhoBau.POWER_CAN_GO_TO_DBKB) {
                Input.gI().createFormChooseLevelBDKB(player);
            } else {
                this.npcChat(player, "Yêu cầu sức mạnh lớn hơn " + Util.numberToMoney(BanDoKhoBau.POWER_CAN_GO_TO_DBKB));
            }
        }
    }

    private void handleMenuAcceptGoToBDKB(Player player, int select) {
        if (select == 0) {
            TreasureUnderSeaService.gI().openBanDoKhoBau(player, Byte.parseByte(String.valueOf(PLAYERID_OBJECT.get(player.id))));
        }
    }

    private void handleKOLQuest(Player player, boolean isVIP, int select) {
        int currentStage;
        Map<Integer, KOLQuestData> questsMap;
        int menuType;

        if (isVIP) {
            currentStage = player.kolVIPQuestStage;
            questsMap = KOL_VIP_QUESTS;
            menuType = ConstNpc.KOL_VIP_REWARD_MENU;
        } else {
            currentStage = player.kolQuestStage;
            questsMap = KOL_QUESTS;
            menuType = ConstNpc.KOL_QUEST_MENU;
        }

        if (currentStage < 1) {
            currentStage = 1;
            if (isVIP) {
                player.kolVIPQuestStage = 1;
            } else {
                player.kolQuestStage = 1;
            }
        }

        KOLQuestData questData = questsMap.get(currentStage);

        if (questData == null) {
            this.createOtherMenu(player, menuType, "Con đã hoàn thành tất cả nhiệm vụ " + (isVIP ? "KOL VIP" : "KOL") + " rồi! Chúc mừng con!", "Đóng");
            return;
        }

        long currentProgress = 0;
        switch (questData.questType) {
            case ConstNpc.KOL_QUEST_TYPE_ITEM_COLLECTION:
                Item requiredItem = InventoryService.gI().findItemBag(player, questData.itemId);
                currentProgress = requiredItem != null ? requiredItem.quantity : 0;
                break;
            case ConstNpc.KOL_QUEST_TYPE_DUNGEON_COMPLETION:
                currentProgress = player.destronGas70CompletionCount;
                break;
            case ConstNpc.KOL_QUEST_TYPE_PVP_WINS:
                currentProgress = player.martialArtsTournamentWins;
                break;
            case ConstNpc.KOL_QUEST_TYPE_DAILY_QUEST_COMPLETION:
                currentProgress = player.dailySuperHardQuestCompletionCount;
                break;
            case ConstNpc.KOL_QUEST_TYPE_BOSS_DEFEAT_PARTICIPATION:
                currentProgress = player.bossBabyDefeatParticipationCount;
                break;
            case ConstNpc.KOL_QUEST_TYPE_MONSTER_KILL_COUNT:
                currentProgress = player.monsterKillCountAutoTrain;
                break;
            default:
                Service.gI().sendThongBao(player, "Lỗi: Loại nhiệm vụ không xác định.");
                return;
        }

        int percent = (int) ((currentProgress * 100.0) / questData.requiredQuantity);
        if (percent > 100) {
            percent = 100;
        }

        String rewardDetails = formatRewardDetails(questData.rewards);

        String npcText = questData.description + "\nPhần thưởng: " + rewardDetails + "\nHoàn thành: "
                + currentProgress + "/" + questData.requiredQuantity + " (" + percent + "%)";

        if (currentProgress >= questData.requiredQuantity) {
            this.createOtherMenu(player, menuType, npcText, "Nhận thưởng", "Đóng");
        } else {
            this.createOtherMenu(player, menuType, npcText, "Đóng");
        }
    }

    private void handleKOLQuestRewardConfirm(Player player, int select, boolean isVIP) {
        if (select == 0) {
            int currentStage;
            Map<Integer, KOLQuestData> questsMap;

            if (isVIP) {
                Item vipTicket = InventoryService.gI().findItemBag(player, 1825);
                if (vipTicket == null || vipTicket.quantity < 1) {
                    Service.gI().sendThongBao(player, "Bạn cần có Vé Nhiệm Vụ VIP để nhận thưởng KOL VIP.");
                    openBaseMenu(player);
                    return;
                }
                currentStage = player.kolVIPQuestStage;
                questsMap = KOL_VIP_QUESTS;
            } else {
                currentStage = player.kolQuestStage;
                questsMap = KOL_QUESTS;
            }

            if (currentStage < 1) {
                currentStage = 1;
                if (isVIP) {
                    player.kolVIPQuestStage = 1;
                } else {
                    player.kolQuestStage = 1;
                }
            }

            KOLQuestData questData = questsMap.get(currentStage);

            if (questData == null) {
                Service.gI().sendThongBao(player, "Không tìm thấy nhiệm vụ hiện tại.");
                return;
            }

            long currentProgress = 0;
            switch (questData.questType) {
                case ConstNpc.KOL_QUEST_TYPE_ITEM_COLLECTION:
                    Item requiredItem = InventoryService.gI().findItemBag(player, questData.itemId);
                    currentProgress = requiredItem != null ? requiredItem.quantity : 0;
                    break;
                case ConstNpc.KOL_QUEST_TYPE_DUNGEON_COMPLETION:
                    currentProgress = player.destronGas70CompletionCount;
                    break;
                case ConstNpc.KOL_QUEST_TYPE_PVP_WINS:
                    currentProgress = player.martialArtsTournamentWins;
                    break;
                case ConstNpc.KOL_QUEST_TYPE_DAILY_QUEST_COMPLETION:
                    currentProgress = player.dailySuperHardQuestCompletionCount;
                    break;
                case ConstNpc.KOL_QUEST_TYPE_BOSS_DEFEAT_PARTICIPATION:
                    currentProgress = player.bossBabyDefeatParticipationCount;
                    break;
                case ConstNpc.KOL_QUEST_TYPE_MONSTER_KILL_COUNT:
                    currentProgress = player.monsterKillCountAutoTrain;
                    break;
                default:
                    Service.gI().sendThongBao(player, "Lỗi: Loại nhiệm vụ không xác định.");
                    return;
            }

            if (currentProgress >= questData.requiredQuantity) {
                if (questData.questType == ConstNpc.KOL_QUEST_TYPE_ITEM_COLLECTION) {
                    Item requiredItem = InventoryService.gI().findItemBag(player, questData.itemId);
                    if (requiredItem != null) {
                        InventoryService.gI().subQuantityItemsBag(player, requiredItem, questData.requiredQuantity);
                    }
                }

                for (RewardItem rewardData : questData.rewards) {
                    Item rewardItem = ItemService.gI().createNewItem((short) rewardData.itemId);
                    rewardItem.quantity = rewardData.quantity;

                    List<Item.ItemOption> options = getPetBackpackOptions(rewardData.itemId);
                    if (options != null) {
                        rewardItem.itemOptions.addAll(options);
                    }

                    InventoryService.gI().addItemBag(player, rewardItem);
                }
                InventoryService.gI().sendItemBags(player);

                Service.gI().sendThongBao(player, "Bạn đã nhận phần thưởng nhiệm vụ " + (isVIP ? "KOL VIP" : "KOL") + " cấp " + currentStage + "!");

                if (isVIP) {
                    player.kolVIPQuestStage++;
                } else {
                    player.kolQuestStage++;
                }

                openBaseMenu(player);

            } else {
                Service.gI().sendThongBao(player, "Bạn không đủ điều kiện hoàn thành nhiệm vụ!");
            }
        }
    }

    private List<Item.ItemOption> getPetBackpackOptions(int itemId) {
        List<Item.ItemOption> options = new ArrayList<>();
        options.add(new Item.ItemOption(73, 0));

        switch (itemId) {
            case 1360:
                options.add(new Item.ItemOption(77, 13));
                options.add(new Item.ItemOption(103, 13));
                options.add(new Item.ItemOption(50, 13));
                options.add(new Item.ItemOption(101, 20));
                options.add(new Item.ItemOption(30, 1));
                options.add(new Item.ItemOption(93, 90));
                break;
            case 1654:
                options.add(new Item.ItemOption(50, 16));
                options.add(new Item.ItemOption(77, 15));
                options.add(new Item.ItemOption(103, 15));
                options.add(new Item.ItemOption(106, 0));
                options.add(new Item.ItemOption(30, 1));
                options.add(new Item.ItemOption(93, 30));
                break;
            case 1797:
                options.add(new Item.ItemOption(50, 25));
                options.add(new Item.ItemOption(103, 30));
                options.add(new Item.ItemOption(30, 1));
                options.add(new Item.ItemOption(93, 90));
                break;
            default:
                return null;
        }
        return options;
    }

    private void nhanCanoc(Player player) {
        if (player.lastCheck != null) {
            LocalDate last = player.lastCheck.toLocalDate();
            LocalDate today = LocalDate.now();
            if (last.isEqual(today)) {
                Service.gI().sendThongBao(player, "Bạn đã nhận cá nóc hôm nay rồi!");
                return;
            }
        }
        player.lastCheck = LocalDateTime.now();
        Item item = ItemService.gI().createNewItem((short) 1002);
        item.quantity = 2;
        item.itemOptions.add(new Item.ItemOption(30, 0));
        item.itemOptions.add(new Item.ItemOption(86, 0));
        item.itemOptions.add(new Item.ItemOption(174, 2025));
        InventoryService.gI().addItemBag(player, item);
        PlayerService.gI().sendInfoHpMpMoney(player);
        InventoryService.gI().sendItemBags(player);

        Service.gI().sendThongBao(player, "Bạn nhận được 2 cá nóc!");
    }

    private String formatRewardDetails(List<RewardItem> rewards) {
        StringBuilder details = new StringBuilder();
        for (int i = 0; i < rewards.size(); i++) {
            RewardItem rewardData = rewards.get(i);
            String rewardName = ItemService.gI().getTemplate(rewardData.itemId).name;
            details.append(rewardData.quantity).append(" ").append(rewardName);
            if (i < rewards.size() - 1) {
                details.append(", ");
            }
        }
        return details.toString();
    }
}
