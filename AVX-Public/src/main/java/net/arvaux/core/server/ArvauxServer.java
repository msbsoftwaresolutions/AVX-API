package net.arvaux.core.server;

import net.arvaux.core.module.Module;

import java.util.Objects;

/**
 * Info about a particular server.
 */
public class ArvauxServer {

    private boolean valid;
    private String name;
    private String motd;
    private int playerCount = -1;
    private int maxPlayers = -1;
    private double tps;
    private long lastHeartbeat;
    private Module serverModule;

    public ArvauxServer(String name, Module serverModule) {
        this.name = name;
        this.lastHeartbeat = System.currentTimeMillis();
        this.valid = true;
        this.serverModule = serverModule;
    }

    public String getName() {
        return name;
    }

    public Module getServerModule() {
        return serverModule;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ArvauxServer)) {
            return false;
        }

        ArvauxServer that = (ArvauxServer) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "ArvauxServer{" +
                "valid=" + valid +
                ", name='" + name + '\'' +
                ", serverModule=" + serverModule +
                '}';
    }
}
