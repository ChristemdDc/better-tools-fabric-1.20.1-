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

    private static final String[] TOOL_TYPES = {"pickaxe", "axe", "shovel", "sword", "hoe"};

    private int panelX, panelY, panelW, panelH;
    private final int leftW = 110;
    private int selectedTool = 0;

    private final Map<String, ToolConfig> working = new HashMap<>();
    private final List<EnchRow> enchRows = new ArrayList<>();

    private int enchScroll = 0;
    private static final int ENCH_ROW_H = 18;
    private static final int ENCH_VISIBLE = 5;

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

        int pw = Math.min(420, this.width - 30);
        int ph = Math.min(260, this.height - 30);
        panelX = (this.width - pw) / 2;
        panelY = (this.height - ph) / 2;
        panelW = pw;
        panelH = ph;

        buildWidgets();
    }

    private void buildWidgets() {
        clearChildren();
        enchRows.clear();
        enchScroll = 0;

        String toolType = TOOL_TYPES[selectedTool];
        ToolConfig cfg = working.get(toolType);

        int toolBtnH = 22;
        int startY = panelY + 24;
        for (int i = 0; i < TOOL_TYPES.length; i++) {
            final int idx = i;
            int btnY = startY + i * (toolBtnH + 2);
            String label = "  " + ToolHelper.getToolDisplayName(TOOL_TYPES[i]);
            addDrawableChild(ButtonWidget.builder(
                Text.literal(label),
                b -> { selectedTool = idx; buildWidgets(); }
            ).dimensions(panelX + 4, btnY, leftW - 8, toolBtnH).build());
        }

        int rx = panelX + leftW + 8;
        int ry = panelY + 24;
        int rw = panelW - leftW - 16;

        addDrawableChild(ButtonWidget.builder(
            Text.literal("3x3 Mining: " + (cfg.threeByThree ? "\u00a7aON\u00a7r" : "\u00a7cOFF\u00a7r")),
            b -> { cfg.threeByThree = !cfg.threeByThree; buildWidgets(); }
        ).dimensions(rx, ry, rw, 20).build());

        addDrawableChild(ButtonWidget.builder(
            Text.literal("Auto-Smelt: " + (cfg.autoSmelt ? "\u00a7aON\u00a7r" : "\u00a7cOFF\u00a7r")),
            b -> { cfg.autoSmelt = !cfg.autoSmelt; buildWidgets(); }
        ).dimensions(rx, ry + 24, rw, 20).build());

        List<Enchantment> enchs = TOOL_ENCHANTS.getOrDefault(toolType, Collections.emptyList());
        for (Enchantment ench : enchs) {
            Identifier id = Registries.ENCHANTMENT.getId(ench);
            if (id == null) continue;
            String key = id.toString();
            int currentLevel = cfg.enchantments.getOrDefault(key, 0);
            enchRows.add(new EnchRow(ench, id, key, currentLevel));
        }

        int enchStartY = ry + 56;

        int visible = Math.min(ENCH_VISIBLE, enchRows.size());
        for (int i = 0; i < visible; i++) {
            int rowIdx = i + enchScroll;
            if (rowIdx >= enchRows.size()) break;
            EnchRow row = enchRows.get(rowIdx);
            int rowY = enchStartY + i * ENCH_ROW_H;

            final String rowKey = row.key;

            addDrawableChild(ButtonWidget.builder(
                Text.literal("-"),
                b -> {
                    ToolConfig c = working.get(toolType);
                    int cur = c.enchantments.getOrDefault(rowKey, 0);
                    if (cur > 0) {
                        c.enchantments.put(rowKey, cur - 1);
                        buildWidgets();
                    }
                }
            ).dimensions(rx, rowY, 14, 14).build());

            addDrawableChild(ButtonWidget.builder(
                Text.literal("+"),
                b -> {
                    ToolConfig c = working.get(toolType);
                    int cur = c.enchantments.getOrDefault(rowKey, 0);
                    if (cur < 100) {
                        c.enchantments.put(rowKey, cur + 1);
                        buildWidgets();
                    }
                }
            ).dimensions(rx + 14, rowY, 14, 14).build());
        }

        if (enchRows.size() > ENCH_VISIBLE) {
            addDrawableChild(ButtonWidget.builder(
                Text.literal("\u25b2"),
                b -> { if (enchScroll > 0) { enchScroll--; buildWidgets(); } }
            ).dimensions(rx + rw - 16, enchStartY, 16, 16).build());
            addDrawableChild(ButtonWidget.builder(
                Text.literal("\u25bc"),
                b -> { if (enchScroll < enchRows.size() - ENCH_VISIBLE) { enchScroll++; buildWidgets(); } }
            ).dimensions(rx + rw - 16, enchStartY + (ENCH_VISIBLE - 1) * ENCH_ROW_H, 16, 16).build());
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

        String toolType = ToolHelper.getToolType(held);
        if (toolType == null) return;

        ToolConfig cfg = working.get(toolType);
        if (cfg == null) return;

        PacketByteBuf buf = PacketByteBufs.create();
        Map<String, Integer> toApply = new HashMap<>();
        for (EnchRow row : enchRows) {
            if (row.level > 0) toApply.put(row.key, row.level);
        }
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
        ctx.fill(0, 0, this.width, this.height, COL_BG);
        ctx.fill(panelX, panelY, panelX + panelW, panelY + panelH, COL_PANEL);
        ctx.fill(panelX + 1, panelY + 1, panelX + panelW - 1, panelY + panelH - 1, COL_PANEL2);
        drawBorder(ctx, panelX, panelY, panelW, panelH, COL_BORDER);
        ctx.fill(panelX + 1, panelY + 1, panelX + panelW - 1, panelY + 14, 0xCC0D0D25);
        ctx.drawCenteredTextWithShadow(textRenderer,
            Text.literal("\u00a7l\u2756 Better Tools Configuration \u2756").withColor(COL_TITLE),
            panelX + panelW / 2, panelY + 4, 0xFFFFFF);
        ctx.fill(panelX + leftW, panelY + 14, panelX + leftW + 1, panelY + panelH - 1, COL_DIVIDER);

        int toolBtnH = 22;
        int selY = panelY + 24 + selectedTool * (toolBtnH + 2);
        ctx.fill(panelX + 4, selY, panelX + leftW - 4, selY + toolBtnH, COL_SELECTED);

        for (int i = 0; i < TOOL_TYPES.length; i++) {
            ItemStack icon = new ItemStack(ToolHelper.getRepresentativeItem(TOOL_TYPES[i]));
            int iconY = panelY + 24 + i * (toolBtnH + 2) + 3;
            ctx.drawItem(icon, panelX + 6, iconY);
        }

        int rx = panelX + leftW + 8;
        int ry = panelY + 24;
        int rw = panelW - leftW - 16;
        String toolName = ToolHelper.getToolDisplayName(TOOL_TYPES[selectedTool]);
        ctx.drawTextWithShadow(textRenderer,
            Text.literal("\u00a7b" + toolName + " Options"),
            rx, panelY + 16, COL_TEXT);

        ctx.fill(rx, ry + 46, rx + rw, ry + 47, COL_DIVIDER);
        ctx.drawTextWithShadow(textRenderer,
            Text.literal("\u00a76\u26a1 Enchantments \u00a77(max 100)"),
            rx, ry + 49, COL_TEXT);

        int enchStartY = ry + 56;
        int visible = Math.min(ENCH_VISIBLE, enchRows.size());
        for (int i = 0; i < visible; i++) {
            int rowIdx = i + enchScroll;
            if (rowIdx >= enchRows.size()) break;
            EnchRow row = enchRows.get(rowIdx);
            int rowY = enchStartY + i * ENCH_ROW_H;

            if (i % 2 == 0) {
                ctx.fill(rx, rowY, rx + rw - 18, rowY + ENCH_ROW_H - 1, 0x22FFFFFF);
            }

            String enchName = getEnchantmentShortName(row.id);
            ctx.drawTextWithShadow(textRenderer,
                Text.literal(row.level > 0 ? "\u00a7e" + enchName : "\u00a77" + enchName),
                rx + 34, rowY + 3, 0xFFFFFF);

            String levelStr = row.level == 0 ? "\u00a78Off" : "\u00a7a" + row.level;
            int levelX = rx + rw - 40;
            ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal(levelStr), levelX, rowY + 3, 0xFFFFFF);
        }

        super.render(ctx, mx, my, delta);
    }

    private void drawBorder(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x, y, x + w, y + 1, color);
        ctx.fill(x, y + h - 1, x + w, y + h, color);
        ctx.fill(x, y, x + 1, y + h, color);
        ctx.fill(x + w - 1, y, x + w, y + h, color);
    }

    private String getEnchantmentShortName(Identifier id) {
        String path = id.getPath();
        String[] words = path.split("_");
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

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private static class EnchRow {
        final Enchantment enchantment;
        final Identifier id;
        final String key;
        int level;

        EnchRow(Enchantment enchantment, Identifier id, String key, int level) {
            this.enchantment = enchantment;
            this.id = id;
            this.key = key;
            this.level = level;
        }
    }
}
