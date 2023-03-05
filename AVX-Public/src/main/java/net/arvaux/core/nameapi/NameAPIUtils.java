package net.arvaux.core.nameapi;

import com.google.common.primitives.Chars;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.arvaux.core.entity.player.GamePlayer;
import net.arvaux.core.group.Group;
import net.arvaux.core.util.M;
import net.arvaux.core.util.org.bukkit.event.player.PlayerTagEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.*;

public class NameAPIUtils {

    private String prefix;
    private String noPerm;
    private String notPlayer;
    private String lastChatMessage = "NONE";

    private Field nameField, uuidField;

    private List<String> nickNames = new NameAPIFileUtils().getConfig().getStringList("nicks");
    private List<String> blackList = new ArrayList<>();
    private List<String> worldsWithDisabledPrefixAndSuffix = new ArrayList<>();
    private List<String> worldBlackList = new ArrayList<>();
    private List<String> mineSkinIds = new ArrayList<>();
    private ArrayList<UUID> nickedPlayers = new ArrayList<>();
    private ArrayList<UUID> nickOnWorldChangePlayers = new ArrayList<>();
    private HashMap<UUID, String> playerNicknames = new HashMap<>();
    private HashMap<UUID, String> oldDisplayNames = new HashMap<>();
    private HashMap<UUID, String> oldPlayerListNames = new HashMap<>();
    private HashMap<UUID, Boolean> canUseNick = new HashMap<>();
    private HashMap<UUID, Integer> nickNameListPages = new HashMap<>();
    private HashMap<UUID, String[]> oldPermissionsExGroups = new HashMap<>();
    private HashMap<UUID, String> oldPermissionsExPrefixes = new HashMap<>();
    private HashMap<UUID, String> oldPermissionsExSuffixes = new HashMap<>();
    private HashMap<UUID, String> oldCloudNETPrefixes = new HashMap<>();
    private HashMap<UUID, String> oldCloudNETSuffixes = new HashMap<>();
    private HashMap<UUID, Integer> oldCloudNETTagIDS = new HashMap<>();
    private HashMap<UUID, String> oldLuckPermsGroups = new HashMap<>();
    private HashMap<UUID, Object> luckPermsPrefixes = new HashMap<>();
    private HashMap<UUID, Object> luckPermsSuffixes = new HashMap<>();
    private HashMap<UUID, HashMap<String, Long>> oldUltraPermissionsGroups = new HashMap<>();
    private HashMap<UUID, String> ultraPermissionsPrefixes = new HashMap<>();
    private HashMap<UUID, String> ultraPermissionsSuffixes = new HashMap<>();
    private HashMap<UUID, String> nametagEditPrefixes = new HashMap<>();
    private HashMap<UUID, String> nametagEditSuffixes = new HashMap<>();
    private HashMap<UUID, NameAPIScoreboardTeamManager> scoreboardTeamManagers = new HashMap<>();
    private HashMap<UUID, String> nameCache = new HashMap<>();
    private HashMap<UUID, String> lastSkinNames = new HashMap<>();
    private HashMap<UUID, String> lastNickNames = new HashMap<>();
    private HashMap<UUID, String> chatPrefixes = new HashMap<>();
    private HashMap<UUID, String> chatSuffixes = new HashMap<>();
    private HashMap<UUID, String> tabPrefixes = new HashMap<>();
    private HashMap<UUID, String> tabSuffixes = new HashMap<>();
    private HashMap<UUID, String> groupNames = new HashMap<>();
    private HashMap<UUID, String> lastGUITexts = new HashMap<>();
    private HashMap<UUID, String> playersTypingNameInChat = new HashMap<>();

    public boolean isNewVersion() {
        return (Integer.parseInt(NameAPIBase.VERSION.split("_")[1]) > 12);
    }

