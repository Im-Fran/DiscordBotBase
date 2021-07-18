package cl.franciscosolis.discordbotbase.objects;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
public class Cooldown {

    private final OffsetDateTime offsetDateTime;

    public Cooldown(final OffsetDateTime time) {
        this.offsetDateTime = time;
    }

    public int getRemainingCooldown() {
        return Math.max((int) Math.ceil(OffsetDateTime.now().until(offsetDateTime, ChronoUnit.MILLIS) / 1000D), 0);
    }

    public boolean isCooldownRemaining() {
        return getRemainingCooldown() != 0;
    }

}