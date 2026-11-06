package com.ayaan.replicatormod.client.gui;

import com.ayaan.replicatormod.network.PacketActivateClipboard;
import com.ayaan.replicatormod.network.PacketHandler;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.List;

/** Simple scrollable list of the player's own saved clipboards. */
public class GuiClipboardPicker extends GuiScreen {

    private static final int ROW_HEIGHT = 20;
    private static final int LIST_TOP = 36;

    private final List<String> names;
    private int scrollOffset = 0;

    public GuiClipboardPicker(List<String> names) {
        this.names = names;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
    }

    private int visibleRows() {
        return (this.height - 16 - LIST_TOP) / ROW_HEIGHT;
    }

    private int rowLeft() {
        int rowWidth = Math.min(300, this.width - 40);
        return this.width / 2 - rowWidth / 2;
    }

    private int rowWidth() {
        return Math.min(300, this.width - 40);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, "Select a clipboard", this.width / 2, 16, 0xFFFFFF);

        if (names.isEmpty()) {
            this.drawCenteredString(this.fontRendererObj, "No saved clipboards yet", this.width / 2, this.height / 2, 0xAAAAAA);
            super.drawScreen(mouseX, mouseY, partialTicks);
            return;
        }

        int visibleRows = visibleRows();
        int maxOffset = Math.max(0, names.size() - visibleRows);
        if (scrollOffset > maxOffset) {
            scrollOffset = maxOffset;
        }

        int rowLeft = rowLeft();
        int rowWidth = rowWidth();

        for (int i = 0; i < visibleRows && (i + scrollOffset) < names.size(); i++) {
            int index = i + scrollOffset;
            int rowTop = LIST_TOP + i * ROW_HEIGHT;
            boolean hovered = mouseX >= rowLeft && mouseX <= rowLeft + rowWidth && mouseY >= rowTop && mouseY <= rowTop + ROW_HEIGHT;
            if (hovered) {
                drawRect(rowLeft, rowTop, rowLeft + rowWidth, rowTop + ROW_HEIGHT, 0x55FFFFFF);
            }
            this.drawString(this.fontRendererObj, names.get(index), rowLeft + 4, rowTop + 6, hovered ? 0xFFFFFF : 0xCCCCCC);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton != 0 || names.isEmpty()) {
            return;
        }

        int visibleRows = visibleRows();
        int rowLeft = rowLeft();
        int rowWidth = rowWidth();

        for (int i = 0; i < visibleRows && (i + scrollOffset) < names.size(); i++) {
            int rowTop = LIST_TOP + i * ROW_HEIGHT;
            if (mouseX >= rowLeft && mouseX <= rowLeft + rowWidth && mouseY >= rowTop && mouseY <= rowTop + ROW_HEIGHT) {
                select(names.get(i + scrollOffset));
                return;
            }
        }
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            scrollOffset -= Integer.signum(wheel);
            if (scrollOffset < 0) {
                scrollOffset = 0;
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.mc.displayGuiScreen(null);
        }
    }

    private void select(String name) {
        PacketHandler.INSTANCE.sendToServer(new PacketActivateClipboard(name));
        this.mc.displayGuiScreen(null);
    }
}
