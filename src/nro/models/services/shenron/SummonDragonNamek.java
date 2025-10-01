package nro.models.services.shenron;

import java.util.ArrayList;
import nro.models.network.Message;
import nro.models.consts.ConstNpc;
import nro.models.database.MrBlue;
import nro.models.database.PlayerDAO;
import nro.models.item.Item;
import java.util.List;
import nro.models.clan.ClanMember;
import nro.models.map.Zone;
import nro.models.player.Player;
import nro.models.server.Client;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.map.service.NpcService;
import nro.models.player.TempEffect;
import nro.models.services.Service;
import nro.models.utils.Logger;
import nro.models.utils.Util;

/**
 *
 * @author By Mr Blue
 *
 */
public class SummonDragonNamek {

    public static final byte DRAGON_PORUNGA = 1;
    private static SummonDragonNamek instance;

    public static final byte WISHED = 0;
    public static final byte TIME_UP = 1;
    private boolean isShenronAppear;
    public Player playerSummonShenron;
    private int playerSummonShenronId;
    private Zone mapShenronAppear;
    private int menuShenron;
    private byte select;
    private final Thread update;
    private boolean active;
    public boolean isPlayerDisconnect;
    private long lastTimeShenronWait;
    private final int timeShenronWait = 300000;

    public static SummonDragonNamek gI() {
        if (instance == null) {
            instance = new SummonDragonNamek();
        }
        return instance;
    }

