package net.arvaux.core.ess.ui.mod;

import net.arvaux.core.entity.player.GamePlayer;
import net.arvaux.core.module.Module;
import net.arvaux.core.ui.UIButton;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class UIButtonModuleMCSG extends UIButton {

    public UIButtonModuleMCSG(int pos) {
        super("§7Switch to §6MCSG", new ItemStack(Material.COOKED_CHICKEN), pos);
    }

    @Override
    public void execute(GamePlayer player) {
        Module.setModule(Module.MCSG);
        this.closeOnClick(true);
    }
}
