package net.arvaux.core.ess.ui.mod;

import net.arvaux.core.entity.player.GamePlayer;
import net.arvaux.core.module.Module;
import net.arvaux.core.ui.UIButton;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class UIButtonModuleBuild extends UIButton {

    public UIButtonModuleBuild(int pos) {
        super("§7Switch to §6Build", new ItemStack(Material.DIAMOND_HOE), pos);
    }

    @Override
    public void execute(GamePlayer player) {
        Module.setModule(Module.BUILD);
        this.closeOnClick(true);
    }
}
