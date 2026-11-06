package com.ayaan.replicatormod.client.gui;

import com.ayaan.replicatormod.network.PacketHandler;
import com.ayaan.replicatormod.network.PacketSaveClipboard;
import com.ayaan.replicatormod.util.BlockCoord;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

public class GuiCopierName extends GuiScreen {

    private final BlockCoord pointA;
    private final BlockCoord pointB;
    private GuiTextField nameField;

    public GuiCopierName(BlockCoord pointA, BlockCoord pointB) {
        this.pointA = pointA;
        this.pointB = pointB;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();

        int fieldWidth = 160;
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        nameField = new GuiTextField(this.fontRendererObj, centerX - fieldWidth / 2, centerY - 10, fieldWidth, 20);
        nameField.setFocused(true);
        nameField.setMaxStringLength(64);

        this.buttonList.add(new GuiButton(0, centerX - 50, centerY + 20, 100, 20, "Save"));
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void updateScreen() {
        nameField.updateCursorCounter();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, "Name this clipboard", this.width / 2, this.height / 2 - 40, 0xFFFFFF);
        this.drawCenteredString(this.fontRendererObj,
                "Point A: " + pointA + "   Point B: " + pointB, this.width / 2, this.height / 2 - 26, 0xAAAAAA);
        nameField.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
            submit();
            return;
        }
        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.mc.displayGuiScreen(null);
            return;
        }
        nameField.textboxKeyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        nameField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            submit();
        }
    }

    private void submit() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            return;
        }
        PacketHandler.INSTANCE.sendToServer(new PacketSaveClipboard(pointA, pointB, name));
        this.mc.displayGuiScreen(null);
    }
}
