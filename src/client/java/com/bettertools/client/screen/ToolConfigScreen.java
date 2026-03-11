package com.bettertools.client.screen;

import com.bettertools.ToolHelper;
import com.bettertools.config.BetterToolsConfig;
import com.bettertools.config.ToolConfig;
import com.bettertools.network.BetterToolsNetwork;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.*;

@Environment(EnvType.CLIENT)
public class ToolConfigScreen extends Screen {

    private static final String[] TOOL_TYPES = {
        "pickaxe", "axe", "shovel", "sword", "hoe",
        "bow", "crossbow", "trident",
        "fishing_rod", "shield", "elytra",
        "helmet", "chestplate", "leggings", "boots"
    };

    private static final Set<String> MINING_TOOLS = Set.of(
        "pickaxe", "axe", "shovel", "sword", "hoe"
    );

    private int panelX, panelY, panelW, panelH;
    private final int leftW = 110;
    private int selectedTool = 0;
    private int toolScroll   = 0;

    private final Map<String, ToolConfig> working = new HashMap<>();
    private final List<EnchRow> enchRows = new ArrayList<>();

    private int enchScroll   = 0;
    private int enchSectionY = 0;
    private int enchLabelY   = 0;

    private static final int ENCH_ROW_H  = 18;
    private static final int ENCH_VISIBLE = 5;
    private static final int TOOL_BTN_H  = 22;

    private static final int COL_BG       = 0x88000000;
    private static final int COL_PANEL    = 0xCC1A1A3A;
    private static final int COL_PANEL2   = 0xCC12122A;
    private static final int COL_BORDER   = 0xFF4A4A8A;
    private static final int COL_TITLE    = 0xFFE8D48B;
    private static final int COL_TEXT     = 0xFFD0D0E8;
    private static final int COL_SELECTED = 0xFF2A2A6A;
    private static final int COL_DIVIDER  = 0xFF303060;

    private static final Map<String, List<Enchantment>> TOOL_ENCHANTS = new LinkedHashMap<>();

