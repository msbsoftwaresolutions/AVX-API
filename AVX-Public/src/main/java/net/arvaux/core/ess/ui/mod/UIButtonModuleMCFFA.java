package net.arvaux.core.ess.ui.mod;

import net.arvaux.core.entity.player.GamePlayer;
import net.arvaux.core.module.Module;
import net.arvaux.core.ui.UIButton;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class UIButtonModuleMCFFA extends UIButton {

    public UIButtonModuleMCFFA(int pos) {
        super("§7Switch to §6MCFFA", new ItemStack(Material.STONE_SWORD), pos);
    }

    @Override
    public void execute(GamePlayer player) {
        Module.setModule(Module.MCFFA);
        this.closeOnClick(true);
    }
}
