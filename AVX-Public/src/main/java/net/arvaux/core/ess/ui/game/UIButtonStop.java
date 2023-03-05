package net.arvaux.core.ess.ui.game;

import net.arvaux.core.entity.player.GamePlayer;
import net.arvaux.core.ui.UIButton;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class UIButtonStop extends UIButton {

    public UIButtonStop() {
        super("§c§l/forcestop", new ItemStack(Material.REDSTONE_BLOCK), 19);
    }

    @Override
    public void execute(GamePlayer player) {
        player.command("forcestop");
        this.closeOnClick(true);
    }
}
