package com.bettertools.network;

import net.minecraft.util.Identifier;

/**
 * Holds all network packet identifiers used by the Better Tools mod.
 */
public final class NetworkPackets {

    /**
     * Packet sent from server to a specific client to open the tool configuration screen.
     * This packet is only ever sent to the player who executed the /bettertools command
     * and only after the server has verified that the player has OP level 4.
     */
    public static final Identifier OPEN_TOOL_CONFIG_SCREEN = new Identifier("bettertools", "open_tool_config_screen");

    private NetworkPackets() {}
}
