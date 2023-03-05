package net.arvaux.core.nameapi;

import net.arvaux.core.Main;
import net.arvaux.core.entity.player.GamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;

public class NameAPI {

    private Player p;

    public NameAPI(Player p) {
        this.p = p;
    }

    public NameAPI(OfflinePlayer player) {
        this.p = p;
    }

    public void setPlayerListName(String name) {
        NameAPINMSTagManager nmsNickManager = new NameAPINMSTagManager();

        nmsNickManager.updatePlayerListName(p, name);
    }

    public void changeSkin(String skinName) {
        NameAPINMSTagManager nmsNickManager = new NameAPINMSTagManager();

        if (skinName != null) {
            // nmsNickManager.updateSkin_1_8_R1(p, skinName);
            nmsNickManager.updateSkin(p, skinName);

        }
    }

    public void updatePlayer() {
        updatePlayer(NameAPINMSTagManager.UpdateType.UPDATE, null, false);
    }

    public void updatePlayer(NameAPINMSTagManager.UpdateType type, String skinName, boolean forceUpdate) {
        new NameAPINMSTagManager().updatePlayer(p, type, skinName, forceUpdate);
    }

    public void setName(String nickName) {
        NameAPINMSTagManager nmsNickManager = new NameAPINMSTagManager();
        nmsNickManager.updateName(p, nickName);
    }

    public void nickPlayer(String nickName) {
        nickPlayer(nickName, nickName);
    }

    public void nickPlayer(String nickName, String skinName) {
        NameAPIUtils utils = new NameAPIUtils();

        if (!(utils.getOldDisplayNames().containsKey(p.getUniqueId())))
            utils.getOldDisplayNames().put(p.getUniqueId(),
                    (p.getDisplayName() != null) ? p.getDisplayName() : p.getName());

        if (!(utils.getOldPlayerListNames().containsKey(p.getUniqueId())))
            utils.getOldPlayerListNames().put(p.getUniqueId(),
                    (p.getPlayerListName() != null) ? p.getPlayerListName() : p.getName());

        utils.getNickedPlayers().add(p.getUniqueId());
        utils.getPlayerNicknames().put(p.getUniqueId(), nickName);

        // NO COLOR CODES
        setName(nickName);
        updatePlayer(NameAPINMSTagManager.UpdateType.NICK, skinName, false);

    }

    public void unnickPlayer() {
        unnickPlayerWithoutRemovingMySQL(false);
    }

    public void unnickPlayerWithoutRemovingMySQL(boolean isQuitUnnick) {
        NameAPIUtils utils = new NameAPIUtils();
        String nickName = getRealName();

        setName(nickName);
        updatePlayer(isQuitUnnick ? NameAPINMSTagManager.UpdateType.QUIT : NameAPINMSTagManager.UpdateType.UNNICK, nickName, true);

        utils.getNickedPlayers().remove(p.getUniqueId());
        utils.getPlayerNicknames().remove(p.getUniqueId());

        if (utils.getChatPrefixes().containsKey(p.getUniqueId()))
            utils.getChatPrefixes().remove(p.getUniqueId());

        if (utils.getChatSuffixes().containsKey(p.getUniqueId()))
            utils.getChatSuffixes().remove(p.getUniqueId());

        if (utils.getTabPrefixes().containsKey(p.getUniqueId()))
            utils.getTabPrefixes().remove(p.getUniqueId());

        if (utils.getTabSuffixes().containsKey(p.getUniqueId()))
            utils.getTabSuffixes().remove(p.getUniqueId());

        unsetGroupName();

        if (utils.getOldDisplayNames().containsKey(p.getUniqueId())
                && utils.getOldPlayerListNames().containsKey(p.getUniqueId())) {
            UUID uuid = p.getUniqueId();
            // String oldDisplayName = getOldDisplayName();
            // String oldPlayerListName = getOldPlayerListName();

            Bukkit.getScheduler().runTaskLater(Main.getInstance(), new Runnable() {

                @Override
                public void run() {
                    if (p.isOnline()) {
                        // p.setDisplayName(oldDisplayName);
                        // setPlayerListName(oldPlayerListName);

                        utils.getOldDisplayNames().remove(uuid);
                        utils.getOldPlayerListNames().remove(uuid);
                    }
                }
            }, 5L);
        }
    }

    public String getRealName() {
        return new GamePlayer(p.getPlayer().getUniqueId()).getName();
    }

    public String getChatPrefix() {

        return null;
    }

