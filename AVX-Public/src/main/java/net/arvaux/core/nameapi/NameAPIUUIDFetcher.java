package net.arvaux.core.nameapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.util.UUIDTypeAdapter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class NameAPIUUIDFetcher {

    private final String UUID_URL = "https://api.mojang.com/users/profiles/minecraft/%s?at=%d";
    private final String NAME_URL = "https://api.mojang.com/user/profiles/%s/names";
    private Gson gson = new GsonBuilder().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();
    private Map<String, UUID> uuidCache = new HashMap<String, UUID>();
    private Map<UUID, String> nameCache = new HashMap<UUID, String>();

    private String name;
    private UUID id;

    public UUID getUUID(String name) {
        return getUUIDAt(name, System.currentTimeMillis());
    }

    public UUID getUUIDAt(String name, long timestamp) {

        name = name.toLowerCase();

        if (uuidCache.containsKey(name))
            return uuidCache.get(name);

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(String.format(UUID_URL, name, timestamp / 1000))
                    .openConnection();
            connection.setReadTimeout(5000);

            NameAPIUUIDFetcher data = gson.fromJson(new BufferedReader(new InputStreamReader(connection.getInputStream())),
                    NameAPIUUIDFetcher.class);

            uuidCache.put(name, data.id);
            nameCache.put(data.id, data.name);

            return data.id;
        } catch (Exception e) {
            NameAPIFileUtils nickNameFileUtils = new NameAPIFileUtils();

            List<String> list = nickNameFileUtils.getConfig().getStringList("nicks");
            ArrayList<String> toRemove = new ArrayList<>();
            final String finalName = name;

            list.stream().filter(s -> s.equalsIgnoreCase(finalName)).forEach(s -> toRemove.add(s));

            if (toRemove.size() >= 1) {
                toRemove.forEach(s -> {
                    list.remove(s);
                    NameAPIBase.UTILS.getNickNames().remove(s);
                });

                nickNameFileUtils.getConfig().set("nicks", list);
                nickNameFileUtils.saveFile();

                // utils.sendConsole("§cThere is no account with username §6" + name + " §cin
                // the mojang database");
            }
        }

        return null;
    }

    public String getName(String name, UUID uuid) {
        if (nameCache.containsKey(uuid))
            return nameCache.get(uuid);

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(
                    String.format(NAME_URL, UUIDTypeAdapter.fromUUID(uuid))).openConnection();
            connection.setReadTimeout(5000);

            NameAPIUUIDFetcher[] nameHistory = gson.fromJson(
                    new BufferedReader(new InputStreamReader(connection.getInputStream())), NameAPIUUIDFetcher[].class);
            NameAPIUUIDFetcher currentNameData = nameHistory[nameHistory.length - 1];
            uuidCache.put(currentNameData.name.toLowerCase(), uuid);
            nameCache.put(uuid, currentNameData.name);

            return currentNameData.name;
        } catch (Exception e) {
        }

        return name;
    }

}