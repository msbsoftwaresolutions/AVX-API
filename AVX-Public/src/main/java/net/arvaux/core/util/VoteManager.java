package net.arvaux.core.util;

import net.arvaux.core.entity.player.GamePlayer;
import net.arvaux.core.group.Group;
import net.arvaux.core.module.Module;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class VoteManager {
    private static Map<UUID, String> voted; // voted and map voted for
    private static HashMap<String, Integer> votes; // map and map votes
    @SuppressWarnings("unused")
    private List<String> maps;
    private List<String> randommaps;
    @SuppressWarnings("unused")

    //Usage: new VoteManager(game);
    public VoteManager() {
        this.randommaps = new ArrayList<>();
        VoteManager.voted = new HashMap<>();
        VoteManager.votes = new HashMap<>();
        this.maps = new ArrayList<>();
    }

    //Add the vote to the player
    public void addVote(GamePlayer player, String map) {
        VoteManager.voted.put(player.getUUID(), map);
        Integer currentVotes = VoteManager.votes.get(map);
        VoteManager.votes.put(map, currentVotes + votePower(player));
    }

    //Delete vote
    public static void delVote(GamePlayer player) {
        if (VoteManager.voted == null || VoteManager.votes == null)
            return;
        String map = VoteManager.voted.get(player.getUUID());
        Integer currentVotes = VoteManager.votes.get(map);
        if (VoteManager.voted.containsKey(player.getUUID())) {
            VoteManager.votes.put(map, currentVotes - votePower(player));
            VoteManager.voted.remove(player.getUUID());
        }
    }

    public HashMap<String, Integer> getVotes() {
        return votes;
    }

    public static int votePower(GamePlayer player) {
        if (player.hasGroup(Group.OWNER) || player.hasGroup(Group.ADMIN) || player.hasGroup(Group.PARTNER)) {
            return 10;
        } else if (player.hasGroup(Group.VIP) || player.hasGroup(Group.HUSTLER) ||
                player.hasGroup(Group.BUILDER) ||
                player.hasGroup(Group.MOD) || player.hasGroup(Group.SRMOD)) {
            return 6;
        } else if (player.hasGroup(Group.ATHENE)) {
            return 5;
        } else if (player.hasGroup(Group.PRO)) {
            return 4;
        } else if (player.hasGroup(Group.MINERVA)) {
            return 3;
        } else if (player.hasGroup(Group.NINOX)) {
            return 2;
        } else if (player.hasGroup(Group.REGULAR)) {
            return 1;
        }
        return 1;
    }

    public List<String> getRandomMaps() {
        return randommaps;
    }

    public Map<UUID, String> getVoted() {
        return voted;
    }


    public List<String> getMaps() {
        return maps;
    }

    //Gets the Winning Map!
    public String getWinningMap() {
        Map.Entry<String, Integer> winningMap = null;

        for (Map.Entry<String, Integer> entry : VoteManager.votes.entrySet()) {
            if (winningMap == null || entry.getValue() > winningMap.getValue()) {
                winningMap = entry;
            }
        }
        return winningMap.getKey();
    }

}