    static {
        TOOL_ENCHANTS.put("pickaxe", Arrays.asList(
            Enchantments.EFFICIENCY, Enchantments.FORTUNE, Enchantments.SILK_TOUCH,
            Enchantments.UNBREAKING, Enchantments.MENDING
        ));
        TOOL_ENCHANTS.put("axe", Arrays.asList(
            Enchantments.EFFICIENCY, Enchantments.FORTUNE, Enchantments.SILK_TOUCH,
            Enchantments.UNBREAKING, Enchantments.MENDING,
            Enchantments.SHARPNESS, Enchantments.SMITE, Enchantments.BANE_OF_ARTHROPODS
        ));
        TOOL_ENCHANTS.put("shovel", Arrays.asList(
            Enchantments.EFFICIENCY, Enchantments.FORTUNE, Enchantments.SILK_TOUCH,
            Enchantments.UNBREAKING, Enchantments.MENDING
        ));
        TOOL_ENCHANTS.put("sword", Arrays.asList(
            Enchantments.SHARPNESS, Enchantments.SMITE, Enchantments.BANE_OF_ARTHROPODS,
            Enchantments.LOOTING, Enchantments.FIRE_ASPECT, Enchantments.SWEEPING_EDGE,
            Enchantments.UNBREAKING, Enchantments.MENDING, Enchantments.KNOCKBACK
        ));
        TOOL_ENCHANTS.put("hoe", Arrays.asList(
            Enchantments.EFFICIENCY, Enchantments.FORTUNE, Enchantments.SILK_TOUCH,
            Enchantments.UNBREAKING, Enchantments.MENDING
        ));
        TOOL_ENCHANTS.put("bow", Arrays.asList(
            Enchantments.POWER, Enchantments.PUNCH, Enchantments.FLAME,
            Enchantments.INFINITY, Enchantments.UNBREAKING, Enchantments.MENDING,
            Enchantments.VANISHING_CURSE
        ));
        TOOL_ENCHANTS.put("crossbow", Arrays.asList(
            Enchantments.MULTISHOT, Enchantments.PIERCING, Enchantments.QUICK_CHARGE,
            Enchantments.UNBREAKING, Enchantments.MENDING, Enchantments.VANISHING_CURSE
        ));
        TOOL_ENCHANTS.put("trident", Arrays.asList(
            Enchantments.LOYALTY, Enchantments.IMPALING, Enchantments.RIPTIDE,
            Enchantments.CHANNELING, Enchantments.UNBREAKING, Enchantments.MENDING,
            Enchantments.VANISHING_CURSE
        ));
        TOOL_ENCHANTS.put("fishing_rod", Arrays.asList(
            Enchantments.LUCK_OF_THE_SEA, Enchantments.LURE,
            Enchantments.UNBREAKING, Enchantments.MENDING, Enchantments.VANISHING_CURSE
        ));
        TOOL_ENCHANTS.put("shield", Arrays.asList(
            Enchantments.UNBREAKING, Enchantments.MENDING, Enchantments.VANISHING_CURSE
        ));
        TOOL_ENCHANTS.put("elytra", Arrays.asList(
            Enchantments.UNBREAKING, Enchantments.MENDING,
            Enchantments.BINDING_CURSE, Enchantments.VANISHING_CURSE
        ));
        TOOL_ENCHANTS.put("helmet", Arrays.asList(
            Enchantments.PROTECTION, Enchantments.FIRE_PROTECTION,
            Enchantments.BLAST_PROTECTION, Enchantments.PROJECTILE_PROTECTION,
            Enchantments.RESPIRATION, Enchantments.AQUA_AFFINITY,
            Enchantments.THORNS, Enchantments.UNBREAKING, Enchantments.MENDING,
            Enchantments.BINDING_CURSE, Enchantments.VANISHING_CURSE
        ));
        TOOL_ENCHANTS.put("chestplate", Arrays.asList(
            Enchantments.PROTECTION, Enchantments.FIRE_PROTECTION,
            Enchantments.BLAST_PROTECTION, Enchantments.PROJECTILE_PROTECTION,
            Enchantments.THORNS, Enchantments.UNBREAKING, Enchantments.MENDING,
            Enchantments.BINDING_CURSE, Enchantments.VANISHING_CURSE
        ));
        TOOL_ENCHANTS.put("leggings", Arrays.asList(
            Enchantments.PROTECTION, Enchantments.FIRE_PROTECTION,
            Enchantments.BLAST_PROTECTION, Enchantments.PROJECTILE_PROTECTION,
            Enchantments.THORNS, Enchantments.SWIFT_SNEAK,
            Enchantments.UNBREAKING, Enchantments.MENDING,
            Enchantments.BINDING_CURSE, Enchantments.VANISHING_CURSE
        ));
        TOOL_ENCHANTS.put("boots", Arrays.asList(
            Enchantments.PROTECTION, Enchantments.FIRE_PROTECTION,
            Enchantments.BLAST_PROTECTION, Enchantments.PROJECTILE_PROTECTION,
            Enchantments.FEATHER_FALLING, Enchantments.DEPTH_STRIDER,
            Enchantments.FROST_WALKER, Enchantments.THORNS,
            Enchantments.SOUL_SPEED, Enchantments.UNBREAKING, Enchantments.MENDING,
            Enchantments.BINDING_CURSE, Enchantments.VANISHING_CURSE
        ));
    }

    public ToolConfigScreen() {
        super(Text.translatable("screen.bettertools.config"));
    }

    @Override
    protected void init() {
        if (working.isEmpty()) {
            for (String t : TOOL_TYPES) {
                working.put(t, BetterToolsConfig.getConfig(t).copy());
            }
        }

        int pw = Math.min(440, this.width  - 30);
        int ph = Math.min(280, this.height - 30);
        panelX = (this.width  - pw) / 2;
        panelY = (this.height - ph) / 2;
        panelW = pw;
        panelH = ph;

        buildWidgets();
    }

