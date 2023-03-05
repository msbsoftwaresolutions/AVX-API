package net.arvaux.core.hologram;

import net.arvaux.core.menu.utils.MultiLanguageManager;
import net.minecraft.server.v1_8_R3.Packet;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Objects;

public class LocaleSpecificHologram extends HologramBase {

    static {
        HoloEntityCoreFunc.addToMaps(LocaleSpecificHologram.class, EntityType.ARMOR_STAND);
    }

    private Locale locale;
    private HologramLine line;

    LocaleSpecificHologram(Locale locale, Location location, String message, HologramLine line) {
        super(location, false);
        this.locale = locale;
        this.line = line;

        this.setSmall(true);
        this.setCustomName(message);
        this.setCustomNameVisible(true);

        this.spawn();
    }

    @Override
    public void sendPacket(Player player, Packet packet) {
        if (!Objects.equals(MultiLanguageManager.getPlayerLocale().toLanguageTag(), this.locale.toLanguageTag())) {
            return;
        }

        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    @Override
    public boolean isValid() {
        return this.line.isValid();
    }
}
