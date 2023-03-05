package net.arvaux.core.ess.ui.mod;

import net.arvaux.core.entity.player.GamePlayer;
import net.arvaux.core.module.Module;
import net.arvaux.core.ui.UIButton;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class UIButtonModuleDefault extends UIButton {

    public UIButtonModuleDefault(int pos) {
        super("§7Switch to §6Default", new ItemStack(Material.LONG_GRASS, 1, (byte) 2), pos);
    }

    @Override
    public void execute(GamePlayer player) {
        Module.setModule(Module.DEFAULT);
        this.closeOnClick(true);
    }
}
