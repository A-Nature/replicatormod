package com.ayaan.replicatormod.client.hud;

import com.ayaan.replicatormod.client.ClientState;
import com.ayaan.replicatormod.init.ModItems;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;

/**
 * Draws a wireframe box, sized to the active clipboard's dimensions, at
 * whatever block the player is currently looking at — a preview of where a
 * paste would land, using the same origin-corner convention as the actual
 * paste. Shown only while holding the Infinity Replicator with a clipboard
 * active and looking at a block in range.
 */
public class PastePreviewRenderer {

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || ClientState.getActiveClipboardName() == null) {
            return;
        }

        ItemStack held = mc.thePlayer.getCurrentEquippedItem();
        if (held == null || held.getItem() != ModItems.infinityReplicator) {
            return;
        }

        if (mc.objectMouseOver == null || mc.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
            return;
        }

        boolean swapped = (ClientState.getRotation() & 1) != 0;
        int sizeX = swapped ? ClientState.getActiveSizeZ() : ClientState.getActiveSizeX();
        int sizeY = ClientState.getActiveSizeY();
        int sizeZ = swapped ? ClientState.getActiveSizeX() : ClientState.getActiveSizeZ();
        if (sizeX <= 0 || sizeY <= 0 || sizeZ <= 0) {
            return;
        }

        ForgeDirection dir = ForgeDirection.getOrientation(mc.objectMouseOver.sideHit);
        int originX = mc.objectMouseOver.blockX + dir.offsetX;
        int originY = mc.objectMouseOver.blockY + dir.offsetY;
        int originZ = mc.objectMouseOver.blockZ + dir.offsetZ;

        Entity view = mc.renderViewEntity != null ? mc.renderViewEntity : mc.thePlayer;
        double partialTicks = event.partialTicks;
        double camX = view.lastTickPosX + (view.posX - view.lastTickPosX) * partialTicks;
        double camY = view.lastTickPosY + (view.posY - view.lastTickPosY) * partialTicks;
        double camZ = view.lastTickPosZ + (view.posZ - view.lastTickPosZ) * partialTicks;

        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_LINE_BIT | GL11.GL_CURRENT_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        try {
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glLineWidth(2.0F);
            GL11.glColor4f(0.2F, 1.0F, 1.0F, 0.85F);

            GL11.glTranslated(originX - camX, originY - camY, originZ - camZ);
            drawBoxOutline(sizeX, sizeY, sizeZ);
        } finally {
            GL11.glPopAttrib();
            GL11.glPopMatrix();
        }
    }

    private void drawBoxOutline(double sx, double sy, double sz) {
        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glVertex3d(0, 0, 0);
        GL11.glVertex3d(sx, 0, 0);
        GL11.glVertex3d(sx, 0, sz);
        GL11.glVertex3d(0, 0, sz);
        GL11.glVertex3d(0, 0, 0);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glVertex3d(0, sy, 0);
        GL11.glVertex3d(sx, sy, 0);
        GL11.glVertex3d(sx, sy, sz);
        GL11.glVertex3d(0, sy, sz);
        GL11.glVertex3d(0, sy, 0);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3d(0, 0, 0);
        GL11.glVertex3d(0, sy, 0);
        GL11.glVertex3d(sx, 0, 0);
        GL11.glVertex3d(sx, sy, 0);
        GL11.glVertex3d(sx, 0, sz);
        GL11.glVertex3d(sx, sy, sz);
        GL11.glVertex3d(0, 0, sz);
        GL11.glVertex3d(0, sy, sz);
        GL11.glEnd();
    }
}
