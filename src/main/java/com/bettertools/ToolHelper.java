package com.bettertools;

import net.minecraft.entity.EquipmentSlot;
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
        if (item instanceof BowItem) return "bow";
        if (item instanceof CrossbowItem) return "crossbow";
        if (item instanceof TridentItem) return "trident";
        if (item instanceof FishingRodItem) return "fishing_rod";
        if (item instanceof ShieldItem) return "shield";
        if (item instanceof ElytraItem) return "elytra";
        if (item instanceof ArmorItem armorItem) {
            EquipmentSlot slot = armorItem.getSlotType();
            if (slot == EquipmentSlot.HEAD) return "helmet";
            if (slot == EquipmentSlot.CHEST) return "chestplate";
            if (slot == EquipmentSlot.LEGS) return "leggings";
            if (slot == EquipmentSlot.FEET) return "boots";
        }
        return null;
    }

    public static boolean isToolItem(ItemStack stack) {
        return getToolType(stack) != null;
    }

    public static Item getRepresentativeItem(String toolType) {
        return switch (toolType) {
            case "pickaxe"    -> Items.DIAMOND_PICKAXE;
            case "axe"        -> Items.DIAMOND_AXE;
            case "shovel"     -> Items.DIAMOND_SHOVEL;
            case "sword"      -> Items.DIAMOND_SWORD;
            case "hoe"        -> Items.DIAMOND_HOE;
            case "bow"        -> Items.BOW;
            case "crossbow"   -> Items.CROSSBOW;
            case "trident"    -> Items.TRIDENT;
            case "fishing_rod"-> Items.FISHING_ROD;
            case "shield"     -> Items.SHIELD;
            case "elytra"     -> Items.ELYTRA;
            case "helmet"     -> Items.DIAMOND_HELMET;
            case "chestplate" -> Items.DIAMOND_CHESTPLATE;
            case "leggings"   -> Items.DIAMOND_LEGGINGS;
            case "boots"      -> Items.DIAMOND_BOOTS;
            default           -> Items.AIR;
        };
    }

    public static String getToolDisplayName(String toolType) {
        return switch (toolType) {
            case "pickaxe"    -> "Pickaxe";
            case "axe"        -> "Axe";
            case "shovel"     -> "Shovel";
            case "sword"      -> "Sword";
            case "hoe"        -> "Hoe";
            case "bow"        -> "Bow";
            case "crossbow"   -> "Crossbow";
            case "trident"    -> "Trident";
            case "fishing_rod"-> "Fishing Rod";
            case "shield"     -> "Shield";
            case "elytra"     -> "Elytra";
            case "helmet"     -> "Helmet";
            case "chestplate" -> "Chestplate";
            case "leggings"   -> "Leggings";
            case "boots"      -> "Boots";
            default           -> toolType;
        };
    }
}
