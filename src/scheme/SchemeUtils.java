package mindustry.scheme;

import arc.math.geom.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.game.*;
import mindustry.content.*;

import static arc.Core.*;
import static mindustry.Vars.*;

// all the helper functions here ...
// also this class makes it easy to add admin`s commands
// oh no /js looks so bad
public class SchemeUtils{

    public static void template(Runnable admins, Runnable js, Runnable server){
        if(settings.getBool("adminssecret")){
            if(settings.getBool("usejs")) js.run();
            else admins.run();
        }else {
            if(net.client()) ui.showInfoFade("@feature.serveronly");
            else server.run();
        }
    }

	public static void history(){
		Call.sendChatMessage("/history");
	}

	public static void toggleCoreItems(){
		settings.put("coreitems", !settings.getBool("coreitems"));
	}

    public static void changeUnit(){
        Runnable admins = () -> {
            SchemeSize.unit.select(false, (unit, amount) -> {
                Call.sendChatMessage("/units change " + unit.name);
                updatefrag();
            });
        };
        Runnable js = () -> {
            SchemeSize.unit.select(false, (unit, amount) -> {
                Call.sendChatMessage(js(
                    "player.unit().kill()\n" +
                    "var newUnit = " + getUnit(unit) + ".spawn(player.team(), player.x, player.y);\n" +
                    "Call.unitControl(player, newUnit);"
                ));
                updatefrag();
            });
        };
        Runnable server = () -> {
            SchemeSize.unit.select(false, (unit, amount) -> { // I think there is an easier way, but I do not know it
                player.unit().kill(); // remove does work in multiplayer, so I use kill
                var newUnit = unit.spawn(player.team(), player.x, player.y);
                Call.unitControl(player, newUnit);
                updatefrag();
            });
        };
        template(admins, js, server);
    }

    public static void changeEffect(){
        SchemeSize.effect.select(true, (effect, amount) -> {
            if(amount.get() == 0) player.unit().unapply(effect);
            else player.unit().apply(effect, amount.get());
        });
    }

    public static void changeItem(){
        Runnable admins = () -> {
            SchemeSize.item.select(true, (item, amount) -> {
                Call.sendChatMessage("/give " + item.name + " " + String.valueOf(fix(item, (int)amount.get())));
            });
        };
        Runnable js = () -> {
            SchemeSize.unit.select(false, (item, amount) -> {
                Call.sendChatMessage(js(
                    "player.team().core().items.add(" + getItem(item) + ", " + String.valueOf(fix(item, (int)amount.get())) + ");"
                ));
            });
        };
        Runnable server = () -> {
            SchemeSize.item.select(true, (item, amount) -> {
                player.team().core().items.add(item, fix(item, (int)amount.get()));
            });
        };
        template(admins, js, server);
    }

	public static void changeTeam(){
        Runnable admins = () -> {
            SchemeSize.team.select((team, plr) -> {
                Call.sendChatMessage("/team " + team.name + " " + plr.name);
            });
        };
        Runnable server = () -> {
            SchemeSize.team.select((team, plr) -> {
                plr.team(team);
            });
        };
        template(admins, admins, server);
    }

	public static void placeCore(){
        Runnable admins = () -> {
            Call.sendChatMessage("/core small");
        };
        Runnable server = () -> {
            var tile = world.tiles.get(player.tileX(), player.tileY());
            if(tile != null) tile.setNet(tile.block() != Blocks.coreShard ? Blocks.coreShard : Blocks.air, player.team(), 0);
        };
        template(admins, admins, server);
    }

    public static void lookAt(){
    	player.unit().lookAt(input.mouseWorld());
    }

    public static void teleport(Vec2 pos){
    	player.unit().set(pos);
    }

    public static void selfDest(){
    	player.unit().kill();
    	updatefrag();
    }

    public static void spawnUnit(){
        Runnable admins = () -> {
            SchemeSize.unit.select(true, (unit, amount) -> {
                Call.sendChatMessage("/spawn " + unit.name + " " + String.valueOf((int)amount.get()) + " " + player.team().name);
            });
        };
        Runnable server = () -> {
            SchemeSize.unit.select(true, (unit, amount) -> {
                for (int i = 0; i < amount.get(); i++)
                    unit.spawn(player.team(), player.x, player.y);
            });
        };
        template(admins, admins, server);
    }

    public static void showInfo(){
        SchemeSize.keycomb.show();
    }

    // helpfull methods
    private static void updatefrag(){
        SchemeSize.hudfrag.updateShield(player.unit());
    }

    private static int fix(Item item, int amount){
        var items = player.team().core().items;
        return amount == 0 ? -items.get(item) : (items.get(item) + amount < 0 ? -items.get(item) : amount);
    }

    // js helpfull methods
    private static String js(String code){
        return "/js var player = Groups.player.find(p => p.name == \"" + Vars.player.name + "\");\n" + js;
    }

    private static String getUnit(UnitType unit){
        return "Vars.content.units().find(u => u.name == \"" + unit.name + "\")";
    }

    private static String getItem(Item item){
        return "Vars.content.items().find(i => i.name == \"" + item.name + "\")";
    }
}