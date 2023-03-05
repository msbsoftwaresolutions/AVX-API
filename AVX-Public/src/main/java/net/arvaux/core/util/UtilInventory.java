package net.arvaux.core.util;

import com.google.common.collect.Sets;
import net.arvaux.core.menu.item.util.UtilItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class UtilInventory {

    public static void addItemsSafely(Player player, ItemStack... items) {
        PlayerInventory inventory = player.getInventory();
        for (ItemStack item : items) {
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }

            Map<Integer, ItemStack> remaining = inventory.addItem(item);
            if (remaining == null || remaining.isEmpty()) {
                continue;
            }

            remaining.values().forEach(left -> UtilItem.dropItem(player.getLocation(), left));
        }
    }

    public static void clear(PlayerInventory inventory) {
        clear(inventory, true);
    }

    public static void clear(PlayerInventory inventory, boolean armor) {
        PlayerInventoryClearEvent event = new PlayerInventoryClearEvent(inventory, armor);

        inventory.clear();
        inventory.getHolder().setItemOnCursor(new ItemStack(Material.AIR));

        if (armor) {
            inventory.setArmorContents(new ItemStack[4]);
        }

        Bukkit.getPluginManager().callEvent(event);
    }

    public static void clear(Inventory inventory) {
        InventoryClearEvent event = new InventoryClearEvent(inventory);
        inventory.clear();
        Bukkit.getPluginManager().callEvent(event);
    }

    public static int remove(ItemStack stack, int amount, Inventory inventory) {
        ItemStack[] contents = inventory.getContents();

        int toRemove = amount * stack.getAmount();

        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item == null || !item.isSimilar(stack)) {
                continue;
            }

            if (toRemove < item.getAmount()) {
                item.setAmount(item.getAmount() - toRemove);
                inventory.setItem(i, item);
                toRemove = 0;
                break;
            }

            toRemove -= item.getAmount();
            inventory.clear(i);

            if (toRemove <= 0) {
                break;
            }
        }

        // This way, if too many items were removed, we return a positive number.
        // Otherwise, we return a negative number indicating that there were missing
        // items
        return -toRemove;
    }

    public static int getCount(ItemStack stack, Inventory inventory) {
        int count = 0;
        for (ItemStack other : inventory.getContents()) {
            if (other == null || !other.isSimilar(stack)) {
                continue;
            }

            count += other.getAmount();
        }

        return Math.floorDiv(count, stack.getAmount());
    }

    public static int[] getRandomSlots(int lines, int slots) {
        Set<Integer> set = Sets.newHashSet();
        int max = lines * 9;
        if (max < slots) {
            throw new IllegalArgumentException("Too many slots!");
        }

        while (set.size() < slots) {
            set.add(ThreadLocalRandom.current().nextInt(0, max));
        }

        // Map to primitive
        return set.stream().mapToInt(value -> value).toArray();
    }

    public static class PlayerInventoryClearEvent extends InventoryEvent {

        private static final HandlerList HANDLER_LIST = new HandlerList();

        private boolean armorCleared;
        private ItemStack[] armor;

        public PlayerInventoryClearEvent(PlayerInventory inventory, boolean armorCleared) {
            super(inventory);
            this.armor = inventory.getArmorContents();
            this.armorCleared = armorCleared;
        }

        public static HandlerList getHandlerList() {
            return HANDLER_LIST;
        }

        public boolean isArmorCleared() {
            return armorCleared;
        }

        public ItemStack[] getArmor() {
            return armor;
        }

        @Override
        public PlayerInventory getInventory() {
            return (PlayerInventory) super.getInventory();
        }

        @Override
        public HandlerList getHandlers() {
            return HANDLER_LIST;
        }
    }

    public static class InventoryClearEvent extends InventoryEvent {

        private static final HandlerList HANDLER_LIST = new HandlerList();

        public InventoryClearEvent(Inventory inventory) {
            super(inventory);
        }

        public static HandlerList getHandlerList() {
            return HANDLER_LIST;
        }

        @Override
        public HandlerList getHandlers() {
            return HANDLER_LIST;
        }
    }

    public static abstract class InventoryEvent extends Event {

        private Inventory inventory;
        private ItemStack[] contents;

        public InventoryEvent(Inventory inventory) {
            this.inventory = inventory;
            this.contents = inventory.getContents();
        }

        public Inventory getInventory() {
            return inventory;
        }

        public ItemStack[] getContents() {
            return contents;
        }
    }
}
