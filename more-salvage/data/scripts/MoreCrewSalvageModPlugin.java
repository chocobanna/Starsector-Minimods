package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;

public class MoreCrewFromSalvageModPlugin extends BaseModPlugin {

    @Override
    public void onGameLoad(boolean newGame) {
        // Don’t double-register on reloads
        if (!Global.getSector().getListenerManager().hasListenerOfClass(MoreCrewFromSalvageLootListener.class)) {
            Global.getSector().getListenerManager().addListener(new MoreCrewFromSalvageLootListener(), true);
        }
    }
}