    @SuppressWarnings("unused")
    public void performRankedNick(Player p, Group group, String skinType, String name) {

        String chatPrefix = "", chatSuffix = "", tabPrefix = "", tabSuffix = "", tagPrefix = "", tagSuffix = "",
                nameWithoutColors = name.replace("§", "");
        String skinName = "";
        boolean isCancelled = false;
        int sortID = 9999;

        if (nameWithoutColors.length() <= 16) {
            if (!(false) // AllowCustomNamesShorterThanThreeCharacters
                    || (nameWithoutColors.length() > 2)) {
                if (!(containsSpecialChars(nameWithoutColors)) || false) { // AllowSpecialCharactersInCustomName
                    if (!(blackList.contains(name.toUpperCase()))) {
                        boolean nickNameIsInUse = false;

                        for (String nickName : playerNicknames.values()) {
                            if (nickName.toUpperCase().equalsIgnoreCase(name.toUpperCase()))
                                nickNameIsInUse = true;
                        }

                        if (!(nickNameIsInUse) || false) { // AllowPlayersToUseSameNickName
                            boolean playerWithNameIsKnown = false;

                            for (Player all : Bukkit.getOnlinePlayers()) {
                                if (all.getName().toUpperCase().equalsIgnoreCase(name.toUpperCase()))
                                    playerWithNameIsKnown = true;
                            }

                            if (Bukkit.getOfflinePlayers() != null) {
                                for (OfflinePlayer all : Bukkit.getOfflinePlayers()) {
                                    if ((all != null) && (all.getName() != null)
                                            && all.getName().toUpperCase().equalsIgnoreCase(name.toUpperCase()))
                                        playerWithNameIsKnown = true;
                                }
                            }

                            if (!(false) // AllowPlayersToNickAsKnownPlayers
                                    && playerWithNameIsKnown)
                                isCancelled = true;

                            if (!(isCancelled)) {
                                String groupName = "";

                                for (int i = 1; i <= 18; i++) {

                                    chatPrefix = group.getPrefix();
                                    chatSuffix = "";
                                    tabPrefix = group.getPrefix();
                                    tabSuffix = "";
                                    tagPrefix = group.getPrefix();
                                    tagSuffix = "";
                                    groupName = group.getName();

                                }

                                if (groupName.isEmpty())
                                    return;

                                String randomColor = "§" + ("0123456789abcdef".charAt(new Random().nextInt(16)));

                                chatPrefix = chatPrefix.replaceAll("%randomColor%", randomColor);
                                chatSuffix = chatSuffix.replaceAll("%randomColor%", randomColor);
                                tabPrefix = tabPrefix.replaceAll("%randomColor%", randomColor);
                                tabSuffix = tabSuffix.replaceAll("%randomColor%", randomColor);
                                tagPrefix = tagPrefix.replaceAll("%randomColor%", randomColor);
                                tagSuffix = tagSuffix.replaceAll("%randomColor%", randomColor);

                                if (skinType.equalsIgnoreCase("DEFAULT"))
                                    skinName = p.getName();
                                else if (skinType.equalsIgnoreCase("NORMAL"))
                                    skinName = new Random().nextBoolean() ? "Steve" : "Alex";
                                else if (skinType.equalsIgnoreCase("RANDOM"))
                                    skinName = nickNames.get(new Random().nextInt(getNickNames().size()));
                                else if (skinType.equalsIgnoreCase("SKINFROMNAME"))
                                    skinName = name;
                                else
                                    skinName = skinType;

                                if (lastSkinNames.containsKey(p.getUniqueId()))
                                    lastSkinNames.remove(p.getUniqueId());

                                if (lastNickNames.containsKey(p.getUniqueId()))
                                    lastNickNames.remove(p.getUniqueId());

                                lastSkinNames.put(p.getUniqueId(), skinName);
                                lastNickNames.put(p.getUniqueId(), name);

                                new NameAPI(p).setGroupName(groupName);

                            } else
                                p.sendMessage(M.EXCEPTION + "Player is known.");
                        } else
                            p.sendMessage(M.EXCEPTION + "Name in use.");
                    } else
                        p.sendMessage(M.EXCEPTION + "Name not allowed.");
                } else
                    p.sendMessage(M.EXCEPTION + "Name contains invalid characters.");
            } else
                p.sendMessage(M.EXCEPTION + "Name is short (chars < 2).");
        } else
            p.sendMessage(M.EXCEPTION + "Name is long (chars > 16).");
    }

