package net.arvaux.core.ess.ui.game;

import net.arvaux.core.entity.player.GamePlayer;
import net.arvaux.core.ui.UIButton;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class UIButtonDM extends UIButton {

    public UIButtonDM() {
        super("§e§l/deathmatch", new ItemStack(Material.GOLD_BLOCK), 10);
    }

    @Override
    public void execute(GamePlayer player) {
        player.command("dm");
        this.closeOnClick(true);
    }
}
