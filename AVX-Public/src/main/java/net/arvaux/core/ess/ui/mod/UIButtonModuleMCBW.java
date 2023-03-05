package net.arvaux.core.ess.ui.mod;

import net.arvaux.core.entity.player.GamePlayer;
import net.arvaux.core.module.Module;
import net.arvaux.core.ui.UIButton;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class UIButtonModuleMCBW extends UIButton {

    public UIButtonModuleMCBW(int pos) {
        super("§7Switch to §6MCBW", new ItemStack(Material.BED), pos);
    }

    @Override
    public void execute(GamePlayer player) {
        Module.setModule(Module.MCBW);
        this.closeOnClick(true);
    }
}