    public void performNick(Player p, String customNickName) {

        String name = customNickName.equals("RANDOM") ? nickNames.get((new Random().nextInt(nickNames.size())))
                : customNickName;

        boolean nickNameIsInUse = false;

        for (String nickName : playerNicknames.values()) {
            if (nickName.toUpperCase().equalsIgnoreCase(name.toUpperCase()))
                nickNameIsInUse = true;
        }

        while (nickNameIsInUse) {
            nickNameIsInUse = false;
            name = nickNames.get((new Random().nextInt(nickNames.size())));

            for (String nickName : playerNicknames.values()) {
                if (nickName.toUpperCase().equalsIgnoreCase(name.toUpperCase()))
                    nickNameIsInUse = true;
            }
        }

        boolean serverFull = Bukkit.getOnlinePlayers().size() >= Bukkit.getMaxPlayers();
        String nameWhithoutColors = ChatColor.stripColor(name);
        String[] prefixSuffix = name.split(nameWhithoutColors);
        //@SuppressWarnings("unused")
        String chatPrefix, chatSuffix, tabPrefix, tabSuffix, tagPrefix, tagSuffix;

        if (prefixSuffix.length >= 1) {
            chatPrefix = ChatColor.translateAlternateColorCodes('&', prefixSuffix[0]);

            if (chatPrefix.length() > 16)
                chatPrefix = chatPrefix.substring(0, 16);

            if (prefixSuffix.length >= 2) {
                chatSuffix = ChatColor.translateAlternateColorCodes('&', prefixSuffix[1]);

                if (chatSuffix.length() > 16)
                    chatSuffix = chatSuffix.substring(0, 16);
            } else
                chatSuffix = "§r";

            tabPrefix = chatPrefix;
            tabSuffix = chatSuffix;
            tagPrefix = chatPrefix;
            tagSuffix = chatSuffix;
        } else {
            chatPrefix = (serverFull ? new GamePlayer(p.getUniqueId()).getDisplayGroup().getPrefix()
                    : new GamePlayer(p.getUniqueId()).getDisplayGroup().getPrefix());
            chatSuffix = "";
            tabPrefix = (serverFull ? new GamePlayer(p.getUniqueId()).getDisplayGroup().getPrefix()
                    : new GamePlayer(p.getUniqueId()).getDisplayGroup().getPrefix());
            tabSuffix = "";
            tagPrefix = (serverFull ? new GamePlayer(p.getUniqueId()).getDisplayGroup().getPrefix()
                    : new GamePlayer(p.getUniqueId()).getDisplayGroup().getPrefix());
            tagSuffix = "";
        }

        new NameAPI(p).setGroupName(serverFull ? new GamePlayer(p.getUniqueId()).getDisplayGroup().getName()
                : new GamePlayer(p.getUniqueId()).getDisplayGroup().getName());

        Bukkit.getPluginManager().callEvent(new PlayerTagEvent(p, nameWhithoutColors, nameWhithoutColors, chatPrefix,
                chatSuffix, tabPrefix, tabSuffix, tagPrefix, tagSuffix));
    }

	/*@SuppressWarnings("unused")
	public void performReNick(Player p) {
		if (!(new NameAPI(p).isNicked())) {

			String name = nickNames.get((new Random().nextInt(nickNames.size())));
			boolean isCancelled = false;
			boolean nickNameIsInUse = false;

			for (String nickName : playerNicknames.values()) {
				if (nickName.toUpperCase().equalsIgnoreCase(name.toUpperCase()))
					nickNameIsInUse = true;
			}

			while (nickNameIsInUse) {
				nickNameIsInUse = false;
				name = nickNames.get((new Random().nextInt(nickNames.size())));

				for (String nickName : playerNicknames.values()) {
					if (nickName.toUpperCase().equalsIgnoreCase(name.toUpperCase()))
						nickNameIsInUse = true;
				}
			}

			if (!(nickNameIsInUse) || false) { // AllowPlayersToUseSameNickName
				boolean playerWithNameIsKnown = false;

				for (Player all : Bukkit.getOnlinePlayers()) {
					if (all.getName().toUpperCase().equalsIgnoreCase(name.toUpperCase()))
						playerWithNameIsKnown = true;
				}

				if (Bukkit.getOfflinePlayers() != null) {
					for (OfflinePlayer all : Bukkit.getOfflinePlayers()) {
						if ((all != null) && (all.getName() != null)
								&& all.getName().toUpperCase().equalsIgnoreCase(name.toUpperCase()))
							playerWithNameIsKnown = true;
					}
				}

				if (!(false) && playerWithNameIsKnown) // AllowPlayersToNickAsKnownPlayers
					isCancelled = true;

				if (!(isCancelled)) {
					if (!(name.equalsIgnoreCase(p.getName()))) {

						boolean serverFull = Bukkit.getOnlinePlayers().size() >= Bukkit.getMaxPlayers();
						String prefix = new GamePlayer(p.getUniquieId()).getDisplayGroup().getPrefix();
						String suffix = "";

						new NameAPI(p).setGroupName("ServerFull");

						Bukkit.getPluginManager().callEvent(new PlayerTagEvent(p, nameWhithoutColors, nameWhithoutColors, chatPrefix,
								chatSuffix, tabPrefix, tabSuffix, tagPrefix, tagSuffix));


					} else
						p.sendMessage(M.EXCEPTION_PREFIX + "Can't nick as yourself.");
				} else
					p.sendMessage(M.EXCEPTION_PREFIX + "Player is known.");
			} else
				p.sendMessage(M.EXCEPTION_PREFIX + "Name in use.");
		}
	}*/

