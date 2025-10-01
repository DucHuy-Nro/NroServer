package nro.models.player;

/**
 *
 * @author By Mr Blue
 */
public class Captcha {

    public String code;
    public long timeStart;
    public boolean verified;

    public Captcha(String code) {
        this.code = code;
        this.timeStart = System.currentTimeMillis();
        this.verified = false;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - timeStart > 60_000;
    }
}