    private SummonDragonNamek() {
        this.update = new Thread(() -> {
            while (active) {
                try {
                    if (isShenronAppear) {
                        if (isPlayerDisconnect) {
                            List<Player> players = mapShenronAppear.getPlayers();
                            for (Player plMap : players) {
                                if (plMap.isPl() && plMap.id == playerSummonShenronId) {
                                    playerSummonShenron = plMap;
                                    reSummonShenron();
                                    isPlayerDisconnect = false;
                                    break;
                                }
                            }

                        }
                        if (Util.canDoWithTime(lastTimeShenronWait, timeShenronWait)) {
                            shenronLeave(playerSummonShenron, TIME_UP);
                        }
                    }
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        this.active();
    }

    private void active() {
        if (!active) {
            active = true;
            this.update.start();
        }
    }

    public void summonNamec(Player pl) {
        if (pl.zone.map.mapId == 7) {
            playerSummonShenron = pl;
            playerSummonShenronId = (int) pl.id;
            mapShenronAppear = pl.zone;
            lastTimeShenronWait = System.currentTimeMillis();
            sendNotifyShenronNamekAppear();
            activeShenron(pl, true, DRAGON_PORUNGA);
            sendBlackGokuhesNamec(pl);
        } else {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
        }
    }

    private void reSummonShenron() {
        activeShenron(playerSummonShenron, true, DRAGON_PORUNGA);
        sendBlackGokuhesNamec(playerSummonShenron);
    }

    private void activeShenron(Player pl, boolean appear, byte type) {
        Message msg;
        try {
            msg = new Message(-83);
            msg.writer().writeByte(appear ? 0 : (byte) 1);
            if (appear) {
                msg.writer().writeShort(pl.zone.map.mapId);
                msg.writer().writeShort(pl.zone.map.bgId);
                msg.writer().writeByte(pl.zone.zoneId);
                msg.writer().writeInt((int) pl.id);
                msg.writer().writeUTF("null");
                msg.writer().writeShort(pl.location.x);
                msg.writer().writeShort(pl.location.y);
                msg.writer().writeByte(type);
                isShenronAppear = true;
            }
            Service.gI().sendMessAllPlayer(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendNotifyShenronNamekAppear() {
        Message msg = null;
        try {
            msg = new Message(-25);
            msg.writer().writeUTF(playerSummonShenron.name + " vừa gọi rồng thần namek tại "
                    + playerSummonShenron.zone.map.mapName + " khu vực " + playerSummonShenron.zone.zoneId);
            Service.gI().sendMessAllPlayerIgnoreMe(playerSummonShenron, msg);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void confirmWish() {
        if (this.menuShenron == ConstNpc.SHOW_SHENRON_NAMEK_CONFIRM) {
            try {
                byte option = -1;
                switch (select) {
                    case 0:
                        option = 50; // Sức đánh
                        break;
                    case 1:
                        option = 77; // HP
                        break;
                    case 2:
                        option = 103; // KI
                        break;
                }

                if (option != -1) {
                    int param = 20;
                    long duration = 7 * 24 * 60 * 60 * 1000L;
                    long expireTime = System.currentTimeMillis() + duration;

                    if (playerSummonShenron.clan != null) {
                        for (ClanMember member : playerSummonShenron.clan.members) {
                            Player p = Client.gI().getPlayer(member.id);
                            if (p == null) {
                                p = MrBlue.loadById(member.id);
                            }

                            if (p != null) {
                                if (p.tempEffects == null) {
                                    p.tempEffects = new ArrayList<>();
                                }

                                p.tempEffects.add(new TempEffect(option, param, expireTime));
                                if (p.session != null) {
                                    Service.gI().sendThongBao(p, "Bạn nhận được buff +20% chỉ số trong 7 ngày");
                                }

                                PlayerDAO.saveTempEffect(p.id, option, param, expireTime);
                            }
                        }
                    } else {
                        if (playerSummonShenron.tempEffects == null) {
                            playerSummonShenron.tempEffects = new ArrayList<>();
                        }

                        playerSummonShenron.tempEffects.add(new TempEffect(option, param, expireTime));
                        Service.gI().sendThongBao(playerSummonShenron, "Bạn nhận được buff +20% chỉ số trong 7 ngày");
                        PlayerDAO.saveTempEffect(playerSummonShenron.id, option, param, expireTime);
                    }
                }
            } catch (Exception e) {
                Logger.logException(getClass(), e, "Lỗi khi thực hiện điều ước Shenron");
            }
            shenronLeave(this.playerSummonShenron, WISHED);
        }
    }

    public void showConfirmShenron(Player pl, int menu, byte select) {
        this.menuShenron = menu;
        this.select = select;
        String wish = null;
        switch (menu) {
            case ConstNpc.SHOW_SHENRON_NAMEK_CONFIRM:
                switch (select) {
                    case 0:
                        wish = "20% sức đánh cho toàn bang hội";
                        break;
                    case 1:
                        wish = "20% hp cho toàn bang hội";
                        break;
                    case 2:
                        wish = "20% ki cho toàn bang hội";
                        break;
                }
                break;
        }
        NpcService.gI().createMenuRongThieng(pl, ConstNpc.SHENRON_NAMEK_CONFIRM, "Ngươi có chắc muốn ước?", wish, "Từ chối");
    }

    public void sendBlackGokuhesNamec(Player pl) {
        NpcService.gI().createMenuRongThieng(pl, ConstNpc.SHOW_SHENRON_NAMEK_CONFIRM, "Ta sẽ ban cho cả bang hội ngươi 1 điều ước, ngươi có 5 phút, hãy suy nghĩ thật kỹ trước khi quyết định", "20%\n sức đánh", "20%\nHp", "20% Ki");
    }

    public void shenronLeave(Player pl, byte type) {
        if (type == WISHED) {
            NpcService.gI().createTutorial(pl, 0, "Điều ước của ngươi đã được thực hiện...tạm biệt");
        } else {
            NpcService.gI().createMenuRongThieng(pl, ConstNpc.IGNORE_MENU, "Ta buồn ngủ quá rồi\nHẹn gặp ngươi lần sau, ta đi đây, bái bai");
        }
        activeShenron(pl, false, SummonDragon.DRAGON_SHENRON);
        this.isShenronAppear = false;
        this.menuShenron = -1;
        this.select = -1;
        this.playerSummonShenron = null;
        this.playerSummonShenronId = -1;
        this.mapShenronAppear = null;
    }
}
