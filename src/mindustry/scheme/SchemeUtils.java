package mindustry.scheme;

import arc.math.geom.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.game.*;
import mindustry.world.*;
import mindustry.content.*;

import static arc.Core.*;
import static mindustry.Vars.*;
import static mindustry.scheme.SchemeVars.*;

// all the helper functions here ...
// also this class makes it easy to add admin`s commands
public class SchemeUtils{

    public static void template(Runnable admins, Runnable js, Runnable server){
        if(!settings.getBool("enabledsecret")) ui.showInfoFade("@feature.secretonly");
        else if(settings.getBool("adminssecret")){
            if(settings.getBool("usejs")) js.run();
            else admins.run();
        } else {
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
            unit.select(false, true, false, (player, team, unit, amount) -> {
                if(!hasCore(player)) return;
                Call.sendChatMessage("/unit " + unit.id + " " + player.name);
                m_renderer.update();
            });
        };
        Runnable js = () -> {
            unit.select(false, true, false, (player, team, unit, amount) -> {
                if(!hasCore(player)) return;
                Call.sendChatMessage(js(getPlayer(player)));
                Call.sendChatMessage(js("player.unit().spawnedByCore = true"));
                Call.sendChatMessage(js("player.unit(" + getUnit(unit) + ".spawn(player.team(), player.x, player.y))"));
                m_renderer.update();
            });
        };
        Runnable server = () -> {
            unit.select(false, true, false, (player, team, unit, amount) -> {
                if(!hasCore(player)) return;
                player.unit().spawnedByCore(true);
                player.unit(unit.spawn(player.team(), player.x, player.y));
                m_renderer.update();
            });
        };
        template(admins, js, server);
    }

    public static void changeEffect(){
        Runnable admins = () -> {
            ui.showInfoFade("@feature.jsonly");
        };
        Runnable js = () -> {
            effect.select(true, true, false, (player, team, effect, amount) -> {
                Call.sendChatMessage(js(getPlayer(player)));
                if(amount.get() == 0) Call.sendChatMessage(js("player.unit().unapply(" + getEffect(effect) + ")"));
                else Call.sendChatMessage(js("player.unit().apply(" + getEffect(effect) + ", " + amount.get() + ")"));
            });
        };
        Runnable server = () -> {
            effect.select(true, true, false, (player, team, effect, amount) -> {
                if(amount.get() == 0) player.unit().unapply(effect);
                else player.unit().apply(effect, amount.get());
            });
        };
        template(admins, js, server);
    }

    public static void changeItem(){
        Runnable admins = () -> {
            item.select(true, false, false, (player, team, item, amount) -> {
                if(!hasCore(player)) return;
                Call.sendChatMessage("/give " + item.id + " " + fix(item, (int)amount.get()));
            });
        };
        Runnable js = () -> {
            item.select(true, false, true, (player, team, item, amount) -> {
                if(!hasCore(player)) return;
                Call.sendChatMessage(js(getTeam(team) + ".core().items.add(" + getItem(item) + ", " + fix(item, (int)amount.get())) + ")");
            });
        };
        Runnable server = () -> {
            item.select(true, false, true, (player, team, item, amount) -> {
                if(!hasCore(player)) return;
                team.core().items.add(item, fix(item, (int)amount.get()));
            });
        };
        template(admins, js, server);
    }

	public static void changeTeam(){
        Runnable admins = () -> {
            team.select((player, team) -> {
                Call.sendChatMessage("/team " + team.id + " " + player.name);
            });
        };
        Runnable js = () -> {
            team.select((player, team) -> {
                Call.sendChatMessage(js(getPlayer(player)));
                Call.sendChatMessage(js("player.team(" + getTeam(team) + ")"));
            });
        };
        Runnable server = () -> {
            team.select((player, team) -> {
                player.team(team);
            });
        };
        template(admins, js, server);
    }

	public static void placeCore(){
        Runnable admins = () -> {
            Call.sendChatMessage("/core small");
        };
        Runnable js = () -> {
            Call.sendChatMessage(js(getPlayer(player)));
            Call.sendChatMessage(js("var tile = Vars.world.tiles.get(player.tileX(), player.tileY())"));
            Call.sendChatMessage(js("if(tile != null){ tile.setNet(tile.block() != Blocks.coreShard ? Blocks.coreShard : Blocks.air, player.team(), 0) }"));
        };
        Runnable server = () -> {
            var tile = world.tiles.get(player.tileX(), player.tileY());
            if(tile != null) tile.setNet(tile.block() != Blocks.coreShard ? Blocks.coreShard : Blocks.air, player.team(), 0);
        };
        template(admins, js, server);
    }

    public static void lookAt(){
    	player.unit().lookAt(input.mouseWorld());
    }

    public static void teleport(Vec2 pos){
        player.set(pos);
        player.unit().set(pos);
    }

