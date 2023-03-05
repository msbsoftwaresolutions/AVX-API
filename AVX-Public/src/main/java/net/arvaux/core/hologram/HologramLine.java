package net.arvaux.core.hologram;

import com.google.common.collect.Maps;
import net.arvaux.core.menu.utils.Message;
import org.bukkit.Location;

import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

public class HologramLine {
    private Map<Locale, LocaleSpecificHologram> armorStandMap;
    private Message message;
    private Location location;
    private Hologram parent;

    public HologramLine(Message message, Location location, Hologram parent) {
        this.parent = parent;
        if (message == null) {
            throw new IllegalArgumentException("message cant be null");
        }

        this.location = location;
        this.armorStandMap = Maps.newConcurrentMap();
        this.message = message;

        this.spawn(Locale.ENGLISH); // If we support more than one language, change this to iterate through #values()
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;

        for (Entry<Locale, LocaleSpecificHologram> hologramEntry : this.armorStandMap.entrySet()) {
            LocaleSpecificHologram hologram = hologramEntry.getValue();
            hologram.setCustomName(message.toString(hologramEntry.getKey()).replace('&', '§'));
            hologram.sendMetadata();
        }
    }

    public Location getLocation() {
        return location.clone();
    }

    public void teleport(Location location) {
        this.armorStandMap.values().forEach(armorStand -> armorStand.teleport(location));
    }

    private void spawn(Locale locale) {
        LocaleSpecificHologram armorStand = new LocaleSpecificHologram(locale, this.location, this.message.toString(locale).replace('&', '§'), this);
        this.armorStandMap.put(locale, armorStand);
    }

    public void move(double x, double y, double z) {
        this.armorStandMap.values().forEach(stand -> stand.move(x, y, z));
    }

    public void remove() {
        this.armorStandMap.values().forEach(LocaleSpecificHologram::removeEntity);
        this.armorStandMap.clear();
    }

    public boolean isValid() {
        return this.parent.isValid();
    }
}