    public boolean containsSpecialChars(String s) {
        List<Character> allowCharacters = Chars
                .asList("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_".toCharArray());

        for (char c : s.toCharArray()) {
            if (!(allowCharacters.contains(c)))
                return true;
        }

        return false;
    }

    public GameProfile getDefaultGameProfile() {
        GameProfile gameProfile = new GameProfile(UUID.fromString("8667ba71-b85a-4004-af54-457a9734eed7"), "Steve");
        gameProfile.getProperties().put("textures",
                new Property("textures", getDefaultSkinValue(), getDefaultSkinSignature()));

        return gameProfile;
    }

    public String getDefaultSkinValue() {
        return "ewogICJ0aW1lc3RhbXAiIDogMTU4OTU2NzM1NzQyMSwKICAicHJvZmlsZUlkIiA6ICI5MzRiMmFhOGEyODQ0Yzc3ODg2NDhiNDBiY2IzYjAzMSIsCiAgInByb2ZpbGVOYW1lIiA6ICI0Z2wiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmQzYjA2YzM4NTA0ZmZjMDIyOWI5NDkyMTQ3YzY5ZmNmNTlmZDJlZDc4ODVmNzg1MDIxNTJmNzdiNGQ1MGRlMSIKICAgIH0KICB9Cn0=";
    }

    public String getDefaultSkinSignature() {
        return "WMx2gG2+sM8D4qLHAjiiEWMNwxR6hT0H1uhqoiM1g2IJuM9dODDpgQOEEn7+9K1GxHr45Y7NQ4s9tFk7a5M1BL+IpLUNuZ3PIH2qAuoqVvaYrcYX05e9SBTuHLJCuSo+RjDqyT6AWkE4nYpD6vTLoOQS8Ku+ZXyPdFh2ALW95zQPHh2ZXrlaY+Ktdwf2TEq0vqr8agxzhDaksBxQBgbntu5VS4Z9yJ2hTeftZZALadewYegDI7Dkf/9yWr+6PEcCczhCyrf4xhHzBOjBLMDjg6ZRQhGGscv6dLP7hdqbIRPwvZ/tH0NW1GE1UWqof5TspCmlHNI592djxo2MqDhA0LrT4jbQfsnfZze0urQwMQG1V3fDDrf8kfZdD+7H29UmFAaTvfqMkwqKalPExm75oqeFole4qzxifl1Rv1i3bJy8ZZlgixZzxhl3idDIP5IkPnQHt9YFOEpOQLWtJV8dTixCP5TvYVQRtXkgFtzIUljDNkrqUmqlkeXPlZR27lEykuLIGPrV4U/bXanNVpHKcCMsD7vzFC1wu1XS0JWozN8SFZdwmVTCFpmBgqeKHBPbIuTqdOF0+YZ7xoxc7W869vgFJSaJ7jdlcTsHFt+AQcWUxlSqoV1n1kyQ7hF/zcjoi2YtAMy9XGh1IODS+UPl/edqs7Sq+fA054/ivaqzeh4=";
    }

    public String getPrefix() {
        return prefix;
    }

    public String getNoPerm() {
        return noPerm;
    }

    public String getNotPlayer() {
        return notPlayer;
    }

    public String getLastChatMessage() {
        return lastChatMessage;
    }

