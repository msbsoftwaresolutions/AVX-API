package net.arvaux.core.ess.ui.mod;

import net.arvaux.core.entity.player.GamePlayer;
import net.arvaux.core.module.Module;
import net.arvaux.core.ui.UIButton;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class UIButtonModuleHub extends UIButton {

    public UIButtonModuleHub(int pos) {
        super("§7Switch to §6Hub", new ItemStack(Material.COMPASS), pos);
    }

    @Override
    public void execute(GamePlayer player) {
        Module.setModule(Module.HUB);
        this.closeOnClick(true);
    }
}
