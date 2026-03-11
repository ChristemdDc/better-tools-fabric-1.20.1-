package com.bettertools;

import com.bettertools.config.BetterToolsConfig;
import com.bettertools.network.BetterToolsNetwork;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SmeltingRecipe;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BetterToolsMod implements ModInitializer {
    public static final String MOD_ID = "bettertools";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Better Tools Mod initialized!");
        BetterToolsConfig.load();
        registerAutoSmelt();
        registerNetworking();
    }

    private void registerAutoSmelt() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient()) return;

            ItemStack heldStack = player.getMainHandStack();
            String toolType = ToolHelper.getToolType(heldStack);
            if (toolType == null) return;

            var config = BetterToolsConfig.getConfig(toolType);
            if (!config.autoSmelt) return;

            ServerWorld serverWorld = (ServerWorld) world;

            Box searchBox = new Box(
                pos.getX() - 0.6, pos.getY() - 0.6, pos.getZ() - 0.6,
                pos.getX() + 1.6, pos.getY() + 1.6, pos.getZ() + 1.6
            );

            List<ItemEntity> items = serverWorld.getEntitiesByClass(
                ItemEntity.class, searchBox, item -> item.age <= 1
            );

            for (ItemEntity itemEntity : items) {
                ItemStack itemStack = itemEntity.getStack();
                if (itemStack.isEmpty()) continue;

                SimpleInventory inv = new SimpleInventory(itemStack.copy());
                Optional<SmeltingRecipe> recipe = serverWorld.getRecipeManager()
                    .getFirstMatch(RecipeType.SMELTING, inv, serverWorld);

                if (recipe.isPresent()) {
                    ItemStack result = recipe.get().getOutput(serverWorld.getRegistryManager()).copy();
                    result.setCount(itemStack.getCount());
                    itemEntity.setStack(result);
                }
            }
        });
    }

    private void registerNetworking() {
        ServerPlayNetworking.registerGlobalReceiver(BetterToolsNetwork.APPLY_ENCHANTMENTS,
            (server, player, handler, buf, responseSender) -> {
                int enchCount = buf.readInt();
                Map<String, Integer> enchantments = new HashMap<>();
                for (int i = 0; i < enchCount; i++) {
                    String id = buf.readString();
                    int level = buf.readInt();
                    enchantments.put(id, level);
                }
                server.execute(() -> {
                    ItemStack held = player.getMainHandStack();
                    if (held.isEmpty()) return;

                    NbtList enchList = new NbtList();
                    for (Map.Entry<String, Integer> entry : enchantments.entrySet()) {
                        if (entry.getValue() <= 0) continue;
                        NbtCompound tag = new NbtCompound();
                        tag.putString("id", entry.getKey());
                        tag.putShort("lvl", entry.getValue().shortValue());
                        enchList.add(tag);
                    }
                    NbtCompound nbt = held.getOrCreateNbt();
                    nbt.put("Enchantments", enchList);
                });
            });
    }
}
