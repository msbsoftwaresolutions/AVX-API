package net.arvaux.core.util;

import net.arvaux.core.server.ArvauxServer;

import java.util.Comparator;

public class UtilServer {

    public static final Comparator<ArvauxServer> SERVER_COMPARATOR = (server1, server2) -> {
        if (server1.getName().length() == server2.getName().length()) {
            return Comparator.comparing(ArvauxServer::getName).compare(server1, server2);
        }

        return Comparator.<ArvauxServer>comparingInt(value -> value.getName().length()).compare(server1, server2);
    };

}
