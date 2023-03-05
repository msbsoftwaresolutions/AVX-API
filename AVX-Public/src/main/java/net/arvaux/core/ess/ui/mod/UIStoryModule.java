package net.arvaux.core.ess.ui.mod;

import net.arvaux.core.ui.UIViewController;

public class UIStoryModule extends UIViewController {

    public UIStoryModule() {
        super("Module Switcher", 3);
        this.add(new UIButtonModuleDefault(11));
        this.add(new UIButtonModuleHub(12));
        this.add(new UIButtonModuleMCSG(13));
        this.add(new UIButtonModuleBuild(14));
        this.add(new UIButtonModuleMCBW(15));
        this.add(new UIButtonModuleMCFFA(16));
    }

}
