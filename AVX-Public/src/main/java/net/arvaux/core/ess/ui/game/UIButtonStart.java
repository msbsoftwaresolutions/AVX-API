package net.arvaux.core.ess.ui.game;

import net.arvaux.core.entity.player.GamePlayer;
import net.arvaux.core.ui.UIButton;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class UIButtonStart extends UIButton {

    public UIButtonStart() {
        super("§a§l/forcestart", new ItemStack(Material.EMERALD_BLOCK), 1);
    }

    @Override
    public void execute(GamePlayer player) {
        player.command("forcestart");
        this.closeOnClick(true);
    }
}
