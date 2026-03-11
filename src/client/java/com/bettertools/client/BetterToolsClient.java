package com.bettertools.client;

import com.bettertools.client.screen.ToolConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class BetterToolsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(ClientCommandManager.literal("bettertools")
                .requires(source -> source.hasPermissionLevel(4))
                .executes(context -> {
                    MinecraftClient client = context.getSource().getClient();
                    client.execute(() -> client.setScreen(new ToolConfigScreen()));
                    return 1;
                })
            )
        );
    }
}
