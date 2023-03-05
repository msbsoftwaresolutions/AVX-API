package net.arvaux.core.hologram;

import net.arvaux.core.hologram.entity.PersistentEntity;
import net.arvaux.core.menu.utils.Message;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class EntityAttachedHologram extends Hologram {

    private Entity entity;

    public EntityAttachedHologram(Location location, Entity entity, String... messages) {
        super(location, messages);
        this.entity = entity;
    }

    public EntityAttachedHologram(Location location, Entity entity, Message... messages) {
        super(location, messages);
        this.entity = entity;
    }

    @Override
    public boolean isValid() {
        if (this.entity instanceof PersistentEntity) {
            return !((PersistentEntity) this.entity).isUnloaded();
        }

        return this.entity != null && !this.entity.isDead() && this.entity.isValid();
    }
}