    public static void kill(Player ppl){
        Runnable admins = () -> {
            Call.sendChatMessage("/despawn " + ppl.id);
        };
        Runnable js = () -> {
            Call.sendChatMessage(js(getPlayer(ppl)));
            Call.sendChatMessage(js("player.unit().spawnedByCore = true"));
            Call.sendChatMessage(js("player.clearUnit()"));
        };
        Runnable server = () -> {
            ppl.unit().spawnedByCore(true);
            ppl.clearUnit();
        };
        template(admins, js, server);
    }

    public static void spawnUnit(){
        Runnable admins = () -> {
            unit.select(true, false, true, (player, team, unit, amount) -> {
                if(!hasCore(player)) return;
                Call.sendChatMessage("/spawn " + unit.id + " " + (int)amount.get() + " " + team.id);
                m_renderer.update();
            });
        };
        Runnable js = () -> {
            unit.select(true, true, true, (player, team, unit, amount) -> {
                if(!hasCore(player)) return;
                Call.sendChatMessage(js(getPlayer(player)));
                Call.sendChatMessage(js("var unit = " + getUnit(unit)));
                Call.sendChatMessage(js("for(var i = 0; i < " + amount.get() + "; i++) unit.spawn(" + getTeam(team) + ", player.x, player.y)"));
                m_renderer.update();
            });
        };
        Runnable server = () -> {
            unit.select(true, true, true, (player, team, unit, amount) -> {
                if(!hasCore(player)) return;
                for (int i = 0; i < amount.get(); i++) unit.spawn(player.team(), player.x, player.y);
                m_renderer.update();
            });
        };
        template(admins, js, server);
    }

    public static void edit(int sx, int sy, int ex, int ey){
        Runnable admins = () -> {
            tile.select(false, (floor, block, overlay) -> {
                String base = "/fill " + (ex - sx + 1) + " " + (ey - sy + 1) + " ";
                if (floor != null) Call.sendChatMessage(base + floor.id);
                if (block != null) Call.sendChatMessage(base + block.id);
                if (overlay != null) Call.sendChatMessage(base + overlay.id);
            });
        };
        Runnable js = () -> {
            tile.select(false, (floor, block, overlay) -> {
                Call.sendChatMessage(js("var floor = " + getBlock(floor)));
                Call.sendChatMessage(js("var block = " + getBlock(block)));
                Call.sendChatMessage(js("var overlay = " + getBlock(overlay)));
                Call.sendChatMessage(js("var setb = (tile) => { if(block != null) tile.setNet(block) }"));
                Call.sendChatMessage(js("var setf = (tile) => { tile.setFloorNet(floor==null?tile.floor():floor.asFloor(),overlay==null?tile.overlay():overlay.asFloor());setb(tile) }"));
                Call.sendChatMessage(js("var nulc = (tile) => { if(tile != null) setf(tile) }"));
                Call.sendChatMessage(js("var todo = (x, y) => { nulc(tile = Vars.world.tiles.get(x, y)) }"));
                Call.sendChatMessage(js("for(var x = " + sx + "; x <= " + ex + "; x++){ for(var y = " + sy + "; y <= " + ey + "; y++){ todo(x, y) } }"));
            });
        };
        Runnable server = () -> {
            tile.select(false, (floor, block, overlay) -> {
                for(int x = sx; x <= ex; x++){
                    for(int y = sy; y <= ey; y++){
                        Tile tile = world.tiles.get(x, y);
                        if(tile == null) continue;

                        tile.setFloorNet(floor == null ? tile.floor() : floor, overlay == null ? tile.overlay() : overlay);
                        if(block != null) tile.setNet(block);
                    }
                }
            });
        };
        template(admins, js, server);
    }


    // helpfull methods
    private static int fix(Item item, int amount){
        var items = player.team().core().items;
        return amount == 0 ? -items.get(item) : (items.get(item) + amount < 0 ? -items.get(item) : amount);
    }

    private static boolean hasCore(Player ppl){
        boolean has = ppl.core() != null;
        if(!has) ui.showInfoFade("@nocore");
        return has;
    }


    // js helpfull methods
    private static String js(String code){
        return "/js " + code;
    }

    private static String getPlayer(Player ppl){
        return "var player = Groups.player.getByID(" + ppl.id + ")";
    }

    private static String getUnit(UnitType unit){
        return "Vars.content.unit(" + unit.id + ")";
    }

    private static String getEffect(StatusEffect effect){
        return "Vars.content.statusEffects().get(" + effect.id + ")";
    }

    private static String getItem(Item item){
        return "Vars.content.item(" + item.id + ")";
    }

    private static String getBlock(Block block){
        return block == null ? "null" : "Vars.content.block(" + block.id + ")";
    }

    private static String getTeam(Team team){
        return "Team." + team.name;
    }
}
