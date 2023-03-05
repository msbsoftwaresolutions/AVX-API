package net.arvaux.mcbw;

import net.arvaux.core.Main;
import net.arvaux.core.cmd.CommandHelpCenter;
import net.arvaux.core.cmd.GameCommand;
import net.arvaux.core.cuboid.EventCuboid;
import net.arvaux.core.ess.CommandSpawnpoint;
import net.arvaux.core.ess.EventDeathMessage;
import net.arvaux.core.ess.GameState;
import net.arvaux.core.entity.player.GamePlayer;
import net.arvaux.core.group.Group;
import net.arvaux.core.hologram.Hologram;
import net.arvaux.core.module.Module;
import net.arvaux.core.module.PluginModule;
import net.arvaux.core.server.ServerSQL;
import net.arvaux.core.util.*;
import net.arvaux.core.util.org.bukkit.event.server.ServerSwitchModuleEvent;
import net.arvaux.mcbw.gen.Generator;
import net.arvaux.mcbw.gen.GeneratorTier;
import net.arvaux.mcbw.gen.GeneratorType;
import net.arvaux.mcbw.island.Island;
import net.arvaux.mcbw.island.IslandColor;
import net.arvaux.mcbw.island.IslandUpgrade;
import net.arvaux.mcbw.player.ArmorUpgradeLevel;
import net.arvaux.mcbw.player.AxeUpgradeLevel;
import net.arvaux.mcbw.player.BedwarsPlayer;
import net.arvaux.mcbw.player.PickaxeUpgradeLevel;
import net.arvaux.mcbw.tasks.PlayerRespawnTask;
import net.arvaux.mcbw.ui.MainShopStoryboard;
import net.arvaux.mcbw.ui.CategoriesStoryboard;
import net.arvaux.mcbw.ui.UpgradeStoryboard;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.material.Bed;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.stream.Collectors;

public class BWBase extends PluginModule implements Listener {

    public static int MIN_PLAYERS;
    public static int MAX_PLAYERS = 8;
    public static boolean FORCE_LOBBY;
    //public static boolean SIZE_MATTERS;
    public static boolean FORCE_DM;
    public static boolean FORCE_STOP;
    public static boolean VOTING;
    private static VoteManager _vm;
    private static BWMapPool _mapPool;
    public static String LIVE_MAP;
    public static boolean GAME_RUNNING;
    public static BWTimerManager TIME_MANAGER;
    public static List<Generator> DIAMOND_GEN_LIST;
    public static List<Generator> EMERALD_GEN_LIST;
    public static List<UUID> RESPAWNING_TIMER;
    public static int MAX_TEAM_SIZE = 1;
    public static BlockManager BLOCKS;

    public static List<Island> ISLANDS;
    public static int RADIUS;
    public static List<Villager> SHOP_NPC_LIST;
    public static List<Villager> UPGRADE_NPC_LIST;
    public static List<Hologram> HOLO_LIST;

    public static Map<UUID, PickaxeUpgradeLevel> PICKAXE_LVL_MAP = new HashMap<>();
    public static Map<UUID, AxeUpgradeLevel> AXE_LVL_MAP = new HashMap<>();
    public static List<UUID> SHEAR_LIST = new ArrayList<>();
    public static Map<UUID, ArmorUpgradeLevel> ARMOR_MAP = new HashMap<>();

