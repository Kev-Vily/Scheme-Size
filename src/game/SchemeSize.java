package mindustry.game;

import arc.*;
import arc.util.*;
import mindustry.mod.*;
import mindustry.Vars;
import mindustry.game.EventType.*;
import mindustry.input.*;
import mindustry.ui.dialogs.*;

public class SchemeSize extends Mod{

    public SchemeSize(){
        Events.on(ClientLoadEvent.class, e -> {
            //wait 10 secs, because... idk
            Time.runTask(10f, () -> {
                // Change Schematics
                Vars.schematics = new Schematics512();
                Vars.schematics.loadSync();

                // Change Input
                if(!Vars.mobile){
                    Vars.control.setInput(new DesktopInput512());
                }

                // Change Menu
                Vars.ui.settings = new SettingsMenuDialogMod();

                // Logs
                // Log.info(Vars.schematics);
                // Log.info(Vars.control.input);
                // Log.info(Vars.ui.settings);
                // Log.info(Vars.mobile);
            });
        });
    }
}