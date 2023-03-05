package net.arvaux.core.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.arvaux.core.Main;
import net.arvaux.core.module.Module;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ServerMap {

    private static final Map<Module, Set<ArvauxServer>> GROUPS = Maps.newHashMap();
    private static final LocalServer LOCAL_SERVER = new LocalServer();

    public static int getPlayersInModule(Module module) {
        return ServerSQL.getTotalPlayersOfType(module.name());
    }

    public static int getPlayersInServer(String server) {
        return ServerSQL.getTotalPlayersOfType(server);
    }

    public static List<ArvauxServer> getServersByGroup(Module module) {
        Set<ArvauxServer> servers = GROUPS.get(module);
        if (servers == null) {
            if (Module.getModule() == module) {
                return Lists.newArrayList(LOCAL_SERVER);
            }

            return Lists.newArrayList();
        }

        if (servers.isEmpty()) {
            if (Module.getModule() == module) {
                return Lists.newArrayList(LOCAL_SERVER);
            }

            return Lists.newArrayList();
        }

        List<ArvauxServer> serverList = Lists.newArrayList(servers);
        if (Module.getModule() == module) {
            serverList.add(LOCAL_SERVER);
        }

        return serverList;
    }

}