    public static void join(GamePlayer player) {
        BWScoreboard.send(player);
        player.bukkit().setGameMode(GameMode.ADVENTURE);
        if (GameState.isState(GameState.LOBBY)) {
            GameState.setPlaying(player);
            player.setHealth(20);
            player.bukkit().setFoodLevel(20);
            player.resetInventory();
            player.bukkit().teleport(CommandSpawnpoint.getWaitingpoint());
            if (Bukkit.getServer().getOnlinePlayers().size() > BWBase.MAX_PLAYERS) {
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
            Island playerIsland = BWBase.islandForPlayer(player);
            if (playerIsland != null) {
                if (playerIsland.isBedPlaced() == true) {
                    new BedwarsPlayer(player).setSpectatorMode(false);
                    BukkitTask task = Bukkit.getServer().getScheduler().runTaskTimer(Main.getInstance(), new PlayerRespawnTask(player, playerIsland), 0, 20);
                    Bukkit.getServer().getScheduler().runTaskLater(Main.getInstance(), task::cancel, 20 * 6);
                    return;
                }
            } else {
                M.next(player, "server bw");
                GameState.setSpectating(player);
                player.bukkit().teleport(
                        new GamePlayer(
                                GameState.PLAYING.get(0)).bukkit().getLocation().add(0, 40, 0));
            }
        }
    }

    public static void start() {
        GameState.setState(GameState.LOBBY);
        ServerSQL.add_info(Main.getInstance().getServerID().getName(), GameState.getState().name(), "GAMESTATE");
        BWBase.GAME_RUNNING = false;
        BWBase.MIN_PLAYERS = 6;
        BWBase.MAX_PLAYERS = 8;
        // BWBase.SIZE_MATTERS = true;
        BWBase.TIME_MANAGER = new BWTimerManager();
        BWBase.FORCE_LOBBY = false;
        BWBase.FORCE_DM = false;
        BWBase.VOTING = true;
        BWBase.FORCE_STOP = false;
        BWBase._vm = new VoteManager();
        BWBase._mapPool = new BWMapPool(BWBase._vm);
        BWBase._mapPool.setupMapPool();

        BWBase.ISLANDS = new ArrayList<Island>();
        BWBase.RADIUS = 0;
        BWBase.DIAMOND_GEN_LIST = new ArrayList<Generator>();
        BWBase.EMERALD_GEN_LIST = new ArrayList<Generator>();
        BWBase.RESPAWNING_TIMER = new ArrayList<UUID>();
        BWBase.UPGRADE_NPC_LIST = new ArrayList<Villager>();
        BWBase.SHOP_NPC_LIST = new ArrayList<Villager>();
        BWBase.HOLO_LIST = new ArrayList<Hologram>();

        BWBase.SHEAR_LIST = new ArrayList<UUID>();
        BWBase.AXE_LVL_MAP = new HashMap<UUID, AxeUpgradeLevel>();
        BWBase.PICKAXE_LVL_MAP = new HashMap<UUID, PickaxeUpgradeLevel>();
        BWBase.ARMOR_MAP = new HashMap<UUID, ArmorUpgradeLevel>();

        //BWBase._vm = new VoteManager();
        BWBase.BLOCKS = new BlockManager();

        BWBase.SHEAR_LIST.clear();
        BWBase.AXE_LVL_MAP.clear();
        BWBase.PICKAXE_LVL_MAP.clear();
        BWBase.ARMOR_MAP.clear();
        if (!Module.isModule(Module.MCBW))
            return;
        BWBase.TIME_MANAGER.run();
        for (Player players : Bukkit.getOnlinePlayers()) {
            BWBase.join(new GamePlayer(players.getUniqueId()));
        }
    }

    public static void stop() {
        if (BWBase.TIME_MANAGER != null)
            BWBase.TIME_MANAGER.cancel();
        BWBase.ISLANDS = null;
        BWBase.RADIUS = 0;
        BWBase.BLOCKS.reset();
        BWBase.BLOCKS = null;

        BWBase.SHEAR_LIST.clear();
        BWBase.AXE_LVL_MAP.clear();
        BWBase.PICKAXE_LVL_MAP.clear();
        BWBase.ARMOR_MAP.clear();

        BWBase.SHEAR_LIST = null;
        BWBase.AXE_LVL_MAP = null;
        BWBase.PICKAXE_LVL_MAP = null;
        BWBase.ARMOR_MAP = null;

        //BWBase.DIAMOND_GEN_LIST.stream().forEach(gen -> gen.setActivated(false));
        //BWBase.EMERALD_GEN_LIST.stream().forEach(gen -> gen.setActivated(false));

        BWBase.DIAMOND_GEN_LIST = null;
        BWBase.EMERALD_GEN_LIST = null;
        BWBase.RESPAWNING_TIMER = null;
        BWBase._vm = null;
        BWBase._mapPool = null;

        for (Player players : Bukkit.getOnlinePlayers()) {
            GamePlayer v = new GamePlayer(players.getUniqueId());
            v.resetInventory();
            v.removeScoreboard();
            players.teleport(CommandSpawnpoint.getWaitingpoint());
        }
        BWBase.UPGRADE_NPC_LIST.stream().forEach(npc -> npc.remove());
        BWBase.SHOP_NPC_LIST.stream().forEach(npc -> npc.remove());
        BWBase.HOLO_LIST.stream().forEach(holo -> holo.remove());
        BWBase.UPGRADE_NPC_LIST = null;
        BWBase.SHOP_NPC_LIST = null;
        BWBase.HOLO_LIST = null;

        WorldManager.deleteWorld("", "BW-" + BWBase.LIVE_MAP);

        BWBase.LIVE_MAP = null;
        BWBase.TIME_MANAGER = null;
    }

    public static void death(GamePlayer player, Island playerIsland, boolean manualDeath, boolean offline) {
        if (!BWBase.RESPAWNING_TIMER.contains(player.getUUID())) {
            player.resetInventory();

            BWBase.RESPAWNING_TIMER.add(player.getUUID());
            if (playerIsland.isBedPlaced() == true) {

                new BedwarsPlayer(player).setSpectatorMode(false);

                BukkitTask task = Bukkit.getServer().getScheduler().runTaskTimer(
                        Main.getInstance(), new PlayerRespawnTask(player, playerIsland), 0, 20);
                Bukkit.getServer().getScheduler().runTaskLater(Main.getInstance(), task::cancel, 20 * 6);

                if (EventDeathMessage.LAST_DAMAGE_MAP.get(player.getUUID()) != null) {
                    // add kill stat
                    BWSQL.add_stat(EventDeathMessage.LAST_DAMAGE_MAP.get(player.getUUID()),
                            BWSQL.getStat(EventDeathMessage.LAST_DAMAGE_MAP.get(player.getUUID()),
                                    "nonfinalkills") + 1, "nonfinalkills");
                }
                BWSQL.add_stat(player.getUUID(),
                        BWSQL.getStat(player.getUUID(), "nonfinaldeaths") + 1, "nonfinaldeaths");

            } else {
                player.bukkit().getWorld().strikeLightningEffect(player.bukkit().getLocation());

                if (offline == false)
                    player.send(5, 60, 20,
                            "§c§lDEFEAT!", "§3It helps to stay awake in this game.");

                if (BWBase.getActiveIslands().contains(playerIsland)) {
                    for (Player players : Bukkit.getOnlinePlayers()) {
                        GamePlayer server = new GamePlayer(players.getUniqueId());
                        server.send("");
                        server.send(M.MCBW + playerIsland.getColor().getChatColor() + "§l" +
                                playerIsland.getColor().getDisplayName() + " Team §3just got §c§lKNOCKED OUT!");
                        server.send("");
                    }
                }

                int victimPoints = BWSQL.getStat(player.getUUID(), "points");
                int pointsOnLot = victimPoints / 20;

                // remove point stat
                BWSQL.add_stat(player.getUUID(), victimPoints - pointsOnLot, "points");
                if (offline == false)
                    M.lossPoints(new GamePlayer(player.getUUID()),
                            "Bedwars", pointsOnLot, victimPoints - pointsOnLot);

                if (EventDeathMessage.LAST_DAMAGE_MAP.get(player.getUUID()) != null) {
                    Player cause1 = Bukkit.getPlayer(EventDeathMessage.LAST_DAMAGE_MAP.get(player.getUUID()));
                    int killerPoints = BWSQL.getStat(cause1.getUniqueId(), "points");
                    int pointsAdded = pointsOnLot < 5 ? 5 : pointsOnLot;

                    // add point stat
                    // could the problem be here then?
                    BWSQL.add_stat(cause1.getUniqueId(), killerPoints + pointsAdded, "points");

                    M.gainPoints(new GamePlayer(cause1.getUniqueId()),
                            "Bedwars", pointsAdded, killerPoints + pointsAdded);

                    // add kill stat
                    BWSQL.add_stat(cause1.getUniqueId(),
                            BWSQL.getStat(cause1.getUniqueId(), "kills") + 1, "kills");

                }
                // add games played
                BWSQL.add_stat(player.getUUID(), BWSQL.getStat(player.getUUID(), "played") + 1,
                        "played");

                new BedwarsPlayer(player).setSpectatorMode(true);
                islandForPlayer(player).setActive(false);

                BWBase.RESPAWNING_TIMER.remove(player.getUUID());

                if (!(GameState.isState(GameState.LOBBY))) {
                    BWBase.endGameIfNeeded();
                }

            }

            player.bukkit().teleport(playerIsland.getSpawnLocation().clone().add(0, 40, 0));
            if (manualDeath == true) {
                player.setHealth(0);
            }
        }
    }

    public static void setupPlayers() {
        int currentEmptyIslandIndex = 0;
        List<Island> emptyIslands = BWBase.ISLANDS.stream().filter(island -> island.getPlayers().size() == 0).collect(Collectors.toList());
        if (emptyIslands.size() == 0) {
            emptyIslands = BWBase.ISLANDS.stream().filter(island -> island.getPlayers().size()
                    < BWBase.MAX_TEAM_SIZE).collect(Collectors.toList());
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            Island island = BWBase.islandForPlayer(new GamePlayer(player.getUniqueId()));

            if (EventCuboid.WAITING_PLAYER.contains(player.getUniqueId())) {
                EventCuboid.WAITING_PLAYER.remove(player.getUniqueId());
                new GamePlayer(player.getUniqueId()).resetInventory();
            }

            if (island == null) {
                if (emptyIslands.size() == 0) {
                    M.next_noSpaceFound(new GamePlayer(player.getUniqueId()), "server bw");
                    GameState.setSpectating(new GamePlayer(player.getUniqueId()));
                    continue;
                }

                Island foundIsland = emptyIslands.get(currentEmptyIslandIndex);

                if (foundIsland.getPlayers().size() >= BWBase.MAX_TEAM_SIZE) {
                    M.next_noSpaceFound(new GamePlayer(player.getUniqueId()), "server bw");
                    GameState.setSpectating(new GamePlayer(player.getUniqueId()));
                    continue;
                }

                currentEmptyIslandIndex += 1;
                if (currentEmptyIslandIndex >= emptyIslands.size()) {
                    currentEmptyIslandIndex = 0;
                }

                foundIsland.addPlayer(new GamePlayer(player.getUniqueId()));
                GameState.setPlaying(new GamePlayer(player.getUniqueId()));
            }

        }

        for (Island island : BWBase.ISLANDS) {
            if (island.getPlayers().size() > 0) {
                island.setActive(true);
            } else if (island.getPlayers().size() <= 0) {
                island.setActive(false);
            }

            island.getPlayers().stream().forEach(player -> new BedwarsPlayer(new GamePlayer(player)).setPlaying());

            if (island.isActive() == false) {

                Block block = island.getBedLocation().getBlock();

                //Bukkit.broadcastMessage("island activity is false for " + island.getColor() + island.getColor().getDisplayName());


                if (block.getState().getData() instanceof Bed) {
                    Bed bed = (Bed) block.getState().getData();
                    BlockFace otherBedDirection = bed.isHeadOfBed() ? bed.getFacing().getOppositeFace() : bed.getFacing();
                    Location locPrime = block.getLocation();
                    Location locOther = block.getRelative(otherBedDirection).getLocation();

                    block.setType(Material.AIR);
                    block.getDrops().clear();

                    Bukkit.getWorld("BW-" + BWBase.LIVE_MAP).getBlockAt(locPrime).setType(Material.AIR);
                    Bukkit.getWorld("BW-" + BWBase.LIVE_MAP).getBlockAt(locPrime).getDrops().clear();

                    Bukkit.getWorld("BW-" + BWBase.LIVE_MAP).getBlockAt(locOther).setType(Material.AIR);
                    Bukkit.getWorld("BW-" + BWBase.LIVE_MAP).getBlockAt(locOther).getDrops().clear();

                   // Bukkit.broadcastMessage("breaking bed for " + island.getColor() + island.getColor().getDisplayName());

                }

            }

            island.spawnShops();
            island.getIronGenerators().stream().forEach(gen -> gen.setTier(GeneratorTier.ONE));
            island.getIronGenerators().stream().forEach(gen -> gen.setActivated(true));
            island.getGoldGenerators().stream().forEach(gen -> gen.setTier(GeneratorTier.ONE));
            island.getGoldGenerators().stream().forEach(gen -> gen.setActivated(true));
        }

    }

    public static Island getIslandForBedLocation(Location location) {
        Optional<Island> islandOptional = ISLANDS.stream().filter(island -> {

            if (island.getBedLocation().equals(location)) {
                return true;
            }

            return false;
        }).findFirst();

        return islandOptional.orElse(null);
    }

    public static void endGameIfNeeded() {
        if (GameState.isState(GameState.LOBBY) ||
                GameState.isState(GameState.PREGAME) ||
                GameState.isState(GameState.POSTGAME) ||
                GameState.isState(GameState.CLEANUP))
            return;

        if (getActiveIslands().size() > 1) {
            return; // no need to end
        }

        GameState.setState(GameState.POSTGAME);
        ServerSQL.add_info(Main.getInstance().getServerID().getName(), GameState.getState().name(), "GAMESTATE");

        if (BWBase.LIVE_MAP != null) {
            Bukkit.getWorld("BW-" + BWBase.LIVE_MAP).setTime(18000);
        }

        if (BWBase.getActiveIslands().size() != 0) {
            BWBase.setWinner(BWBase.getActiveIslands().get(0));
            BWBase.getActiveIslands().get(0).getPlayers().forEach(uuid ->
                    BWSQL.add_stat(uuid, BWSQL.getStat(uuid, "played") + 1,
                            "played"));
            return;

        }
    }

    public static List<Island> getActiveIslands() {
        return ISLANDS.stream().filter(island -> {
            if (island.isActive() == false) return false;
            if (island.isBedPlaced() == true) return true;

            return island.alivePlayerCount() > 0;
        }).collect(Collectors.toList());
    }

    public static Island islandForPlayer(GamePlayer player) {
        return BWBase.ISLANDS.stream().filter(island -> island.isMember(player)).findFirst().orElse(null);
    }

    // must be called on the correct state and on every tick in order for delays to work cohesively
    public static void tick() {


        // start
        if (BWBase.TIME_MANAGER.inGame == ((20*60))) {
            for (Generator diamonds : BWBase.DIAMOND_GEN_LIST) {
                diamonds.setTier(GeneratorTier.ONE);
            }
            for (Generator diamonds : BWBase.EMERALD_GEN_LIST) {
                diamonds.setTier(GeneratorTier.ONE);
            }
        }
        // 5 min into game
        if (BWBase.TIME_MANAGER.inGame == ((20*60) - (5*60))) {
            for (Generator diamonds : BWBase.DIAMOND_GEN_LIST) {
                diamonds.setTier(GeneratorTier.TWO);
            }
            for (Player players : Bukkit.getOnlinePlayers()) {
                GamePlayer player = new GamePlayer(players.getUniqueId());
                player.send(M.MCBW + GeneratorType.DIAMOND.getName() + "s §3have now leveled up to §c§lTier II!");
                player.send(Sound.LEVEL_UP);
            }
        }
        // 8 min into game
        if (BWBase.TIME_MANAGER.inGame == ((20*60) - (8*60))) {
            for (Generator emeralds : BWBase.EMERALD_GEN_LIST) {
                emeralds.setTier(GeneratorTier.TWO);
            }
            for (Player players : Bukkit.getOnlinePlayers()) {
                GamePlayer player = new GamePlayer(players.getUniqueId());
                player.send(M.MCBW + GeneratorType.EMERALD.getName() + "s §3have now leveled up to §c§lTier II!");
                player.send(Sound.LEVEL_UP, 0.8F);
            }
        }
        // 10 min into game
        if (BWBase.TIME_MANAGER.inGame == ((20*60) - (10*60))) {
            for (Generator diamonds : BWBase.DIAMOND_GEN_LIST) {
                diamonds.setTier(GeneratorTier.THREE);
            }
            for (Player players : Bukkit.getOnlinePlayers()) {
                GamePlayer player = new GamePlayer(players.getUniqueId());
                player.send(M.MCBW + GeneratorType.DIAMOND.getName() + "s §3have now leveled up to §c§lTier III!");
                player.send(Sound.LEVEL_UP, 0.6F);
            }
        }
        // 12 min into game
        if (BWBase.TIME_MANAGER.inGame == ((20*60) - (12*60))) {
            for (Generator emeralds : BWBase.EMERALD_GEN_LIST) {
                emeralds.setTier(GeneratorTier.THREE);
            }
            for (Player players : Bukkit.getOnlinePlayers()) {
                GamePlayer player = new GamePlayer(players.getUniqueId());
                player.send(M.MCBW + GeneratorType.EMERALD.getName() + "s §3have now leveled up to §c§lTier III!");
                player.send(Sound.LEVEL_UP, 0.4F);
            }
        }

        // Spawn Diamonds
        for (Generator generators : BWBase.DIAMOND_GEN_LIST) {
            generators.setActivated(true);
            generators.spawn();
        }

        // Spawn Emeralds
        for (Generator generators : BWBase.EMERALD_GEN_LIST) {
            generators.setActivated(true);
            generators.spawn();
        }

        // Spawn Island Gen
        for (Island island : BWBase.ISLANDS){
            island.itemShopEntity.teleport(island.getShopEntityLocation());
            island.upgradeShopEntity.teleport(island.getUpgradeEntityLocation());

            //if (island.isActive() == false)
            //    continue;
            for (Generator generator : island.getGoldGenerators()) {
                generator.spawn();
            }
            for (Generator generator : island.getIronGenerators()) {
                generator.spawn();
            }
        }

        for (Island island : BWBase.getActiveIslands()) {
            if (island.getLevelForUpgrade(IslandUpgrade.ALERT) != 0) {
                List<Location> withinIsland = BlockManager.getBlocks(island.getBedLocation(), 12)
                        .stream().map(Block::getLocation).collect(Collectors.toList());

                for (UUID player : GameState.PLAYING) {
                    if (island.isMember(new GamePlayer(player))) continue;

                    Location normalized = new Location(
                            new GamePlayer(player).bukkit().getLocation().getWorld(),
                            new GamePlayer(player).bukkit().getLocation().getBlockX(),
                            new GamePlayer(player).bukkit().getLocation().getBlockY(),
                            new GamePlayer(player).bukkit().getLocation().getBlockZ()
                    );

                    if (withinIsland.contains(normalized)) {
                        island.alert(new GamePlayer(player));
                        break;
                    }
                }
            }
        }

    }

    // call this only once you bozo smh
    public static void setupIslands() {
        String name = "BW-" + BWBase.LIVE_MAP;

        // Clear Island Chests
        for(Chunk chunk : Bukkit.getWorld(name).getLoadedChunks()) {
            for (BlockState chest : chunk.getTileEntities()) {
                if (chest instanceof Chest) {
                    Inventory contents = ((Chest) chest).getInventory();
                    contents.clear();
                }
            }
        }

        if(C.BW_DATA_CONFIG.getString("maps." + BWBase.LIVE_MAP + ".sp") != null) {

            // Diamond
            double px1 = C.BW_DATA_CONFIG.getInt("maps." + BWBase.LIVE_MAP + ".diamond1.x");
            double py1 = C.BW_DATA_CONFIG.getInt("maps." + BWBase.LIVE_MAP + ".diamond1.y");
            double pz1 = C.BW_DATA_CONFIG.getInt("maps." + BWBase.LIVE_MAP + ".diamond1.z");
            float pyaw1 = (float) C.BW_DATA_CONFIG.getDouble("maps." + BWBase.LIVE_MAP + ".diamond1" + ".pos2.yaw");
            float ppitch1 = (float) 0.0;

            double px2 = C.BW_DATA_CONFIG.getInt("maps." + BWBase.LIVE_MAP + ".diamond2.x");
            double py2 = C.BW_DATA_CONFIG.getInt("maps." + BWBase.LIVE_MAP + ".diamond2.y");
            double pz2 = C.BW_DATA_CONFIG.getInt("maps." + BWBase.LIVE_MAP + ".diamond2.z");
            float pyaw2 = (float) C.BW_DATA_CONFIG.getDouble("maps." + BWBase.LIVE_MAP + ".diamond2" + ".pos2.yaw");
            float ppitch2 = (float) 0.0;

            double px3 = C.BW_DATA_CONFIG.getInt("maps." + BWBase.LIVE_MAP + ".diamond3.x");
            double py3 = C.BW_DATA_CONFIG.getInt("maps." + BWBase.LIVE_MAP + ".diamond3.y");
            double pz3 = C.BW_DATA_CONFIG.getInt("maps." + BWBase.LIVE_MAP + ".diamond3.z");
            float pyaw3 = (float) C.BW_DATA_CONFIG.getDouble("maps." + BWBase.LIVE_MAP + ".diamond3.yaw");
            float ppitch3 = (float) 0.0;

            double px4 = C.BW_DATA_CONFIG.getInt("maps." + BWBase.LIVE_MAP + ".diamond4.x");
            double py4 = C.BW_DATA_CONFIG.getInt("maps." + BWBase.LIVE_MAP + ".diamond4.y");
            double pz4 = C.BW_DATA_CONFIG.getInt("maps." + BWBase.LIVE_MAP + ".diamond4.z");
            float pyaw4 = (float) C.BW_DATA_CONFIG.getDouble("maps." + BWBase.LIVE_MAP + ".diamond4.yaw");
            float ppitch4 = (float) 0.0;

            Location ps1 = new Location(Bukkit.getWorld(name), M.getSpawnLoc(px1), py1, M.getSpawnLoc(pz1), pyaw1, ppitch1);
            Location ps2 = new Location(Bukkit.getWorld(name), M.getSpawnLoc(px2), py2, M.getSpawnLoc(pz2), pyaw2, ppitch2);
            Location ps3 = new Location(Bukkit.getWorld(name), M.getSpawnLoc(px3), py3, M.getSpawnLoc(pz3), pyaw3, ppitch3);
            Location ps4 = new Location(Bukkit.getWorld(name), M.getSpawnLoc(px4), py4, M.getSpawnLoc(pz4), pyaw4, ppitch4);

            Generator d1 = new Generator(ps1, GeneratorType.DIAMOND, false);
            Generator d2 = new Generator(ps2, GeneratorType.DIAMOND, false);
            Generator d3 = new Generator(ps3, GeneratorType.DIAMOND, false);
            Generator d4 = new Generator(ps4, GeneratorType.DIAMOND, false);

            // Emerald
            double qx1 = C.BW_DATA_CONFIG.getInt("maps." + BWBase.LIVE_MAP + ".emerald1.x");
            double qy1 = C.BW_DATA_CONFIG.getInt("maps." + BWBase.LIVE_MAP + ".emerald1.y");
            double qz1 = C.BW_DATA_CONFIG.getInt("maps." + BWBase.LIVE_MAP + ".emerald1.z");
            float qyaw1 = (float) C.BW_DATA_CONFIG.getDouble("maps." + BWBase.LIVE_MAP + ".emerald1" + ".pos2.yaw");
            float qpitch1 = (float) 0.0;

            double qx2 = C.BW_DATA_CONFIG.getInt("maps." + BWBase.LIVE_MAP + ".emerald2.x");
            double qy2 = C.BW_DATA_CONFIG.getInt("maps." + BWBase.LIVE_MAP + ".emerald2.y");
            double qz2 = C.BW_DATA_CONFIG.getInt("maps." + BWBase.LIVE_MAP + ".emerald2.z");
            float qyaw2 = (float) C.BW_DATA_CONFIG.getDouble("maps." + BWBase.LIVE_MAP + ".emerald2" + ".pos2.yaw");
            float qpitch2 = (float) 0.0;

            double qx3 = C.BW_DATA_CONFIG.getInt("maps." + BWBase.LIVE_MAP + ".emerald3.x");
            double qy3 = C.BW_DATA_CONFIG.getInt("maps." + BWBase.LIVE_MAP + ".emerald3.y");
            double qz3 = C.BW_DATA_CONFIG.getInt("maps." + BWBase.LIVE_MAP + ".emerald3.z");
            float qyaw3 = (float) C.BW_DATA_CONFIG.getDouble("maps." + BWBase.LIVE_MAP + ".emerald3" + ".pos2.yaw");
            float qpitch3 = (float) 0.0;

            double qx4 = C.BW_DATA_CONFIG.getInt("maps." + BWBase.LIVE_MAP + ".emerald4.x");
            double qy4 = C.BW_DATA_CONFIG.getInt("maps." + BWBase.LIVE_MAP + ".emerald4.y");
            double qz4 = C.BW_DATA_CONFIG.getInt("maps." + BWBase.LIVE_MAP + ".emerald4.z");
            float qyaw4 = (float) C.BW_DATA_CONFIG.getDouble("maps." + BWBase.LIVE_MAP + ".emerald4" + ".pos2.yaw");
            float qpitch4 = (float) 0.0;

            Location rs1 = new Location(Bukkit.getWorld(name), M.getSpawnLoc(qx1), qy1, M.getSpawnLoc(qz1), qyaw1, qpitch1);
            Location rs2 = new Location(Bukkit.getWorld(name), M.getSpawnLoc(qx2), qy2, M.getSpawnLoc(qz2), qyaw2, qpitch2);
            Location rs3 = new Location(Bukkit.getWorld(name), M.getSpawnLoc(qx3), qy3, M.getSpawnLoc(qz3), qyaw3, qpitch3);
            Location rs4 = new Location(Bukkit.getWorld(name), M.getSpawnLoc(qx4), qy4, M.getSpawnLoc(qz4), qyaw4, qpitch4);

            Generator e1 = new Generator(rs1, GeneratorType.EMERALD, false);
            Generator e2 = new Generator(rs2, GeneratorType.EMERALD, false);
            Generator e3 = new Generator(rs3, GeneratorType.EMERALD, false);
            Generator e4 = new Generator(rs4, GeneratorType.EMERALD, false);

            BWBase.DIAMOND_GEN_LIST.add(d1);
            BWBase.DIAMOND_GEN_LIST.add(d2);
            BWBase.DIAMOND_GEN_LIST.add(d3);
            BWBase.DIAMOND_GEN_LIST.add(d4);

            BWBase.EMERALD_GEN_LIST.add(e1);
            BWBase.EMERALD_GEN_LIST.add(e2);
            BWBase.EMERALD_GEN_LIST.add(e3);
            BWBase.EMERALD_GEN_LIST.add(e4);

            for(String id : C.BW_DATA_CONFIG.getConfigurationSection("maps." + BWBase.LIVE_MAP + ".sp").getKeys(false)){

                // Sets island color
                Island island = new Island(IslandColor.fromID(Integer.parseInt(id)));

                // Set player spawn
                double gx = C.BW_DATA_CONFIG.getInt("maps." + BWBase.LIVE_MAP + ".sp." + id + ".x");
                double gy = C.BW_DATA_CONFIG.getInt("maps." + BWBase.LIVE_MAP + ".sp." + id + ".y");
                double gz = C.BW_DATA_CONFIG.getInt("maps." + BWBase.LIVE_MAP + ".sp." + id + ".z");
                float gyaw = (float) C.BW_DATA_CONFIG.getDouble("maps." + BWBase.LIVE_MAP + ".sp." + id + ".yaw");
                float gpitch = (float) 0.0;
                Location gs = new Location(Bukkit.getWorld(name), M.getSpawnLoc(gx), gy, M.getSpawnLoc(gz), gyaw, gpitch);

                // Iron gen spawn
                double hx = C.BW_DATA_CONFIG.getInt("maps." + BWBase.LIVE_MAP + ".sp." + id + ".iron1.x");
                double hy = C.BW_DATA_CONFIG.getInt("maps." + BWBase.LIVE_MAP + ".sp." + id + ".iron1.y");
                double hz = C.BW_DATA_CONFIG.getInt("maps." + BWBase.LIVE_MAP + ".sp." + id + ".iron1.z");
                float hyaw = (float) C.BW_DATA_CONFIG.getDouble("maps." + BWBase.LIVE_MAP + ".sp." + id + ".iron1.yaw");
                float hpitch = (float) 0.0;
                Location hs = new Location(Bukkit.getWorld(name), M.getSpawnLoc(hx), hy, M.getSpawnLoc(hz), hyaw, hpitch);

                // Iron gen 2 spawn
                double ix = C.BW_DATA_CONFIG.getInt("maps." + BWBase.LIVE_MAP + ".sp." + id + ".iron2.x");
                double iy = C.BW_DATA_CONFIG.getInt("maps." + BWBase.LIVE_MAP + ".sp." + id + ".iron2.y");
                double iz = C.BW_DATA_CONFIG.getInt("maps." + BWBase.LIVE_MAP + ".sp." + id + ".iron2.z");
                float iyaw = (float) C.BW_DATA_CONFIG.getDouble("maps." + BWBase.LIVE_MAP + ".sp." + id + ".iron2.yaw");
                float ipitch = (float) 0.0;
                Location is = new Location(Bukkit.getWorld(name), M.getSpawnLoc(ix), iy, M.getSpawnLoc(iz), iyaw, ipitch);

                // Gold gen spawn
                double jx = C.BW_DATA_CONFIG.getInt("maps." + BWBase.LIVE_MAP + ".sp." + id + ".gold1.x");
                double jy = C.BW_DATA_CONFIG.getInt("maps." + BWBase.LIVE_MAP + ".sp." + id + ".gold1.y");
                double jz = C.BW_DATA_CONFIG.getInt("maps." + BWBase.LIVE_MAP + ".sp." + id + ".gold1.z");
                float jyaw = (float) C.BW_DATA_CONFIG.getDouble("maps." + BWBase.LIVE_MAP + ".sp." + id + ".gold1.yaw");
                float jpitch = (float) 0.0;
                Location js = new Location(Bukkit.getWorld(name), M.getSpawnLoc(jx), jy, M.getSpawnLoc(jz), jyaw, jpitch);

                // Shop NPC spawn
                double kx = C.BW_DATA_CONFIG.getInt("maps." + BWBase.LIVE_MAP + ".sp." + id + ".shop.x");
                double ky = C.BW_DATA_CONFIG.getInt("maps." + BWBase.LIVE_MAP + ".sp." + id + ".shop.y");
                double kz = C.BW_DATA_CONFIG.getInt("maps." + BWBase.LIVE_MAP + ".sp." + id + ".shop.z");
                float kyaw = (float) C.BW_DATA_CONFIG.getDouble("maps." + BWBase.LIVE_MAP + ".sp." + id + ".shop.yaw");
                float kpitch = (float) 0.0;
                Location ks = new Location(Bukkit.getWorld(name), M.getSpawnLoc(kx), ky, M.getSpawnLoc(kz), kyaw, kpitch);

                // Upgrade NPC spawn
                double lx = C.BW_DATA_CONFIG.getInt("maps." + BWBase.LIVE_MAP + ".sp." + id + ".upgrade.x");
                double ly = C.BW_DATA_CONFIG.getInt("maps." + BWBase.LIVE_MAP + ".sp." + id + ".upgrade.y");
                double lz = C.BW_DATA_CONFIG.getInt("maps." + BWBase.LIVE_MAP + ".sp." + id + ".upgrade.z");
                float lyaw = (float) C.BW_DATA_CONFIG.getDouble("maps." + BWBase.LIVE_MAP + ".sp." + id + ".upgrade.yaw");
                float lpitch = (float) 0.0;
                Location ls = new Location(Bukkit.getWorld(name), M.getSpawnLoc(lx), ly, M.getSpawnLoc(lz), lyaw, lpitch);

                // Bed
                double mx = C.BW_DATA_CONFIG.getInt("maps." + BWBase.LIVE_MAP + ".sp." + id + ".beddy.x");
                double my = C.BW_DATA_CONFIG.getInt("maps." + BWBase.LIVE_MAP + ".sp." + id + ".beddy.y");
                double mz = C.BW_DATA_CONFIG.getInt("maps." + BWBase.LIVE_MAP + ".sp." + id + ".beddy.z");
                Location ms = new Location(Bukkit.getWorld(name), mx, my, mz).getBlock().getLocation();

                // Set locations
                island.setSpawnLocation(gs);
                island.setIronGenerator1(hs);
                island.setIronGenerator2(is);
                island.setGoldGenerator(js);
                island.setShopLocation(ks);
                island.setUpgradeLocation(ls);
                island.setBedLocation(ms);

                // Set Holo location for Island Generator
                Hologram hologram = new Hologram(island.getGoldGenerator().clone().subtract(0, 0.5, 0), "§f§lCollect!");
                hologram.teleport(island.getGoldGenerator().clone().add(0, 1, 0));
                BWBase.HOLO_LIST.add(hologram);

                // Set Island locations
                Generator gen1 = new Generator(island.getIronGenerator1(), GeneratorType.IRON, true);
                Generator gen2 = new Generator(island.getIronGenerator2(), GeneratorType.IRON, true);
                Generator gen3 = new Generator(island.getGoldGenerator(), GeneratorType.GOLD, true);

                island.getIronGenerators().add(gen1);
                island.getIronGenerators().add(gen2);
                island.getGoldGenerators().add(gen3);

                BWBase.ISLANDS.add(island);
            }
        }
     }

    public static void setWinner(Island island) {
        M.winner(island.getColor().getChatColor() + "§l" + island.getColor().getDisplayName(), "Bedwars");

        island.getPlayers().stream().forEach(uuid ->
                BWSQL.add_stat(uuid, BWSQL.getStat(uuid, "won") + 1, "won"));
        island.getPlayers().stream().forEach(uuid ->
                new GamePlayer(uuid).send(5, 60, 20,
                "§3§lVICTORY!", ""));
        island.getPlayers().stream().forEach(uuid ->
                M.next(new GamePlayer(uuid), "server bw"));

        //Player winner = Bukkit.getPlayer(GameState.PLAYING.get(M.getRandomInt(0,
        //        GameState.PLAYING.size()-1)));
        Bukkit.getWorld("BW-" + BWBase.LIVE_MAP).setTime(18000);

    }

    public static void mapInfo() {
        String author = C.BW_DATA_CONFIG.getString("maps." + BWBase.LIVE_MAP + ".author");
        String link = C.BW_DATA_CONFIG.getString("maps." + BWBase.LIVE_MAP + ".link");
        if (author == null)
            author = "Arvaux Associate";
        if (link == null)
            link = M.FORUM_LINK;
        M.mapInfo(BWBase.getMapNameOf(BWBase.LIVE_MAP), author, link);
    }

    public static void mapWon() {
        BWBase.VOTING = false;

        for (Player players : Bukkit.getOnlinePlayers()) {
            GamePlayer server = new GamePlayer(players.getUniqueId());

            String map = BWBase.get_vm().getWinningMap();
            BWBase.LIVE_MAP = map;

            server.send(M.MCBW + "§3The map §b§l" + BWBase.getMapNameOf(map) + "§3 was chosen!");

            server.send(Sound.ITEM_PICKUP);
        }
    }

    public static VoteManager get_vm() {
        return BWBase._vm;
    }

    public static String getMapNameOf(String name) {
        return C.BW_DATA_CONFIG.getString("maps." + name + ".name");
    }

    //public static VoteManager getVM() {
    //    return BWBase._vm;
    //}

    @Override
    public void boot() {
        this.getPluginManager().registerEvents(this, Main.getInstance());
        this.getPluginManager().registerEvents(new EventJoinQuit(), Main.getInstance());
        this.getPluginManager().registerEvents(new EventMCBW(), Main.getInstance());
        this.getPluginManager().registerEvents(new MainShopStoryboard(), Main.getInstance());
        this.getPluginManager().registerEvents(new CategoriesStoryboard.ArmorStoryboard(), Main.getInstance());
        this.getPluginManager().registerEvents(new CategoriesStoryboard.ArcheryStoryboard(), Main.getInstance());
        this.getPluginManager().registerEvents(new CategoriesStoryboard.BlockStoryboard(), Main.getInstance());
        this.getPluginManager().registerEvents(new CategoriesStoryboard.ToolsStoryboard(), Main.getInstance());
        this.getPluginManager().registerEvents(new CategoriesStoryboard.PotionStoryboard(), Main.getInstance());
        this.getPluginManager().registerEvents(new CategoriesStoryboard.WeaponryStoryboard(), Main.getInstance());
        this.getPluginManager().registerEvents(new CategoriesStoryboard.UtilityStoryboard(), Main.getInstance());



        this.getPluginManager().registerEvents(new UpgradeStoryboard(), Main.getInstance());
        new EventMCBW().onEntityHmm();
        this.switchModule(new ServerSwitchModuleEvent(Module.getModule(),
                Module.getModuleS(Main.getInstance().config.getString("last-module"))));
    }

    @EventHandler
    public void switchModule(ServerSwitchModuleEvent event) {
        if (event.getModule() == Module.MCBW) {
            BWBase.start();
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.teleport(CommandSpawnpoint.getWaitingpoint());
            }
            for (Player player : Bukkit.getOnlinePlayers()) {
                BWBase.join(new GamePlayer(player.getUniqueId()));
            }
        }
    }