    public void setLastChatMessage(String lastChatMessage) {
        this.lastChatMessage = lastChatMessage;
    }

    public Field getNameField() {
        return nameField;
    }

    public void setNameField(Field nameField) {
        this.nameField = nameField;
    }

    public Field getUUIDField() {
        return uuidField;
    }

    public void setUUIDField(Field uuidField) {
        this.uuidField = uuidField;
    }

    public ArrayList<UUID> getNickedPlayers() {
        return nickedPlayers;
    }

    public ArrayList<UUID> getNickOnWorldChangePlayers() {
        return nickOnWorldChangePlayers;
    }

    public HashMap<UUID, String> getPlayerNicknames() {
        return playerNicknames;
    }

    public List<String> getNickNames() {
        return nickNames;
    }

    public List<String> getBlackList() {
        return blackList;
    }

    public List<String> getWorldsWithDisabledPrefixAndSuffix() {
        return worldsWithDisabledPrefixAndSuffix;
    }

    public List<String> getWorldBlackList() {
        return worldBlackList;
    }

    public List<String> getMineSkinIds() {
        return mineSkinIds;
    }

    public HashMap<UUID, String> getOldDisplayNames() {
        return oldDisplayNames;
    }

    public HashMap<UUID, String> getOldPlayerListNames() {
        return oldPlayerListNames;
    }

    public HashMap<UUID, Boolean> getCanUseNick() {
        return canUseNick;
    }

    public HashMap<UUID, Integer> getNickNameListPages() {
        return nickNameListPages;
    }

    public HashMap<UUID, String[]> getOldPermissionsExGroups() {
        return oldPermissionsExGroups;
    }

    public HashMap<UUID, String> getOldPermissionsExPrefixes() {
        return oldPermissionsExPrefixes;
    }

    public HashMap<UUID, String> getOldPermissionsExSuffixes() {
        return oldPermissionsExSuffixes;
    }

    public HashMap<UUID, String> getOldCloudNETPrefixes() {
        return oldCloudNETPrefixes;
    }

    public HashMap<UUID, String> getOldCloudNETSuffixes() {
        return oldCloudNETSuffixes;
    }

    public HashMap<UUID, Integer> getOldCloudNETTagIDS() {
        return oldCloudNETTagIDS;
    }

    public HashMap<UUID, String> getOldLuckPermsGroups() {
        return oldLuckPermsGroups;
    }

    public HashMap<UUID, Object> getLuckPermsPrefixes() {
        return luckPermsPrefixes;
    }

    public HashMap<UUID, Object> getLuckPermsSuffixes() {
        return luckPermsSuffixes;
    }

    public HashMap<UUID, HashMap<String, Long>> getOldUltraPermissionsGroups() {
        return oldUltraPermissionsGroups;
    }

    public HashMap<UUID, String> getUltraPermissionsPrefixes() {
        return ultraPermissionsPrefixes;
    }

    public HashMap<UUID, String> getUltraPermissionsSuffixes() {
        return ultraPermissionsSuffixes;
    }

    public HashMap<UUID, String> getNametagEditPrefixes() {
        return nametagEditPrefixes;
    }

    public HashMap<UUID, String> getNametagEditSuffixes() {
        return nametagEditSuffixes;
    }

    public HashMap<UUID, NameAPIScoreboardTeamManager> getScoreboardTeamManagers() {
        return scoreboardTeamManagers;
    }

    public HashMap<UUID, String> getNameCache() {
        return nameCache;
    }

    public HashMap<UUID, String> getLastSkinNames() {
        return lastSkinNames;
    }

    public HashMap<UUID, String> getLastNickNames() {
        return lastNickNames;
    }

    public HashMap<UUID, String> getChatPrefixes() {
        return chatPrefixes;
    }

    public HashMap<UUID, String> getChatSuffixes() {
        return chatSuffixes;
    }

    public HashMap<UUID, String> getTabPrefixes() {
        return tabPrefixes;
    }

    public HashMap<UUID, String> getTabSuffixes() {
        return tabSuffixes;
    }

    public HashMap<UUID, String> getGroupNames() {
        return groupNames;
    }

    public HashMap<UUID, String> getLastGUITexts() {
        return lastGUITexts;
    }

    public HashMap<UUID, String> getPlayersTypingNameInChat() {
        return playersTypingNameInChat;
    }

}