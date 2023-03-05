package net.arvaux.core.ui;

import net.arvaux.core.entity.player.GamePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public abstract class UIButton implements Listener {

    public ItemStack item;
    public String name;
    public GamePlayer player;
    private boolean _close;
    private List<String> _description;
    private boolean _enchanted;
    private boolean _hidden;
    private int _position;
    private int _amount;

    public UIButton(String name, ItemStack item, int position) {
        this.name = "§b§l" + name;
        this.item = item;
        this._position = position;
    }

    public UIButton(String name, ItemStack item, int position, int amount) {
        this.name = name;
        this.item = item;
        this._position = position;
        this._amount = amount;
    }

    public UIButton(String name) {

    }

    @EventHandler
    public void c(InventoryClickEvent event) throws IOException {
        this.execute(new GamePlayer(event.getWhoClicked().getUniqueId()));
    }

    public void closeOnClick(boolean close) {
        this._close = close;
    }

    public boolean doesCloseOnClick() {
        return this._close;
    }

    public abstract void execute(GamePlayer player) throws IOException;

    public List<String> getDescription() {
        return this._description;
    }

    public void setDescription(String... description) {
        this._description = Arrays.asList(description);
    }

    public boolean getIsEnchanted() {
        return this._enchanted;
    }

    public ItemStack getItemStack() {
        return this.item;
    }

    public String getName() {
        return this.name;
    }

    public int getPosition() {
        return this._position;
    }

    public int getAmount() {
        return this._position;
    }


    public boolean isHidden() {
        return this._hidden;
    }

    public void setHidden(boolean hidden) {
        this._hidden = hidden;
    }

    public void setEnchanted(boolean enchanted) {
        this._enchanted = enchanted;
    }

}