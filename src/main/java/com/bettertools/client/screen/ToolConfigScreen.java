package com.bettertools.client.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * The tool-configuration screen shown to the OP-level-4 player who ran /bettertools.
 *
 * <p>This screen is opened exclusively on the client of the player whose command
 * execution was validated server-side.  It is never opened for other players.</p>
 */
public class ToolConfigScreen extends Screen {

    private static final int SCREEN_WIDTH = 200;
    private static final int SCREEN_HEIGHT = 150;
    private static final int BACKGROUND_COLOR = 0xCC000000;

    public ToolConfigScreen() {
        super(Text.translatable("screen.bettertools.tool_config.title"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int panelLeft = centerX - SCREEN_WIDTH / 2;
        int panelTop = centerY - SCREEN_HEIGHT / 2;

        // Close button
        this.addDrawableChild(
                ButtonWidget.builder(Text.translatable("gui.bettertools.close"), button -> this.close())
                        .dimensions(panelLeft + SCREEN_WIDTH / 2 - 40, panelTop + SCREEN_HEIGHT - 30, 80, 20)
                        .build()
        );
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int panelLeft = centerX - SCREEN_WIDTH / 2;
        int panelTop = centerY - SCREEN_HEIGHT / 2;

        // Draw panel background
        context.fill(panelLeft, panelTop, panelLeft + SCREEN_WIDTH, panelTop + SCREEN_HEIGHT, BACKGROUND_COLOR);

        // Draw title
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                this.title,
                centerX,
                panelTop + 10,
                0xFFFFFF
        );

        // Draw descriptive label
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.translatable("screen.bettertools.tool_config.description"),
                centerX,
                panelTop + 40,
                0xAAAAAA
        );

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