    public void setChatPrefix(String chatPrefix) {
        NameAPIUtils utils = new NameAPIUtils();

        if (utils.getChatPrefixes().containsKey(p.getUniqueId()))
            utils.getChatPrefixes().remove(p.getUniqueId());

        utils.getChatPrefixes().put(p.getUniqueId(), ChatColor.translateAlternateColorCodes('&', chatPrefix));

        p.setDisplayName(utils.getChatPrefixes().get(p.getUniqueId()) + getNickName()
                + utils.getChatSuffixes().get(p.getUniqueId()));
        setPlayerListName(utils.getTabPrefixes().get(p.getUniqueId()) + getNickName()
                + utils.getTabSuffixes().get(p.getUniqueId()));

    }

    public String getChatSuffix() {

        return null;
    }

    public void setChatSuffix(String chatSuffix) {
        NameAPIUtils utils = new NameAPIUtils();

        if (utils.getChatSuffixes().containsKey(p.getUniqueId()))
            utils.getChatSuffixes().remove(p.getUniqueId());

        utils.getChatSuffixes().put(p.getUniqueId(), ChatColor.translateAlternateColorCodes('&', chatSuffix));

        p.setDisplayName(utils.getChatPrefixes().get(p.getUniqueId()) + getNickName()
                + utils.getChatSuffixes().get(p.getUniqueId()));
        setPlayerListName(utils.getTabPrefixes().get(p.getUniqueId()) + getNickName()
                + utils.getTabSuffixes().get(p.getUniqueId()));

    }

    public String getTabPrefix() {

        return null;
    }

    public void setTabPrefix(String tabPrefix) {
        NameAPIUtils utils = new NameAPIUtils();

        if (utils.getTabPrefixes().containsKey(p.getUniqueId()))
            utils.getTabPrefixes().remove(p.getUniqueId());

        utils.getTabPrefixes().put(p.getUniqueId(), ChatColor.translateAlternateColorCodes('&', tabPrefix));

        p.setDisplayName(utils.getChatPrefixes().get(p.getUniqueId()) + getNickName()
                + utils.getChatSuffixes().get(p.getUniqueId()));
        setPlayerListName(utils.getTabPrefixes().get(p.getUniqueId()) + getNickName()
                + utils.getTabSuffixes().get(p.getUniqueId()));

    }

    public String getTabSuffix() {

        return null;
    }

    public void setTabSuffix(String tabSuffix) {
        NameAPIUtils utils = new NameAPIUtils();

        if (utils.getTabSuffixes().containsKey(p.getUniqueId()))
            utils.getTabSuffixes().remove(p.getUniqueId());

        utils.getTabSuffixes().put(p.getUniqueId(), ChatColor.translateAlternateColorCodes('&', tabSuffix));

        p.setDisplayName(utils.getChatPrefixes().get(p.getUniqueId()) + getNickName()
                + utils.getChatSuffixes().get(p.getUniqueId()));
        setPlayerListName(utils.getTabPrefixes().get(p.getUniqueId()) + getNickName()
                + utils.getTabSuffixes().get(p.getUniqueId()));

    }

    public String getTagPrefix() {
        NameAPIUtils utils = new NameAPIUtils();

        return (utils.getScoreboardTeamManagers().containsKey(p.getUniqueId())
                ? utils.getScoreboardTeamManagers().get(p.getUniqueId()).getPrefix()
                : "");
    }

    public void setTagPrefix(String tagPrefix) {
        NameAPIUtils utils = new NameAPIUtils();

        if (utils.getScoreboardTeamManagers().containsKey(p.getUniqueId()))
            utils.getScoreboardTeamManagers().get(p.getUniqueId()).setPrefix(tagPrefix);
    }

    public String getTagSuffix() {
        NameAPIUtils utils = new NameAPIUtils();

        return (utils.getScoreboardTeamManagers().containsKey(p.getUniqueId())
                ? utils.getScoreboardTeamManagers().get(p.getUniqueId()).getSuffix()
                : "");
    }

    public void setTagSuffix(String tagSuffix) {
        NameAPIUtils utils = new NameAPIUtils();

        if (utils.getScoreboardTeamManagers().containsKey(p.getUniqueId()))
            utils.getScoreboardTeamManagers().get(p.getUniqueId()).setSuffix(tagSuffix);
    }

    public boolean isNicked() {
        for (Player all : Bukkit.getOnlinePlayers()) {
            GamePlayer a = new GamePlayer(all.getUniqueId());
            if (a.isDisguise())
                return true;
        }
        return false;
    }

