package net.arvaux.core.ui;

import net.arvaux.core.entity.player.GamePlayer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;

public class UICloseButton extends UIButton{

    public UICloseButton(int position) {
        super("§c✖ §c§lClose §c✖", new ItemStack(Material.BARRIER), position);
    }

    @Override
    public void execute(GamePlayer player) {
        this.closeOnClick(true);
    }
}
