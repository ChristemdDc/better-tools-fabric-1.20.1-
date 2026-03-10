package com.bettertools.command;

import com.bettertools.network.NetworkPackets;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * Registers the /bettertools command.
 *
 * <p>Access is restricted to operators with the maximum permission level (4).
 * When the command is executed, a packet is sent exclusively to the player who
 * issued the command so that only their client opens the tool-configuration screen.
 * No other player receives this packet, preventing unauthorised access to the menu.</p>
 */
public final class BetterToolsCommand {

    /** Maximum OP permission level required to use this command. */
    private static final int REQUIRED_PERMISSION_LEVEL = 4;

    private BetterToolsCommand() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                registerCommand(dispatcher));
    }

    private static void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("bettertools")
                        // Only players with the maximum OP level (4) may execute this command.
                        .requires(source -> source.hasPermissionLevel(REQUIRED_PERMISSION_LEVEL))
                        .executes(BetterToolsCommand::openMenu)
        );
    }

    /**
     * Opens the tool-configuration menu for the player who executed the command.
     *
     * <p>The server sends an {@code OPEN_TOOL_CONFIG_SCREEN} packet directly to the
     * executing player.  Because the packet is addressed to a single
     * {@link ServerPlayerEntity}, no other connected player receives it and therefore
     * no other player's screen is opened.</p>
     *
     * @param context the command context containing the executing source
     * @return 1 on success, 0 if the source is not a player
     */
    private static int openMenu(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendError(Text.literal("This command can only be used by a player."));
            return 0;
        }

        // Send the screen-open packet exclusively to the player who ran the command.
        ServerPlayNetworking.send(player, NetworkPackets.OPEN_TOOL_CONFIG_SCREEN, PacketByteBufs.empty());
        return 1;
    }
}
