package net.arvaux.core.hologram.entity;

public interface PersistentEntity {

    boolean isUnloaded();

    void setUnloaded(boolean value);

    int getChunkX();

    int getChunkZ();
}
