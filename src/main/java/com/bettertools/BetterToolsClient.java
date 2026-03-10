package com.bettertools;

import com.bettertools.client.screen.ToolConfigScreen;
import com.bettertools.network.NetworkPackets;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

/**
 * Client-side initializer for the Better Tools mod.
 *
 * <p>Registers the client-side packet handler that listens for the
 * {@link NetworkPackets#OPEN_TOOL_CONFIG_SCREEN} packet.  When received, it
 * opens the {@link ToolConfigScreen} for the local player.  Because the server
 * only sends this packet to the specific player who issued the /bettertools command
 * and has already verified OP level 4, no permission check is needed here.</p>
 */
@Environment(EnvType.CLIENT)
public class BetterToolsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Register handler for the packet that tells THIS client to open the tool config screen.
        // The server guarantees this packet is sent only to the player who ran /bettertools
        // after confirming they have OP level 4.
        ClientPlayNetworking.registerGlobalReceiver(
                NetworkPackets.OPEN_TOOL_CONFIG_SCREEN,
                (client, handler, buf, responseSender) ->
                        client.execute(() -> MinecraftClient.getInstance().setScreen(new ToolConfigScreen()))
        );
    }
}
