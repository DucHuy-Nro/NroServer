package nro.models.player;

/**
 *
 * @author By Mr Blue
 */
public class TempEffect {

    public int optionId;
    public int param;
    public long expireTime;

    public TempEffect(int optionId, int param, long durationMillis) {
        this.optionId = optionId;
        this.param = param;
        this.expireTime = System.currentTimeMillis() + durationMillis;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expireTime;
    }
}