    // Number of tool buttons that fit in the left panel
    private int toolVisible() {
        int areaH = panelH - 24 - 4;
        boolean needsScroll = TOOL_TYPES.length * (TOOL_BTN_H + 2) > areaH;
        int usable = needsScroll ? areaH - 36 : areaH;
        return Math.max(1, usable / (TOOL_BTN_H + 2));
    }

    private void buildWidgets() {
        clearChildren();
        enchRows.clear();
        enchScroll = 0;

        String toolType = TOOL_TYPES[selectedTool];
        ToolConfig cfg  = working.get(toolType);
        boolean isMining = MINING_TOOLS.contains(toolType);

        // â”€â”€ Left panel â€“ scrollable tool list â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        int toolsAreaY = panelY + 24;
        int tv = toolVisible();
        boolean needsToolScroll = TOOL_TYPES.length > tv;
        int listStartY = needsToolScroll ? toolsAreaY + 18 : toolsAreaY;

        if (needsToolScroll) {
            addDrawableChild(ButtonWidget.builder(
                Text.literal("\u25b2"),
                b -> { if (toolScroll > 0) { toolScroll--; buildWidgets(); } }
            ).dimensions(panelX + 4, toolsAreaY, leftW - 8, 16).build());

            addDrawableChild(ButtonWidget.builder(
                Text.literal("\u25bc"),
                b -> { if (toolScroll < TOOL_TYPES.length - tv) { toolScroll++; buildWidgets(); } }
            ).dimensions(panelX + 4, listStartY + tv * (TOOL_BTN_H + 2) + 2, leftW - 8, 16).build());
        }

        for (int i = 0; i < tv; i++) {
            int idx = i + toolScroll;
            if (idx >= TOOL_TYPES.length) break;
            final int finalIdx = idx;
            int btnY = listStartY + i * (TOOL_BTN_H + 2);
            addDrawableChild(ButtonWidget.builder(
                Text.literal("  " + ToolHelper.getToolDisplayName(TOOL_TYPES[idx])),
                b -> { selectedTool = finalIdx; buildWidgets(); }
            ).dimensions(panelX + 4, btnY, leftW - 8, TOOL_BTN_H).build());
        }

        // â”€â”€ Right panel â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        int rx = panelX + leftW + 8;
        int ry = panelY + 24;
        int rw = panelW - leftW - 16;

        if (isMining) {
            addDrawableChild(ButtonWidget.builder(
                Text.literal("3x3 Mining: " + (cfg.threeByThree ? "\u00a7aON\u00a7r" : "\u00a7cOFF\u00a7r")),
                b -> { cfg.threeByThree = !cfg.threeByThree; buildWidgets(); }
            ).dimensions(rx, ry, rw, 20).build());

            addDrawableChild(ButtonWidget.builder(
                Text.literal("Auto-Smelt: " + (cfg.autoSmelt ? "\u00a7aON\u00a7r" : "\u00a7cOFF\u00a7r")),
                b -> { cfg.autoSmelt = !cfg.autoSmelt; buildWidgets(); }
            ).dimensions(rx, ry + 24, rw, 20).build());

            enchLabelY  = ry + 49;
            enchSectionY = ry + 57;
        } else {
            enchLabelY  = ry + 5;
            enchSectionY = ry + 14;
        }

        // Build enchantment rows for current tool type
        List<Enchantment> enchs = TOOL_ENCHANTS.getOrDefault(toolType, Collections.emptyList());
        for (Enchantment ench : enchs) {
            Identifier id = Registries.ENCHANTMENT.getId(ench);
            if (id == null) continue;
            String key = id.toString();
            enchRows.add(new EnchRow(ench, id, key, cfg.enchantments.getOrDefault(key, 0)));
        }

        int visible = Math.min(ENCH_VISIBLE, enchRows.size());
        for (int i = 0; i < visible; i++) {
            int rowIdx = i + enchScroll;
            if (rowIdx >= enchRows.size()) break;
            EnchRow row = enchRows.get(rowIdx);
            int rowY = enchSectionY + i * ENCH_ROW_H;
            final String rowKey = row.key;

            addDrawableChild(ButtonWidget.builder(
                Text.literal("-"),
                b -> {
                    ToolConfig c = working.get(TOOL_TYPES[selectedTool]);
                    int cur = c.enchantments.getOrDefault(rowKey, 0);
                    if (cur > 0) { c.enchantments.put(rowKey, cur - 1); buildWidgets(); }
                }
            ).dimensions(rx, rowY, 14, 14).build());

            addDrawableChild(ButtonWidget.builder(
                Text.literal("+"),
                b -> {
                    ToolConfig c = working.get(TOOL_TYPES[selectedTool]);
                    int cur = c.enchantments.getOrDefault(rowKey, 0);
                    if (cur < 100) { c.enchantments.put(rowKey, cur + 1); buildWidgets(); }
                }
            ).dimensions(rx + 14, rowY, 14, 14).build());
        }

        if (enchRows.size() > ENCH_VISIBLE) {
            addDrawableChild(ButtonWidget.builder(
                Text.literal("\u25b2"),
                b -> { if (enchScroll > 0) { enchScroll--; buildWidgets(); } }
            ).dimensions(rx + rw - 16, enchSectionY, 16, 16).build());

            addDrawableChild(ButtonWidget.builder(
                Text.literal("\u25bc"),
                b -> { if (enchScroll < enchRows.size() - ENCH_VISIBLE) { enchScroll++; buildWidgets(); } }
            ).dimensions(rx + rw - 16, enchSectionY + (ENCH_VISIBLE - 1) * ENCH_ROW_H, 16, 16).build());
        }

        addDrawableChild(ButtonWidget.builder(
            Text.literal("Apply to Held Item"),
            b -> applyEnchantmentsToHeldItem()
        ).dimensions(rx, panelY + panelH - 46, rw, 18).build());

        addDrawableChild(ButtonWidget.builder(
            Text.literal("Save & Close"),
            b -> saveAndClose()
        ).dimensions(panelX + panelW / 2 - 55, panelY + panelH - 22, 110, 18).build());
    }

