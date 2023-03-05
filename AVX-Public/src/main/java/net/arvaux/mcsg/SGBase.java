package net.arvaux.mcsg;

import net.arvaux.core.Main;
import net.arvaux.core.cmd.CommandHelpCenter;
import net.arvaux.core.cmd.GameCommand;
import net.arvaux.core.ess.CommandSpawnpoint;
import net.arvaux.core.entity.player.GamePlayer;
import net.arvaux.core.group.Group;
import net.arvaux.core.module.Module;
import net.arvaux.core.module.PluginModule;
import net.arvaux.core.server.ServerSQL;
import net.arvaux.core.util.*;
import net.arvaux.core.util.org.bukkit.event.server.ServerSwitchModuleEvent;
import net.arvaux.core.ess.GameState;
import net.arvaux.mcsg.ui.UISponsor;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SGBase extends PluginModule implements Listener {

    public static int DM_SIZE = 3;
    public static int MIN_PLAYERS;
    public static int MAX_PLAYERS = 24;
    public static boolean FORCE_LOBBY;
    public static boolean FORCE_DM;
    public static boolean FORCE_STOP;
    public static boolean VOTING;
    public static String LIVE_MAP = null;
    public static boolean GAME_RUNNING;
    public static SGTimeManager TIME_MANAGER;
    public static SGBountyManager BOUNTY_MANAGER;

    public static List<Location> GAME_SPAWN;
    public static List<Location> DM_SPAWN;
    public static List<Location> T1;
    public static List<Location> T2;
    public static List<Location> OPENED_CHEST_LOC;
    public static List<String> OPENED_CHEST_INV;
    public static List<String> PASSED_TRIBUTES;
    private static VoteManager _vm;
    private static SGMapPool _mapPool;
    public static int RADIUS;
    public static int DMX;
    public static int DMZ;

    public static void join(GamePlayer player) {
        player.bukkit().teleport(CommandSpawnpoint.getWaitingpoint());
        SGScoreboard.send(player);
        player.bukkit().setGameMode(GameMode.ADVENTURE);
        player.setHealth(20);
        player.bukkit().setFoodLevel(20);
        player.resetInventory();
        if (GameState.isState(GameState.LOBBY)) {
            GameState.setPlaying(player);
            player.bukkit().teleport(CommandSpawnpoint.getWaitingpoint());
            if (Bukkit.getServer().getOnlinePlayers().size() > SGBase.MAX_PLAYERS) {
                if (player.hasGroup(Group.REGULAR)) {
                    player.bukkit().kickPlayer("§cThis server is full!");
                } else {
                    GamePlayer kick = null;
                    int i = 0;
                    Player[] players = (Player[]) Bukkit.getOnlinePlayers().toArray();
                    while (kick == null && i < players.length) {
                        if (!(players[i].hasPermission("gm.ninox"))) {
                            kick = new GamePlayer(players[i].getUniqueId());
                        }
                        i++;
                    }
                    if (kick != null) {
                        kick.bukkit().kickPlayer("§cYou have been kicked to make room for a higher ranking player.");
                    }
                }
            }
        } else {
            M.next(player, "server sg");
            GameState.setSpectating(player);
            player.bukkit().teleport(
                    new GamePlayer(
                            GameState.PLAYING.get(0)).bukkit().getLocation().add(0, 40, 0));
        }
    }

    @Override
    public void boot() {
        this.getCommandManager().registerCommands(new CommandDM());
        this.getPluginManager().registerEvents(new UISponsor(), Main.getInstance());
        this.getPluginManager().registerEvents(this, Main.getInstance());
        this.getPluginManager().registerEvents(new EventMCSG(), Main.getInstance());
        this.getPluginManager().registerEvents(new EventJoinQuit(), Main.getInstance());
        this.switchModule(new ServerSwitchModuleEvent(Module.getModule(),
                Module.getModuleS(Main.getInstance().config.getString("last-module"))));
    }

    @Override
    public Module m() {
        return Module.MCSG;
    }

    public static VoteManager get_vm() {
        return SGBase._vm;
    }

    public static void mapWon() {
        SGBase.VOTING = false;

        for (Player players : Bukkit.getOnlinePlayers()) {
            GamePlayer server = new GamePlayer(players.getUniqueId());

            String map = SGBase.get_vm().getWinningMap();
            SGBase.LIVE_MAP = map;

            server.send(M.MCSG + "§3The map §b§l" + SGBase.getMapNameOf(map) + "§3 was chosen!");

            server.send(Sound.ITEM_PICKUP);
        }
    }

    //This is to setup the radius for the deathmatch lightning!
    public static void setupRadius(){
        if(C.SG_DATA_CONFIG.getString("maps.dm-world.center") != null) {
            DMX = C.SG_DATA_CONFIG.getInt("maps.dm-world.center.x");
            DMZ = C.SG_DATA_CONFIG.getInt("maps.dm-world.center.z");
            RADIUS = C.SG_DATA_CONFIG.getInt("maps.dm-world.center.radius") + 25;
        }
    }

    public static void mapInfo() {
        String author = C.SG_DATA_CONFIG.getString("maps." + SGBase.LIVE_MAP + ".author");
        String link = C.SG_DATA_CONFIG.getString("maps." + SGBase.LIVE_MAP + ".link");
        if (author == null)
            author = "Arvaux Associate";
        if (link == null)
            link = M.FORUM_LINK;
        //M.mapInfo(SGBase.getMapNameOf(SGBase.LIVE_MAP), author, link);
    }

    public static String getMapNameOf(String name) {
        return name.replaceAll("_", " ");
        // todo return C.SG_DATA_CONFIG.getString("maps." + name + ".name");
    }

    public static void setupSpawns() {
        String name = "SG-" + SGBase.LIVE_MAP;
        if(C.SG_DATA_CONFIG.getString("maps." + SGBase.LIVE_MAP + ".sp") != null) {
            for(String id : C.SG_DATA_CONFIG.getConfigurationSection("maps." + SGBase.LIVE_MAP + ".sp").getKeys(false)){
                int gx = C.SG_DATA_CONFIG.getInt("maps." + SGBase.LIVE_MAP + ".sp." + id + ".x");
                int gy = C.SG_DATA_CONFIG.getInt("maps." + SGBase.LIVE_MAP + ".sp." + id + ".y");
                int gz = C.SG_DATA_CONFIG.getInt("maps." + SGBase.LIVE_MAP + ".sp." + id + ".z");
                Float yaw = Float.parseFloat(C.SG_DATA_CONFIG.getString("maps." + SGBase.LIVE_MAP + ".sp." + id + ".yaw"));
                Float pitch = (float) 0.0;
                Location gs = new Location(Bukkit.getWorld(name), M.getSpawnLoc(gx), gy, M.getSpawnLoc(gz), yaw, pitch);
                SGBase.GAME_SPAWN.add(gs);
            }
        }
        // dm
        if(C.SG_DATA_CONFIG.getString("maps.dm-world.sp.1") != null) {
            for(String id : C.SG_DATA_CONFIG.getConfigurationSection("maps.dm-world.sp").getKeys(false)){
                if(M.isInt(id)) {
                    int dx = C.SG_DATA_CONFIG.getInt("maps.dm-world.sp." + id + ".x");
                    int dy = C.SG_DATA_CONFIG.getInt("maps.dm-world.sp." + id + ".y");
                    int dz = C.SG_DATA_CONFIG.getInt("maps.dm-world.sp." + id + ".z");
                    Float yaw = Float.parseFloat(C.SG_DATA_CONFIG.getString("maps.dm-world.sp." + id + ".yaw"));
                    Float pitch = (float) 0.0;
                    Location ds = new Location(Bukkit.getWorld("dm-world"),
                            M.getSpawnLoc(dx), dy, M.getSpawnLoc(dz), yaw, pitch);
                    SGBase.DM_SPAWN.add(ds);
                }
            }
        }
    }

    public static void changeChests() {
        World world = Bukkit.getWorld("SG-" + SGBase.LIVE_MAP);
        for(Chunk chunk : world.getLoadedChunks()) {
            for(BlockState block : chunk.getTileEntities()) {
                if(block.getType() == Material.ENDER_CHEST) {
                    T2.add(block.getBlock().getLocation());
                    block.getBlock().setType(Material.CHEST);
                    block.setData(block.getData());
                    block.update();
                }
            }
        }
    }

    public static void setupChests() {
        String name = "SG-" + SGBase.LIVE_MAP;
        for(Chunk chunk : Bukkit.getWorld(name).getLoadedChunks()) {
            for(BlockState chest : chunk.getTileEntities()) {
                if(chest instanceof Chest) {
                    Inventory contents = ((Chest) chest).getInventory();
                    if(!SGBase.OPENED_CHEST_LOC.contains(chest.getLocation())) {
                        SGBase.OPENED_CHEST_LOC.add(chest.getLocation());
                        int size = contents.getSize() - 1;
                        if (chest.getType() == Material.CHEST) {
                            contents.clear(); if (size == 53) {
                                if (SGBase.T2.contains(chest.getBlock().getLocation())) {
                                    int no = M.getRandomInt(9, 12);
                                    for (int i = 0; i < no; i++) {
                                        int loc = M.getRandomInt(0, size);
                                        ItemStack item = SGChestManager.getRandomT2Item(M.getRandomInt(0, 4));
                                        if(!contents.contains(item)) {
                                            contents.setItem(loc, item);
                                        } else i--;
                                    }
                                } else {
                                    SGBase.T1.add(chest.getBlock().getLocation());
                                    int no = M.getRandomInt(8, 10);
                                    for (int i = 0; i < no; i++) {
                                        int loc = M.getRandomInt(0, size);
                                        ItemStack item = SGChestManager.getRandomT1Item(M.getRandomInt(0, 4));
                                        if(!contents.contains(item)) {
                                            contents.setItem(loc, item);
                                        } else i--;
                                    }
                                }
                            } else {
                                if (SGBase.T2.contains(chest.getBlock().getLocation())) {
                                    int no = M.getRandomInt(5, 8);
                                    for (int i = 0; i < no; i++) {
                                        int loc = M.getRandomInt(0, size);
                                        ItemStack item = SGChestManager.getRandomT2Item(M.getRandomInt(0, 4));
                                        if(!contents.contains(item)) {
                                            contents.setItem(loc, item);
                                        } else i--;
                                    }
                                } else {
                                    SGBase.T1.add(chest.getBlock().getLocation());
                                    int no = M.getRandomInt(5, 7);
                                    for (int i = 0; i < no; i++) {
                                        int loc = M.getRandomInt(0, size);
                                        ItemStack item = SGChestManager.getRandomT1Item(M.getRandomInt(0, 4));
                                        if(!contents.contains(item)) {
                                            contents.setItem(loc, item);
                                        } else i--;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        /*for (Location loc1 : T1) {
            UtilBlock.sendBlockChangePacket(loc2, UtilBlock.getBlockData(Material.ARMOR_STAND, (byte) 0));
        }
        for (Location loc2 : T2) {
            UtilBlock.sendBlockChangePacket(loc2, UtilBlock.getBlockData(Material.ARMOR_STAND, (byte) 0));
        }
        for (Location loc : SGBase.OPENED_CHEST_LOC) {
           // Main.BLOCK_DISGUISER.setTranslatedBlock(loc, 54, 33);

        }*/
    }

    public static void start() {
        GameState.setState(GameState.LOBBY);
        ServerSQL.add_info(Main.getInstance().getServerID().getName(), GameState.getState().name(), "GAMESTATE");
        SGBase.GAME_RUNNING = false;
        SGBase.MAX_PLAYERS = 24; // todo check for max players
        SGBase.MIN_PLAYERS = 8;
        //SGBase.SIZE_MATTERS = true;
        SGBase.TIME_MANAGER = new SGTimeManager();
        SGBase.BOUNTY_MANAGER = new SGBountyManager();
        SGBase.BOUNTY_MANAGER.setupBountyManager();
        SGBase.FORCE_LOBBY = false;
        SGBase.FORCE_DM = false;
        SGBase.VOTING = true;
        SGBase.FORCE_STOP = false;
        SGBase.T1 = new ArrayList<>();
        SGBase.T2 = new ArrayList<>();
        SGBase.GAME_SPAWN = new ArrayList<>();
        SGBase.DM_SPAWN = new ArrayList<>();
        SGBase.PASSED_TRIBUTES = new ArrayList<>();
        SGBase.OPENED_CHEST_LOC = new ArrayList<>();
        SGBase.OPENED_CHEST_INV = new ArrayList<>();
        SGBase._vm = new VoteManager();
        SGBase._mapPool = new SGMapPool(SGBase._vm);
        SGBase._mapPool.setupMapPool();

        SGBase.RADIUS = 0;
        if (!Module.isModule(Module.MCSG))
            return;
        SGBase.TIME_MANAGER.run();
        for (Player players : Bukkit.getOnlinePlayers()) {
            SGBase.join(new GamePlayer(players.getUniqueId()));
        }
        WorldManager.copyWorld(
                "plugins/" + Main.getInstance().getDescription().getName() + "/sg/maps/dm-world",
                "", "dm-world");
    }

    public static void stop() {
        if (SGBase.TIME_MANAGER != null)
            SGBase.TIME_MANAGER.cancel();
        SGBase.T1 = null;
        SGBase.T2 = null;
        SGBase.GAME_SPAWN = null;
        SGBase.DM_SPAWN = null;
        SGBase.PASSED_TRIBUTES = null;
        SGBase.OPENED_CHEST_LOC = null;
        SGBase.OPENED_CHEST_INV = null;
        SGBase._vm = null;
        SGBase._mapPool = null;
        for (Player players : Bukkit.getOnlinePlayers()) {
            GamePlayer v = new GamePlayer(players.getUniqueId());
            //v.resetInventory();
            v.removeScoreboard();
            players.teleport(CommandSpawnpoint.getWaitingpoint());
        }
        WorldManager.deleteWorld("", "SG-" + SGBase.LIVE_MAP);
        WorldManager.deleteWorld("", "dm-world");
        SGBase.LIVE_MAP = null;
        SGBase.TIME_MANAGER = null;
        SGBase.BOUNTY_MANAGER = null;
    }

    @EventHandler
    public void switchModule(ServerSwitchModuleEvent event) {
        if (event.getModule() == Module.MCSG) {
            SGBase.start();
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.teleport(CommandSpawnpoint.getWaitingpoint());
            }
            for (Player player : Bukkit.getOnlinePlayers()) {
                SGBase.join(new GamePlayer(player.getUniqueId()));
            }
        }
    }

    @Override
    public void quit() {
        SGBase.stop();
        for (Player players : Bukkit.getOnlinePlayers()) {
            GamePlayer v = new GamePlayer(players.getUniqueId());
            players.teleport(CommandSpawnpoint.getSpawnpoint());
            if (v.isVisibility() == false) {
                v.setVisibility(true, false);
                v.send(M.t(M.GAME, "Player Visibility", true));
            }
        }
    }

    public static void removeTribute(Player player, Entity cause, boolean onDeath){
        if(onDeath) {
            if(GameState.PLAYING.contains(player.getUniqueId())) {
                GameState.removePlaying(new GamePlayer(player.getUniqueId()));
                GameState.setSpectating(new GamePlayer(player.getUniqueId()));
                SGBase.PASSED_TRIBUTES.add(new GamePlayer(player.getUniqueId()).getDisplayName());
            }
        }

        //drop items
        for (ItemStack items : player.getInventory()) {
            if (items != null)
                Bukkit.getWorld("SG-" + SGBase.LIVE_MAP).dropItemNaturally(player.getLocation(), items);
        }

        for (ItemStack items : player.getInventory().getArmorContents()) {
            if (items != null)
                Bukkit.getWorld("SG-" + SGBase.LIVE_MAP).dropItemNaturally(player.getLocation(), items);
        }

        if(GameState.isState(GameState.PREGAME)
                || GameState.isState(GameState.INGAME)
                || GameState.isState(GameState.DM)
                || GameState.isState(GameState.PREDM)) {

            //if(GameState.PLAYING.contains(player.getUniqueId())) {
            int victimPoints = SGSQL.getStat(player.getUniqueId(), "points");
            int pointsOnLot = victimPoints/20;

                // remove point stat
                SGSQL.add_stat(player.getUniqueId(), victimPoints-pointsOnLot, "points");
                M.lossPoints(new GamePlayer(player.getUniqueId()),
                        "Survival Games", pointsOnLot, victimPoints-pointsOnLot);

                if(cause instanceof Player && cause.getUniqueId() != player.getUniqueId() && cause != null) {
                    Player cause1 = (Player) cause;
                    int killerPoints = SGSQL.getStat(cause1.getUniqueId(), "points");
                    int pointsAdded = pointsOnLot < 5 ? 5 : pointsOnLot;

                    // add point stat
                    // could the problem be here then?
                    SGSQL.add_stat(cause1.getUniqueId(), killerPoints + pointsAdded, "points");

                    M.gainPoints(new GamePlayer(cause1.getUniqueId()),
                            "Survival Games", pointsAdded, killerPoints+pointsAdded);

                    // add kill stat
                    SGSQL.add_stat(cause1.getUniqueId(),
                            SGSQL.getStat(cause1.getUniqueId(), "kills") + 1, "kills");

                    // bounty stat
                    if (SGBase.BOUNTY_MANAGER.getBounty(player.getUniqueId()) != 0) {

                        int bounty = SGBase.BOUNTY_MANAGER.getBounty(player.getUniqueId());
                        SGSQL.add_stat(cause1.getUniqueId(), killerPoints + bounty, "points");
                        cause1.sendMessage(M.MCSG + "§a§l+ " + bounty + " §3extra points from the §c§lbounty§3 over "
                                + new GamePlayer(player.getUniqueId()).getDisplayGroup().getColor() + "§l"
                                + new GamePlayer(player.getUniqueId()).getDisplayName() + "!");

                        for (Player server : Bukkit.getOnlinePlayers()) {
                            server.sendMessage(M.MCSG + "§3A §c§lbounty §3of §e§l" + bounty + " §3has been claimed from "
                                    + new GamePlayer(player.getUniqueId()).getDisplayGroup().getColor() + "§l"
                                    + new GamePlayer(player.getUniqueId()).getDisplayName() + "§3's death!");
                            new GamePlayer(server.getUniqueId()).send(Sound.ORB_PICKUP);
                        }
                    }
               SGBase.BOUNTY_MANAGER.removeBounty(player.getUniqueId());
            }
            // add games played
            SGSQL.add_stat(player.getUniqueId(), SGSQL.getStat(player.getUniqueId(), "played") + 1,
                    "played");
        }
        for(Player p : Bukkit.getOnlinePlayers()) {
            //if(GameState.PLAYING.contains(p.getUniqueId())) {
                int pX = player.getLocation().getBlockX();
                int tX = p.getLocation().getBlockX();
                int pZ = p.getLocation().getBlockZ();
                int tZ = player.getLocation().getBlockZ();
                if (!(tX > pX + 150 || tX < pX - 150 || tZ > pZ + 150 || tZ < pZ - 150)) {
                    //p.sendMessage(MessageManager.c("&6A cannon can be heard in the distance in memorial of " + player.getDisplayName() + "&8."));
                }
            //} //else p.sendMessage(MessageManager.c("&6A cannon can be heard in the distance in memorial of " + player.getDisplayName() + "&8."));
        }
        if(GameState.isState(GameState.INGAME)) {
            if(GameState.PLAYING.size() < SGBase.DM_SIZE+1){
                if(SGBase.TIME_MANAGER.inGame > 60){
                    SGBase.TIME_MANAGER.inGame = 60;
                }
            }
        }
        if (!(GameState.isState(GameState.LOBBY))) {
            if (GameState.PLAYING.size() < 2) {
                if (SGBase.LIVE_MAP != null)
                    Bukkit.getWorld("SG-" + SGBase.LIVE_MAP).setTime(18000);
                if (GameState.PLAYING.size() != 0) {
                    SGBase.setWinner(new GamePlayer(GameState.PLAYING.get(0)));
                    GamePlayer winner = new GamePlayer(GameState.PLAYING.get(0));

                    // add games played
                    SGSQL.add_stat(winner.getUUID(), SGSQL.getStat(winner.getUUID(), "played") + 1,
                            "played");

                    int bounty = SGBase.BOUNTY_MANAGER.getBounty(player.getUniqueId());

                    if(SGBase.BOUNTY_MANAGER.isBountied(winner.getUUID())) {
                        if(SGBase.BOUNTY_MANAGER.getBounty(winner.getUUID()) != 0){
                             winner.send(M.MCSG + "§a§l+ " + bounty + " §3extra points from the §c§lbounty§3 over your head!");
                            SGSQL.add_stat(winner.getUUID(), SGSQL.getStat(winner.getUUID(), "points") + bounty, "points");
                         }
                     }
                }
                GameState.setState(GameState.POSTGAME);
                ServerSQL.add_info(Main.getInstance().getServerID().getName(), GameState.getState().name(), "GAMESTATE");
                //UI.updateHubGUI();
            }
            if (onDeath) {

            }
            M.next(new GamePlayer(player.getUniqueId()), "server sg");
            GameState.giveSpectatingItems(new GamePlayer(player.getUniqueId()));
            new GamePlayer(player.getUniqueId()).send(5, 60, 20,
                    "§c§lDEFEAT!", "§3The odds were §c§lnot §3in your favor...");
        }
    }

    public static void setWinner(GamePlayer player) {
        M.winner(player.getDisplayGroup().getColor() + "§l" + player.getDisplayName(), "Survival Games");

        SGSQL.add_stat(player.getUUID(), SGSQL.getStat(player.getUUID(), "won") + 1, "won");

        //Set winner
        //if(SGBase.getHighestKills() != null) {
        //Player winner = Bukkit.getPlayer(GameState.PLAYING.get(M.getRandomInt(0,
        //        GameState.PLAYING.size()-1)));
        Bukkit.getWorld("SG-" + SGBase.LIVE_MAP).setTime(18000);
        Bukkit.getWorld("dm-world").setTime(18000);

        //UI.updateHubGUI();

        player.send(5, 60, 20,
                "§3§lVICTORY!",
                "");
        M.next(player, "server sg");

    }

    public static void sendVotingCommand(GameCommand command) {
        if (SGBase.VOTING == true) {

            if (command.getArgs().length == 0) {

                int totalVotes = 0;
                for (int id = 0; id < SGBase.get_vm().getMaps().size(); id++) {
                    totalVotes = totalVotes + SGBase.get_vm().getVotes().get(SGBase.get_vm().getMaps().get(id));
                }

                StringBuilder previousMaps = new StringBuilder();
                int id = 0;
                command.getPlayer().send(M.HEADER);
                for (String map : SGBase.get_vm().getMaps()) {
                    int voteNumber = id + 1;
                    int votes = SGBase.get_vm().getVotes().get(SGBase.get_vm().getMaps().get(id));
                    String chance = M.getChance(votes, totalVotes);
                    if (votes == 1) {
                        command.getPlayer().send("§7# §b" + voteNumber + " §8| §b§l" + votes + " vote §8» §e§l"
                                        + SGBase.getMapNameOf(map) +  " §7(" + chance + "%)",
                                "§7Vote for " + SGBase.getMapNameOf(map) + "!", "vote " + voteNumber);
                    } else {
                        command.getPlayer().send("§7# §b" + voteNumber + " §8| §b§l" + votes + " votes §8» §e§l"
                                        + SGBase.getMapNameOf(map) +  " §7(" + chance + "%)",
                                "§7Vote for " + SGBase.getMapNameOf(map) + "!", "vote " + voteNumber);
                    }
                    id++;
                }
                command.getPlayer().send(M.HEADER);
            } else if (command.getArgs().length == 1) {
                if (GameState.isState(GameState.LOBBY)) {
                    if (M.isInt(command.getArgs(0))) {
                        if (Integer.parseInt(command.getArgs(0)) == 1
                                || Integer.parseInt(command.getArgs(0)) == 2
                                || Integer.parseInt(command.getArgs(0)) == 3
                                || Integer.parseInt(command.getArgs(0)) == 4
                                || Integer.parseInt(command.getArgs(0)) == 5) {
                            if ((SGBase.get_vm().getVoted().get(command.getPlayer().getUUID())) == null) {
                                int RealNumber = Integer.valueOf(command.getArgs(0));
                                int Number = RealNumber - 1;
                                String map = SGBase.get_vm().getMaps().get(Number);
                                SGBase.get_vm().addVote(command.getPlayer(), map);
                                int votes = SGBase.get_vm().getVotes().get(SGBase.get_vm().getMaps().get(Number));
                                if (votes == 1) {
                                    command.getPlayer().send(M.MCSG + "§3You chose §b§l" + SGBase.getMapNameOf(map) + " §3with §b§l" + votes + " vote!");
                                } else {
                                    command.getPlayer().send(M.MCSG + "§3You chose §b§l" + SGBase.getMapNameOf(map) + " §3with §b§l" + votes + " votes!");
                                }
                            } else command.getPlayer().send(M.MCSG + "§cYou already voted for a map!");
                        } else command.getPlayer().send(M.MCSG + "§cThat is not a voting option!");
                    } else command.getPlayer().send(M.MCSG + "§cInvalid vote!");
                } else command.getPlayer().send(M.GAME_RUNNING);
            }


            CommandHelpCenter help = command.getHelp();
            help.add("[1-5]", "Chooses a map");
            return;

        } else {
            command.getPlayer().send(M.MCSG + "§cVoting period has ended.");
        }
    }

}
