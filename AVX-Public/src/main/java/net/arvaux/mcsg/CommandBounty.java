package net.arvaux.mcsg;

import net.arvaux.core.cmd.Command;
import net.arvaux.core.cmd.GameCommand;
import net.arvaux.core.cmd.ICommand;
import net.arvaux.core.cmd.ICompleter;
import net.arvaux.core.entity.player.GamePlayer;
import net.arvaux.core.ess.GameState;
import net.arvaux.core.module.Module;
import net.arvaux.core.util.M;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommandBounty implements ICommand, ICompleter {

    @Command(name = "confirmbounty", aliases = "cb")
    public boolean j(GameCommand command) {
        if(SGBase.BOUNTY_MANAGER.isConfirming(command.getPlayer().getUUID())) {
            if(GameState.isState(GameState.INGAME)) {
                if(command.getArgs().length == 0) {
                    if(GameState.PLAYING.contains(SGBase.BOUNTY_MANAGER.bountyTarget.get(command.getPlayer().getUUID()))) {
                        if(GameState.PLAYING.contains(SGBase.BOUNTY_MANAGER.bountyTarget.get(command.getPlayer().getUUID()))) {
                            // todo disguise manager see bounty cmd

                            if(M.usePoints(command.getPlayer(), Integer.valueOf(SGBase.BOUNTY_MANAGER.bountyAmount.get(command.getPlayer().getUUID()))) == true) {
                                int points = Integer.valueOf(SGBase.BOUNTY_MANAGER.bountyAmount.get(command.getPlayer().getUUID()));
                                Player giveTo = Bukkit.getPlayer(SGBase.BOUNTY_MANAGER.bountyTarget.get(command.getPlayer().getUUID()));

                                SGBase.BOUNTY_MANAGER.addBounty(giveTo.getUniqueId(), points);

                                for (Player players : Bukkit.getOnlinePlayers()) {
                                    new GamePlayer(players.getUniqueId()).send(M.MCSG +
                                            "§3A §c§lbounty §3of §e§l" + points + "§3 has been set on " + new GamePlayer(giveTo.getUniqueId()).getDisplayGroup().getColor() + "§l"
                                                    + new GamePlayer(giveTo.getUniqueId()).getDisplayName() + " §3by " + command.getPlayer().getDisplayGroup().getColor() + "§l"
                                                    + command.getPlayer().getDisplayName() + "!");
                                    new GamePlayer(players.getUniqueId()).send(Sound.ORB_PICKUP, 0);
                                }
                            } else command.getPlayer().send(M.MCSG + "§3Not enough SG points!");
                        } else command.getPlayer().send(M.EXCEPTION_PLAYER_NOT_FOUND);
                    } else command.getPlayer().send(M.EXCEPTION_PLAYER_OFFLINE);
                    SGBase.BOUNTY_MANAGER.removeBounty(command.getPlayer().getUUID());
                } else command.getHelp().send(command.getSender());
            } else command.getPlayer().send(M.GAME_WRONG_STATE);
        } else command.getPlayer().send(M.MCSG + "§3Use §c§l/bounty §3to bounty a player!");
        return false;
    }

    @Command(name = "bounty")
    public boolean k(GameCommand command) {
        if (!Module.isModule(Module.MCSG)) {
            command.getPlayer().send(M.COMMAND_MOD_DISABLED);
            return false;
        }
            if (GameState.SPECTATING.contains(command.getPlayer().getUUID())) {
                if (GameState.isState(GameState.INGAME)) {
                    if (command.getArgs().length == 2) {
                        Player targetPlayer = Bukkit.getPlayer(command.getArgs()[0]);
                        if (targetPlayer != null) {
                            if (GameState.PLAYING.contains(targetPlayer.getUniqueId())) {
                                //todo
                                /*if (Main.getInstance().getDisguiseManager().isDisguised(targetPlayer)) {
                                    if (X.getString(targetPlayer.getUniqueId().toString() + ".Username").toLowerCase().equalsIgnoreCase(args[0].toLowerCase())) {
                                        player.sendMessage(M.c(M.prefix(player) + " " + M.playerNotFound));
                                        return true;
                                    }
                                }*/
                                if (M.isInt(command.getArgs()[1])) {
                                    if (Integer.valueOf(command.getArgs()[1]) > 9) {
                                        SGBase.BOUNTY_MANAGER.addConfirmList(command.getPlayer().getUUID());
                                        SGBase.BOUNTY_MANAGER.bountyAmount.put(command.getPlayer().getUUID(), Integer.valueOf(command.getArgs()[1]));
                                        SGBase.BOUNTY_MANAGER.bountyTarget.put(command.getPlayer().getUUID(), targetPlayer.getUniqueId());
                                        command.getPlayer().send(M.MCSG + "§cAre you sure you want to bounty §e§l" + Integer.valueOf(command.getArgs()[1])
                                                + " §c§lon " + new GamePlayer(targetPlayer.getUniqueId()).getDisplayGroup().getColor() + "§l" +
                                                new GamePlayer(targetPlayer.getUniqueId()).getDisplayName() + "?");
                                        command.getPlayer().send(M.MCSG + "§3Type §c§l/confirmbounty §3to confirm your bounty.");
                                    } else
                                        command.getPlayer().send(M.MCSG + "§cYour bounty must be higher than 9.");
                                } else
                                    command.getPlayer().send(M.EXCEPTION_INT);
                            } else
                                command.getPlayer().send(M.EXCEPTION_PLAYER_NOT_FOUND);
                        } else
                            command.getPlayer().send(M.EXCEPTION_PLAYER_OFFLINE);
                    } else command.getHelp().send(command.getSender());
                } else
                    command.getPlayer().send(M.GAME_WRONG_STATE);
            } else
                command.getPlayer().send(M.MCSG + "§cAren't you a tribute? L...");

        return false;
    }

    @Command(name = "bounty")
    public List<String> l(GameCommand command) {
        List<String> players = new ArrayList<>();
        if (command.getArgs().length == 0) {
            for (UUID uuid : GameState.PLAYING) {
                players.add(new GamePlayer(uuid).getDisplayName());
            }
            return players;
        }
        return null;
    }

}