    private void applyEnchantmentsToHeldItem() {
        if (client == null || client.player == null) return;
        ItemStack held = client.player.getMainHandStack();
        if (held.isEmpty()) return;

        Map<String, Integer> toApply = new HashMap<>();
        for (EnchRow row : enchRows) {
            if (row.level > 0) toApply.put(row.key, row.level);
        }

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(toApply.size());
        for (Map.Entry<String, Integer> entry : toApply.entrySet()) {
            buf.writeString(entry.getKey());
            buf.writeInt(entry.getValue());
        }
        ClientPlayNetworking.send(BetterToolsNetwork.APPLY_ENCHANTMENTS, buf);
    }

    private void saveAndClose() {
        for (String t : TOOL_TYPES) {
            BetterToolsConfig.toolConfigs.put(t, working.get(t));
        }
        BetterToolsConfig.save();
        this.close();
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        // Background
        ctx.fill(0, 0, this.width, this.height, COL_BG);
        ctx.fill(panelX, panelY, panelX + panelW, panelY + panelH, COL_PANEL);
        ctx.fill(panelX + 1, panelY + 1, panelX + panelW - 1, panelY + panelH - 1, COL_PANEL2);
        drawBorder(ctx, panelX, panelY, panelW, panelH, COL_BORDER);

        // Title bar
        ctx.fill(panelX + 1, panelY + 1, panelX + panelW - 1, panelY + 14, 0xCC0D0D25);
        ctx.drawCenteredTextWithShadow(textRenderer,
            Text.literal("\u00a7l\u2756 Better Tools Configuration \u2756").withColor(COL_TITLE),
            panelX + panelW / 2, panelY + 4, 0xFFFFFF);

        // Left/right divider
        ctx.fill(panelX + leftW, panelY + 14, panelX + leftW + 1, panelY + panelH - 1, COL_DIVIDER);

        // Selected tool highlight & icons
        int tv = toolVisible();
        boolean needsToolScroll = TOOL_TYPES.length > tv;
        int toolsAreaY = panelY + 24;
        int listStartY = needsToolScroll ? toolsAreaY + 18 : toolsAreaY;

        int selRelIdx = selectedTool - toolScroll;
        if (selRelIdx >= 0 && selRelIdx < tv) {
            int selY = listStartY + selRelIdx * (TOOL_BTN_H + 2);
            ctx.fill(panelX + 4, selY, panelX + leftW - 4, selY + TOOL_BTN_H, COL_SELECTED);
        }

        for (int i = 0; i < tv; i++) {
            int idx = i + toolScroll;
            if (idx >= TOOL_TYPES.length) break;
            ItemStack icon = new ItemStack(ToolHelper.getRepresentativeItem(TOOL_TYPES[idx]));
            ctx.drawItem(icon, panelX + 6, listStartY + i * (TOOL_BTN_H + 2) + 3);
        }

        // Right panel header
        int rx = panelX + leftW + 8;
        int rw = panelW - leftW - 16;
        ctx.drawTextWithShadow(textRenderer,
            Text.literal("\u00a7b" + ToolHelper.getToolDisplayName(TOOL_TYPES[selectedTool]) + " Options"),
            rx, panelY + 16, COL_TEXT);

        // Divider above enchantments (only for mining tools)
        if (MINING_TOOLS.contains(TOOL_TYPES[selectedTool])) {
            ctx.fill(rx, enchLabelY - 3, rx + rw, enchLabelY - 2, COL_DIVIDER);
        }

        // Enchantments label
        ctx.drawTextWithShadow(textRenderer,
            Text.literal("\u00a76\u26a1 Enchantments \u00a77(max 100)"),
            rx, enchLabelY, COL_TEXT);

        // Enchantment rows
        int visible = Math.min(ENCH_VISIBLE, enchRows.size());
        for (int i = 0; i < visible; i++) {
            int rowIdx = i + enchScroll;
            if (rowIdx >= enchRows.size()) break;
            EnchRow row = enchRows.get(rowIdx);
            int rowY = enchSectionY + i * ENCH_ROW_H;

            if (i % 2 == 0) ctx.fill(rx, rowY, rx + rw - 18, rowY + ENCH_ROW_H - 1, 0x22FFFFFF);

            String enchName = getEnchantmentShortName(row.id);
            ctx.drawTextWithShadow(textRenderer,
                Text.literal(row.level > 0 ? "\u00a7e" + enchName : "\u00a77" + enchName),
                rx + 34, rowY + 3, 0xFFFFFF);

            String levelStr = row.level == 0 ? "\u00a78Off" : "\u00a7a" + row.level;
            ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal(levelStr), rx + rw - 40, rowY + 3, 0xFFFFFF);
        }

        super.render(ctx, mx, my, delta);
    }

    private void drawBorder(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x,         y,         x + w,     y + 1,     color);
        ctx.fill(x,         y + h - 1, x + w,     y + h,     color);
        ctx.fill(x,         y,         x + 1,     y + h,     color);
        ctx.fill(x + w - 1, y,         x + w,     y + h,     color);
    }

    private String getEnchantmentShortName(Identifier id) {
        String[] words = id.getPath().split("_");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0)));
                if (w.length() > 1) sb.append(w.substring(1));
                sb.append(" ");
            }
        }
        return sb.toString().trim();
    }

    @Override public boolean shouldPause() { return false; }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { this.close(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private static class EnchRow {
        final Enchantment enchantment;
        final Identifier  id;
        final String      key;
        int level;

        EnchRow(Enchantment enchantment, Identifier id, String key, int level) {
            this.enchantment = enchantment;
            this.id    = id;
            this.key   = key;
            this.level = level;
        }
    }
}
