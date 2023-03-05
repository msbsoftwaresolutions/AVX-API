package net.arvaux.core.ui;

import net.arvaux.core.entity.player.GamePlayer;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UIViewController implements Listener {

    private List<UIButton> _buttons;
    private int _lines;
    private String _name;

    public UIViewController(String name, int lines) {
        this._name = "§n" + name;
        this._buttons = new ArrayList<UIButton>();
        this._lines = lines;
    }

    public void add(UIButton button) {
        ItemStack stack = button.getItemStack();
        ItemMeta meta = stack.getItemMeta();

        if (button.getName() != null) {
            meta.setDisplayName(button.getName());
        }
        if (button.getDescription() != null) {
            meta.setLore(button.getDescription());
        }
        if (button.getIsEnchanted() == true) {
            meta.addEnchant(Enchantment.KNOCKBACK, 10, false);
        }

        this.getButtons().add(button);

        stack.setItemMeta(meta);
    }

    @EventHandler
    public void c(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            GamePlayer player = new GamePlayer(event.getWhoClicked().getUniqueId());
            if (event.getInventory().getName().equals(this.getName())) {
                event.setCancelled(true);
                try {
                    if ((event.getCurrentItem().getType() != Material.AIR) &&
                            (event.getCurrentItem().hasItemMeta())) {
                        ItemStack item = event.getCurrentItem();
                        for (UIButton button : this.getButtons()) {
                            if ((item.getType() == button.getItemStack().getType())
                                    && (item.getItemMeta().getDisplayName().equalsIgnoreCase(button.getName()))) {
                                button.c(event);
                                if (button.doesCloseOnClick() == true) {
                                    player.bukkit().closeInventory();
                                    return;
                                }

                                return;
                            }
                        }
                    } else {
                        //player.send(Sound.ITEM_BREAK, 0);
                    }
                } catch (Exception exception) {
                    //exception.printStackTrace();
                }
            }

        }
    }

    public Collection<UIButton> getButtons() {
        return this._buttons;
    }

    public int getLines() {
        return this._lines;
    }

    public String getName() {
        return this._name;
    }

}
