package com.bettertools;

import net.minecraft.item.*;

public class ToolHelper {

    public static String getToolType(ItemStack stack) {
        if (stack.isEmpty()) return null;
        Item item = stack.getItem();
        if (item instanceof PickaxeItem) return "pickaxe";
        if (item instanceof AxeItem) return "axe";
        if (item instanceof ShovelItem) return "shovel";
        if (item instanceof SwordItem) return "sword";
        if (item instanceof HoeItem) return "hoe";
        return null;
    }

    public static boolean isToolItem(ItemStack stack) {
        return getToolType(stack) != null;
    }

    public static Item getRepresentativeItem(String toolType) {
        return switch (toolType) {
            case "pickaxe" -> Items.DIAMOND_PICKAXE;
            case "axe" -> Items.DIAMOND_AXE;
            case "shovel" -> Items.DIAMOND_SHOVEL;
            case "sword" -> Items.DIAMOND_SWORD;
            case "hoe" -> Items.DIAMOND_HOE;
            default -> Items.AIR;
        };
    }

    public static String getToolDisplayName(String toolType) {
        return switch (toolType) {
            case "pickaxe" -> "Pickaxe";
            case "axe" -> "Axe";
            case "shovel" -> "Shovel";
            case "sword" -> "Sword";
            case "hoe" -> "Hoe";
            default -> toolType;
        };
    }
}