    @Override
    public Module m() {
        return Module.MCBW;
    }

    @Override
    public void quit() {
        BWBase.stop();
        for (Player players : Bukkit.getOnlinePlayers()) {
            GamePlayer v = new GamePlayer(players.getUniqueId());
            players.teleport(CommandSpawnpoint.getSpawnpoint());
            if (v.isVisibility() == false) {
                v.setVisibility(true, false);
                v.send(M.t(M.GAME, "Player Visibility", true));
            }
        }
    }

    public static void sendVotingCommand(GameCommand command) {
        if (BWBase.VOTING == true) {

            if (command.getArgs().length == 0) {

                int totalVotes = 0;
                for (int id = 0; id < BWBase.get_vm().getMaps().size(); id++) {
                    totalVotes = totalVotes + BWBase.get_vm().getVotes().get(BWBase.get_vm().getMaps().get(id));
                }

                StringBuilder previousMaps = new StringBuilder();
                int id = 0;
                command.getPlayer().send(M.HEADER);
                for (String map : BWBase.get_vm().getMaps()) {
                    int voteNumber = id + 1;
                    int votes = BWBase.get_vm().getVotes().get(BWBase.get_vm().getMaps().get(id));
                    String chance = M.getChance(votes, totalVotes);
                    if (votes == 1) {
                        command.getPlayer().send("§7# §b" + voteNumber + " §8| §b§l" + votes + " vote §8» §e§l"
                                        + BWBase.getMapNameOf(map) +  " §7(" + chance + "%)",
                                "§7Vote for " + BWBase.getMapNameOf(map) + "!", "vote " + voteNumber);
                    } else {
                        command.getPlayer().send("§7# §b" + voteNumber + " §8| §b§l" + votes + " votes §8» §e§l"
                                        + BWBase.getMapNameOf(map) +  " §7(" + chance + "%)",
                                "§7Vote for " + BWBase.getMapNameOf(map) + "!", "vote " + voteNumber);
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
                            if ((BWBase.get_vm().getVoted().get(command.getPlayer().getUUID())) == null) {
                                Integer RealNumber = Integer.valueOf(command.getArgs(0));
                                Integer Number = RealNumber - 1;
                                String map = BWBase.get_vm().getMaps().get(Number);
                                BWBase.get_vm().addVote(command.getPlayer(), map);
                                int votes = BWBase.get_vm().getVotes().get(BWBase.get_vm().getMaps().get(Number));
                                if (votes == 1) {
                                    command.getPlayer().send(M.MCBW + "§3You chose §b§l" + BWBase.getMapNameOf(map) + " §3with §b§l" + votes + " vote!");
                                } else {
                                    command.getPlayer().send(M.MCBW + "§3You chose §b§l" + BWBase.getMapNameOf(map) + " §3with §b§l" + votes + " votes!");
                                }
                            } else command.getPlayer().send(M.MCBW + "§cYou already voted for a map!");
                        } else command.getPlayer().send(M.MCBW + "§cThat is not a voting option!");
                    } else command.getPlayer().send(M.MCBW + "§cInvalid vote!");
                } else command.getPlayer().send(M.GAME_RUNNING);
            }


            CommandHelpCenter help = command.getHelp();
            help.add("[1-5]", "Chooses a map");
            return;

        } else {
            command.getPlayer().send(M.MCBW + "§cVoting period has ended.");
        }
    }

}
