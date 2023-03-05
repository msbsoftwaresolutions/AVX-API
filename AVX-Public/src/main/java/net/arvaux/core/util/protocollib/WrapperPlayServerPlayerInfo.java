package net.arvaux.core.util.protocollib;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.comphenix.protocol.wrappers.PlayerInfoData;

import java.util.List;

public class WrapperPlayServerPlayerInfo extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.PLAYER_INFO;

    public WrapperPlayServerPlayerInfo() {
        super(new PacketContainer(TYPE), TYPE);

        handle.getModifier().writeDefaults();
    }

    public WrapperPlayServerPlayerInfo(PacketContainer packet) {
        super(packet, TYPE);
    }

    public PlayerInfoAction getAction() {
        return this.handle.getPlayerInfoAction().read(0);
    }

    public void setAction(PlayerInfoAction action) {
        this.handle.getPlayerInfoAction().write(0, action);
    }

    public List<PlayerInfoData> getPlayerInfoData() {
        return this.handle.getPlayerInfoDataLists().read(0);
    }

    public void setPlayerInfoData(List<PlayerInfoData> list) {
        this.handle.getPlayerInfoDataLists().write(0, list);
    }
}
