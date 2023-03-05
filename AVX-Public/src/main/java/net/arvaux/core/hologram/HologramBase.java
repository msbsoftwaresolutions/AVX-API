package net.arvaux.core.hologram;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.events.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.arvaux.core.Main;
import net.arvaux.core.util.NullBoundingBox;
import net.arvaux.core.util.UtilConcurrency;
import net.arvaux.core.util.UtilReflection;
import net.minecraft.server.v1_8_R3.*;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntity.PacketPlayOutRelEntityMove;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Set;

public class HologramBase extends EntityArmorStand {

    private static final Set<HologramBase> HOLOGRAM_BASES = Sets.newHashSet();
    private static final Object LOCK = new Object();

    static {
        HoloEntityCoreFunc.addToMaps(HologramBase.class, EntityType.ARMOR_STAND);
    }

    static {
        new BukkitRunnable() {
            @Override
            public void run() {
                List<HologramBase> toRemove = Lists.newArrayList();

                synchronized (LOCK) {
                    HOLOGRAM_BASES.stream().filter(base -> !base.isValid()).forEach(toRemove::add);
                }

                for (HologramBase base : toRemove) {
                    base.removeEntity();
                }
            }
        }.runTaskTimerAsynchronously(Main.getInstance(), 20L, 20L);

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketListener() {
            @Override
            public void onPacketSending(PacketEvent event) {
                Player player = event.getPlayer();

                if (player == null || !player.isOnline()) {
                    return;
                }

                // DO NOT REMOVE ASYNC CALLS! THIS WILL BREAK VIAVERSION!
                UtilConcurrency.runAsync(() -> {
                    PacketContainer container = event.getPacket();

                    try {
                        if (!(container.getHandle() instanceof PacketPlayOutMapChunk)) {
                            PacketPlayOutMapChunkBulk bulkPacket = (PacketPlayOutMapChunkBulk) container.getHandle();
                            int[] x = (int[]) UtilReflection.getValue(bulkPacket, true, "a");
                            int[] z = (int[]) UtilReflection.getValue(bulkPacket, true, "b");

                            for (int i = 0; i < x.length; i++) {
                                processChunkLoad(event.getPlayer(), x[i], z[i]);
                            }
                            return;
                        }

                        PacketPlayOutMapChunk packet = (PacketPlayOutMapChunk) container.getHandle();
                        int x = (int) UtilReflection.getValue(packet, true, "a");
                        int z = (int) UtilReflection.getValue(packet, true, "b");

                        processChunkLoad(player, x, z);
                    } catch (IllegalAccessException | NoSuchFieldException e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void onPacketReceiving(PacketEvent packetEvent) {
            }

            @Override
            public ListeningWhitelist getSendingWhitelist() {
                return ListeningWhitelist.newBuilder()
                        .types(Server.MAP_CHUNK, Server.MAP_CHUNK_BULK)
                        .options(new ListenerOptions[]{ListenerOptions.ASYNC}).build();
            }

            @Override
            public ListeningWhitelist getReceivingWhitelist() {
                return ListeningWhitelist.EMPTY_WHITELIST;
            }

            @Override
            public Plugin getPlugin() {
                return Main.getInstance();
            }
        });
    }

    public HologramBase(Location location) {
        this(location, true);
    }

    public HologramBase(Location location, boolean spawn) {
        super(((CraftWorld) location.getWorld()).getHandle());
        super.a(new NullBoundingBox());

        this.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        this.setInvisible(true);
        this.setGravity(false);

        if (spawn) {
            this.spawn();
        }
    }

    private static void processChunkLoad(Player player, int x, int z) {
        synchronized (LOCK) {
            for (HologramBase base : HOLOGRAM_BASES) {
                Location location = base.getLocation();
                int chunkX = location.getBlockX() >> 4;
                int chunkZ = location.getBlockZ() >> 4;
                if (chunkX != x || chunkZ != z) {
                    continue;
                }

                PacketPlayOutSpawnEntityLiving refreshPacket = new PacketPlayOutSpawnEntityLiving(base);
                base.sendPacket(player, refreshPacket);
            }
        }
    }

    public void spawn() {
        this.sendPacket(new PacketPlayOutSpawnEntityLiving(this));

        synchronized (LOCK) {
            HOLOGRAM_BASES.add(this);
        }
    }

    public void sendMetadata() {
        PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(this.getId(), this.getDataWatcher(), true);
        this.sendPacket(metadata);
    }

    public void setHelmet(ItemStack itemStack) {
        this.sendPacket(new PacketPlayOutEntityEquipment(this.getId(), 4, CraftItemStack.asNMSCopy(itemStack)));
    }

    public void setChestplate(ItemStack itemStack) {
        this.sendPacket(new PacketPlayOutEntityEquipment(this.getId(), 3, CraftItemStack.asNMSCopy(itemStack)));
    }

    public void setLeggings(ItemStack itemStack) {
        this.sendPacket(new PacketPlayOutEntityEquipment(this.getId(), 2, CraftItemStack.asNMSCopy(itemStack)));
    }

    public void setBoots(ItemStack itemStack) {
        this.sendPacket(new PacketPlayOutEntityEquipment(this.getId(), 1, CraftItemStack.asNMSCopy(itemStack)));
    }

    public void setItemInHand(ItemStack itemStack) {
        this.sendPacket(new PacketPlayOutEntityEquipment(this.getId(), 0, CraftItemStack.asNMSCopy(itemStack)));
    }

    @Override
    public void b(NBTTagCompound nbttagcompound) {
        // Do not save NBT.
    }

    @Override
    public boolean c(NBTTagCompound nbttagcompound) {
        // Do not save NBT.
        return false;
    }

    @Override
    public boolean d(NBTTagCompound nbttagcompound) {
        // Do not save NBT.
        return false;
    }

    @Override
    public void e(NBTTagCompound nbttagcompound) {
        // Do not save NBT.
    }

    @Override
    public boolean isInvulnerable(DamageSource damagesource) {
        return true;
    }

    @Override
    public void a(AxisAlignedBB boundingBox) {
        // Prevent change
    }

    public void removeEntity() {
        this.sendPacket(new PacketPlayOutEntityDestroy(this.getId()));

        synchronized (LOCK) {
            HOLOGRAM_BASES.remove(this);
        }
    }

    public Location getLocation() {
        return new Location(this.world.getWorld(), this.locX, this.locY, this.locZ, this.yaw, this.pitch);
    }

    public void teleport(Location location) {
        Location prevLoc = this.getLocation();
        this.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

        double dist = location.distance(prevLoc);

        if (dist < 8) {
            int xDelta = ((int) (this.locX * 32)) - ((int) (prevLoc.getX() * 32));
            int yDelta = ((int) (this.locY * 32)) - ((int) (prevLoc.getY() * 32));
            int zDelta = ((int) (this.locZ * 32)) - ((int) (prevLoc.getZ() * 32));

            this.sendPacket(
                    new PacketPlayOutRelEntityMoveLook(
                            this.getId(),
                            (byte) xDelta,
                            (byte) yDelta,
                            (byte) zDelta,
                            (byte) ((int) (this.yaw * 256.0F / 360.0F)),
                            (byte) ((int) (this.pitch * 256.0F / 360.0F)),
                            this.onGround
                    )
            );
        }

        if (dist > 4) {
            this.sendPacket(new PacketPlayOutEntityTeleport(this));
        }
    }

    public void move(double x, double y, double z) {
        Location prevLoc = this.getLocation();
        super.move(x, y, z);
        int xDelta = ((int) (this.locX * 32)) - ((int) (prevLoc.getX() * 32));
        int yDelta = ((int) (this.locY * 32)) - ((int) (prevLoc.getY() * 32));
        int zDelta = ((int) (this.locZ * 32)) - ((int) (prevLoc.getZ() * 32));

        this.sendPacket(
                new PacketPlayOutRelEntityMove(
                        this.getId(),
                        (byte) xDelta,
                        (byte) yDelta,
                        (byte) zDelta,
                        this.onGround
                )
        );
    }

    public void sendPacket(Packet packet) {
        for (Player player : this.getLocation().getWorld().getPlayers()) {
            this.sendPacket(player, packet);
        }
    }

    public void sendPacket(Player player, Packet packet) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    public boolean isValid() {
        return true;
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }

        if (!(object instanceof HologramBase)) {
            return false;
        }

        HologramBase that = (HologramBase) object;
        return that.getId() == this.getId();
    }

    @Override
    public int hashCode() {
        return this.getId();
    }
}
