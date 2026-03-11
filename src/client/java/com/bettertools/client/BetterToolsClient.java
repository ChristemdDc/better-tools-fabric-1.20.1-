package com.bettertools.client;

import com.bettertools.client.screen.ToolConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class BetterToolsClient implements ClientModInitializer {

    public static KeyBinding OPEN_CONFIG_KEY;

    @Override
    public void onInitializeClient() {
        OPEN_CONFIG_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.bettertools.open_config",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "category.bettertools"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_CONFIG_KEY.wasPressed()) {
                if (client.player != null) {
                    client.setScreen(new ToolConfigScreen());
                }
            }
        });
    }
}