    public String getRandomStringFromList(ArrayList<String> list) {
        return list.size() != 0 ? list.get((new Random()).nextInt(list.size())) : p.getName();
    }

    public String getRandomName() {
        NameAPIUtils utils = new NameAPIUtils();

        return utils.getNickNames().get((new Random()).nextInt(utils.getNickNames().size()));
    }

    public String getNickName() {
        NameAPIUtils utils = new NameAPIUtils();

        return (utils.getPlayerNicknames().containsKey(p.getUniqueId())
                ? utils.getPlayerNicknames().get(p.getUniqueId())
                : p.getName());
    }

    public String getNickFormat() {
        return getChatPrefix() + getNickName() + getChatSuffix();
    }

    public String getOldDisplayName() {
        NameAPIUtils utils = new NameAPIUtils();

        return utils.getOldDisplayNames().containsKey(p.getUniqueId()) ? utils.getOldDisplayNames().get(p.getUniqueId())
                : p.getName();
    }

    public String getOldPlayerListName() {
        NameAPIUtils utils = new NameAPIUtils();

        return utils.getOldPlayerListNames().containsKey(p.getUniqueId())
                ? utils.getOldPlayerListNames().get(p.getUniqueId())
                : p.getName();
    }

    public String getGroupName() {
        NameAPIUtils utils = new NameAPIUtils();

        return utils.getGroupNames().containsKey(p.getUniqueId()) ? utils.getGroupNames().get(p.getUniqueId()) : "NONE";
    }

    public void setGroupName(String rank) {
        NameAPIUtils utils = new NameAPIUtils();

        utils.getGroupNames().put(p.getUniqueId(), rank);
    }

    public void unsetGroupName() {
        NameAPIUtils utils = new NameAPIUtils();

        if (utils.getGroupNames().containsKey(p.getUniqueId()))
            utils.getGroupNames().remove(p.getUniqueId());
    }

    public void updatePrefixSuffix(String tagPrefix, String tagSuffix, String chatPrefix, String chatSuffix,
                                   String tabPrefix, String tabSuffix) {
        updatePrefixSuffix(tagPrefix, tagSuffix, chatPrefix, chatSuffix, tabPrefix, tabSuffix, 9999, "NONE");
    }

    public void updatePrefixSuffix(String tagPrefix, String tagSuffix, String chatPrefix, String chatSuffix,
                                   String tabPrefix, String tabSuffix, int sortID, String groupName) {
        NameAPIUtils utils = new NameAPIUtils();

        String finalTabPrefix = tabPrefix, finalTabSuffix = tabSuffix;

        if (utils.getChatPrefixes().containsKey(p.getUniqueId()))
            utils.getChatPrefixes().remove(p.getUniqueId());

        if (utils.getChatSuffixes().containsKey(p.getUniqueId()))
            utils.getChatSuffixes().remove(p.getUniqueId());

        if (utils.getTabPrefixes().containsKey(p.getUniqueId()))
            utils.getTabPrefixes().remove(p.getUniqueId());

        if (utils.getTabSuffixes().containsKey(p.getUniqueId()))
            utils.getTabSuffixes().remove(p.getUniqueId());

        utils.getChatPrefixes().put(p.getUniqueId(), chatPrefix);
        utils.getChatSuffixes().put(p.getUniqueId(), chatSuffix);
        utils.getTabPrefixes().put(p.getUniqueId(), tabPrefix);
        utils.getTabSuffixes().put(p.getUniqueId(), tabSuffix);

        if (utils.getScoreboardTeamManagers().containsKey(p.getUniqueId()))
            utils.getScoreboardTeamManagers().remove(p.getUniqueId());

        utils.getScoreboardTeamManagers().put(p.getUniqueId(),
                new NameAPIScoreboardTeamManager(p, tagPrefix, tagSuffix, sortID, groupName));

        NameAPIScoreboardTeamManager sbtm = utils.getScoreboardTeamManagers().get(p.getUniqueId());

        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                UUID uuid = p.getUniqueId();

                sbtm.destroyTeam();

                if (utils.getNickedPlayers().contains(uuid) && p.isOnline()) {
                    sbtm.createTeam();

                    String tmpTabPrefix = finalTabPrefix;
                    String tmpTabSuffix = finalTabSuffix;

                    setPlayerListName(tmpTabPrefix + p.getName() + tmpTabSuffix);
                }
            }
        }, 0, 1000);

        setPlayerListName(tabPrefix + p.getName() + tabSuffix);

        p.setDisplayName(chatPrefix + p.getName() + chatSuffix);

    }

}
