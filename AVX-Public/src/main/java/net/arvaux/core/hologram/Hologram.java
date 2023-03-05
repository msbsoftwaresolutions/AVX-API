package net.arvaux.core.hologram;

import net.arvaux.core.menu.utils.Message;
import net.arvaux.core.menu.utils.StringMessage;
import net.arvaux.core.util.UtilMath;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Location;

public class Hologram {

    private static final double LINE_OFFSET = 0.25;

    private Location location;
    private HologramLine[] holograms;

    public Hologram(Location location, String... messages) {
        this.location = location;
        this.holograms = new HologramLine[0];

        this.setMessages(messages);
    }

    public Hologram(Location location, Message... messages) {
        this.location = location;
        this.holograms = new HologramLine[messages.length];

        for (int i = 0; i < messages.length; i++) {
            if (messages[i] == null) {
                continue;
            }

            this.holograms[i] = new HologramLine(messages[i], this.location.clone().subtract(0, LINE_OFFSET * i, 0), this);
        }
    }

    public HologramLine[] getHolograms() {
        return holograms;
    }

    public void teleport(Location location) {
        this.location = location;
        for (int i = 0; i < this.holograms.length; i++) {
            this.holograms[i].teleport(location.clone().subtract(0, LINE_OFFSET * i, 0));
        }
    }

    public Location getLocation() {
        return location.clone();
    }

    public void setMessages(String... messages) {
        Message[] messagesArray = new Message[messages.length];
        for (int i = 0; i < messages.length; i++) {
            if (StringUtils.isBlank(messages[i])) {
                continue;
            }

            messagesArray[i] = StringMessage.wrap(messages[i]);
        }

        this.setMessages(messagesArray);
    }

    public void setMessages(Message... messages) {
        if (this.holograms.length == messages.length) {
            for (int i = 0; i < messages.length; i++) {
                if (messages[i] == null) {
                    continue;
                }

                this.setMessage(i, messages[i]);
            }

            return;
        }

        this.remove();
        this.holograms = new HologramLine[messages.length];
        for (int i = 0; i < messages.length; i++) {
            if (messages[i] == null) {
                continue;
            }

            this.holograms[i] = new HologramLine(messages[i], this.location.clone().subtract(0, LINE_OFFSET * i, 0), this);
        }
    }

    public void setMessage(int line, Message message) {
        if (message == null) {
            throw new IllegalArgumentException("Message can't be null!");
        }

        if (UtilMath.clamp(line, 0, this.holograms.length - 1) != line) {
            throw new IllegalArgumentException("Line index is not within acceptable parameters!");
        }

        if (this.holograms[line] == null) {
            this.holograms[line] = new HologramLine(message, this.location.clone().subtract(0, LINE_OFFSET * line, 0), this);
            return;
        }

        this.holograms[line].setMessage(message);
    }

    public void remove() {
        for (HologramLine hologram : this.holograms) {
            if (hologram == null) {
                continue;
            }

            hologram.remove();
        }

        this.holograms = null;
    }

    public boolean isValid() {
        return true;
    }
}
