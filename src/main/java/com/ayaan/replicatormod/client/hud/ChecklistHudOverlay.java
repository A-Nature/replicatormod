package com.ayaan.replicatormod.client.hud;

import com.ayaan.replicatormod.client.ClientState;
import com.ayaan.replicatormod.clipboard.MaterialRequirement;
import com.ayaan.replicatormod.init.ModItems;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.lwjgl.opengl.GL11;

import java.util.List;

/**
 * Always-visible top-left HUD listing the active clipboard's required
 * materials, green/red against the player's own inventory. Shown only while
 * holding the Infinity Replicator with a clipboard active. Inventory counts
 * are recomputed periodically rather than every frame.
 */
public class ChecklistHudOverlay {

    private static final int REFRESH_INTERVAL_TICKS = 10;

    private int tickCounter = 0;
    private int[] cachedHave = new int[0];

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.gameSettings.showDebugInfo) {
            return;
        }

        String activeName = ClientState.getActiveClipboardName();
        if (activeName == null) {
            return;
        }

        ItemStack held = mc.thePlayer.getCurrentEquippedItem();
        if (held == null || held.getItem() != ModItems.infinityReplicator) {
            return;
        }

        List<MaterialRequirement> requirements = ClientState.getActiveRequirements();

        if (cachedHave.length != requirements.size()) {
            cachedHave = new int[requirements.size()];
            tickCounter = REFRESH_INTERVAL_TICKS;
        }
        tickCounter++;
        if (tickCounter >= REFRESH_INTERVAL_TICKS) {
            for (int i = 0; i < requirements.size(); i++) {
                cachedHave[i] = requirements.get(i).countInInventory(mc.thePlayer);
            }
            tickCounter = 0;
        }

        FontRenderer font = mc.fontRenderer;
        RenderItem renderItem = RenderItem.getInstance();
        int x = 6;
        int y = 6;

        // Item icon rendering leaves GL lighting/blend/color state dirty if not
        // carefully bracketed — save and restore everything so it can't bleed
        // into the next frame's chat, inventory, or world rendering.
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_CURRENT_BIT | GL11.GL_TEXTURE_BIT);
        try {
            font.drawStringWithShadow("Clipboard: " + activeName, x, y, 0xFFFFFF);
            y += 10;
            font.drawStringWithShadow("X: Undo   R: Rotate (" + ClientState.getRotation() * 90 + " deg)", x, y, 0xAAAAAA);
            y += 12;

            for (int i = 0; i < requirements.size(); i++) {
                MaterialRequirement req = requirements.get(i);
                int have = cachedHave[i];
                boolean satisfied = have >= req.required;

                Item item = req.resolveItem();
                if (item != null) {
                    ItemStack iconStack = new ItemStack(item, 1, req.meta);
                    RenderHelper.enableGUIStandardItemLighting();
                    renderItem.renderItemAndEffectIntoGUI(font, mc.renderEngine, iconStack, x, y);
                    RenderHelper.disableStandardItemLighting();
                    GL11.glColor4f(1F, 1F, 1F, 1F);
                }

                String line = have + "/" + req.required;
                if (!satisfied) {
                    line += " (short " + (req.required - have) + ")";
                }
                font.drawStringWithShadow(line, x + 18, y + 4, satisfied ? 0x55FF55 : 0xFF5555);
                y += 18;
            }
        } finally {
            GL11.glColor4f(1F, 1F, 1F, 1F);
            GL11.glPopAttrib();
        }
    }
}
