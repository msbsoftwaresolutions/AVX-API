package net.arvaux.core.ess.ui.game;

import net.arvaux.core.ess.ui.mod.UIButtonModuleMCBW;
import net.arvaux.core.ess.ui.mod.UIButtonModuleMCFFA;
import net.arvaux.core.ess.ui.mod.UIButtonModuleMCSG;
import net.arvaux.core.module.Module;
import net.arvaux.core.ui.UIViewController;

public class UIStoryGame extends UIViewController {
    public UIStoryGame() {
        super("Game Master", 3);

        // Time Control Buttons
        this.add(new UIButtonStart());
        this.add(new UIButtonStop());
        if (Module.isModule(Module.MCSG)) {
            this.add(new UIButtonDM());
        }

        // Gamemode and Minigame Buttons
        this.add(new UIButtonModuleMCSG(12));
        this.add(new UIButtonModuleMCBW(13));
        this.add(new UIButtonModuleMCFFA(14));

        // Map Buttons

    }
}
