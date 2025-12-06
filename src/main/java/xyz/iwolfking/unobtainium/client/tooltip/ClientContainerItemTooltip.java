package xyz.iwolfking.unobtainium.client.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import xyz.iwolfking.unobtainium.api.lib.tooltip.ContainerItemTooltip;

import javax.annotation.Nullable;

public class ClientContainerItemTooltip implements ClientTooltipComponent {
    public static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/gui/container/bundle.png");
    private static final Component HOLD_SHIFT_COMPONENT = new TranslatableComponent("item.container.tooltip.info", new TranslatableComponent("item.container.tooltip.shift").withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.GRAY);

    private final NonNullList<ItemStack> items;
    private final int containerRows;
    @Nullable
    private final DyeColor backgroundColor;

    public ClientContainerItemTooltip(ContainerItemTooltip tooltip) {
        this.items = tooltip.items();
        this.containerRows = tooltip.containerRows();
        this.backgroundColor = tooltip.backgroundColor();
    }

    @Override
    public void renderText(Font pFont, int pX, int pY, Matrix4f pMatrix4f, MultiBufferSource.BufferSource pBufferSource) {
        if (this.hideInventoryContents()) {
            pFont.drawInBatch(HOLD_SHIFT_COMPONENT, pX, pY, -1, true, pMatrix4f, pBufferSource, false, 0, 15728880);
        }
    }

    @Override
    public int getHeight() {
        if (this.hideInventoryContents()) {
            return 10;
        }
        return this.gridSizeY() * 20 + 2 + 4;
    }

    @Override
    public int getWidth(Font font) {
        if (this.hideInventoryContents()) {
            return font.width(HOLD_SHIFT_COMPONENT);
        }
        return this.gridSizeX() * 18 + 2;
    }

    @Override
    public void renderImage(Font pFont, int pMouseX, int pMouseY, PoseStack pPoseStack, ItemRenderer pItemRenderer, int pBlitOffset) {
        if (this.hideInventoryContents()) return;
        int i = this.gridSizeX();
        int j = this.gridSizeY();
        int k = 0;
        int lastFilledSlot = this.getLastFilledSlot();
        for(int l = 0; l < j; ++l) {
            for(int i1 = 0; i1 < i; ++i1) {
                int j1 = pMouseX + i1 * 18 + 1;
                int k1 = pMouseY + l * 20 + 1;
                this.renderSlot(j1, k1, k, pFont, pPoseStack, pItemRenderer, pBlitOffset, k == lastFilledSlot);
                k++;
            }
        }

        this.drawBorder(pMouseX, pMouseY, i, j, pPoseStack, pBlitOffset);
    }

    private boolean hideInventoryContents() {
      return !Screen.hasShiftDown();
    }

    private int getLastFilledSlot() {
        for (int i = this.items.size() - 1; i >= 0; i--) {
            if (!this.items.get(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    private void renderSlot(int x, int y, int pIndex, Font pFont, PoseStack pPoseStack, ItemRenderer pItemRenderer, int pBlitOffset, boolean lastFilledSlot) {
        if (pIndex >= this.items.size()) {
            this.blit(pPoseStack, x, y, pBlitOffset, ClientContainerItemTooltip.Texture.SLOT);
        } else {
            ItemStack itemstack = this.items.get(pIndex);
            this.blit(pPoseStack, x, y, pBlitOffset, ClientContainerItemTooltip.Texture.SLOT);
            pItemRenderer.renderAndDecorateItem(itemstack, x + 1, y + 1, pIndex);
            pItemRenderer.renderGuiItemDecorations(pFont, itemstack, x + 1, y + 1);
            if (lastFilledSlot) {
                AbstractContainerScreen.renderSlotHighlight(pPoseStack, x + 1, y + 1, pBlitOffset);
            }
        }
    }

    private void drawBorder(int x, int y, int col, int row, PoseStack pPoseStack, int pBlitOffset) {
        this.blit(pPoseStack, x, y, pBlitOffset, ClientContainerItemTooltip.Texture.BORDER_CORNER_TOP);
        this.blit(pPoseStack, x + col * 18 + 1, y, pBlitOffset, ClientContainerItemTooltip.Texture.BORDER_CORNER_TOP);

        for(int i = 0; i < col; ++i) {
            this.blit(pPoseStack, x + 1 + i * 18, y, pBlitOffset, ClientContainerItemTooltip.Texture.BORDER_HORIZONTAL_TOP);
            this.blit(pPoseStack, x + 1 + i * 18, y + row * 20, pBlitOffset, ClientContainerItemTooltip.Texture.BORDER_HORIZONTAL_BOTTOM);
        }

        for(int j = 0; j < row; ++j) {
            this.blit(pPoseStack, x, y + j * 20 + 1, pBlitOffset, ClientContainerItemTooltip.Texture.BORDER_VERTICAL);
            this.blit(pPoseStack, x + col * 18 + 1, y + j * 20 + 1, pBlitOffset, ClientContainerItemTooltip.Texture.BORDER_VERTICAL);
        }

        this.blit(pPoseStack, x, y + row * 20, pBlitOffset, ClientContainerItemTooltip.Texture.BORDER_CORNER_BOTTOM);
        this.blit(pPoseStack, x + col * 18 + 1, y + row * 20, pBlitOffset, ClientContainerItemTooltip.Texture.BORDER_CORNER_BOTTOM);
    }

    private void blit(PoseStack pPoseStack, int pX, int pY, int pBlitOffset, ClientContainerItemTooltip.Texture texture) {
        float[] color = this.getBackgroundColor();
        RenderSystem.setShaderColor(color[0], color[1], color[2], 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE_LOCATION);
        GuiComponent.blit(pPoseStack, pX, pY, pBlitOffset, texture.x, texture.y, texture.w, texture.h, 128, 128);
    }

    private float[] getBackgroundColor() {
        if (this.backgroundColor == null) {
            return new float[]{1.0F, 1.0F, 1.0F};
        } else if (this.backgroundColor == DyeColor.WHITE) {
            return new float[]{0.9019608F, 0.9019608F, 0.9019608F};
        } else {
            return this.backgroundColor.getTextureDiffuseColors();
        }
    }

    private int gridSizeX() {
        return 9;
    }

    private int gridSizeY() {
        return this.containerRows;
    }

    enum Texture {
        SLOT(0, 0, 18, 20),
        BLOCKED_SLOT(0, 40, 18, 20),
        BORDER_VERTICAL(0, 18, 1, 20),
        BORDER_HORIZONTAL_TOP(0, 20, 18, 1),
        BORDER_HORIZONTAL_BOTTOM(0, 60, 18, 1),
        BORDER_CORNER_TOP(0, 20, 1, 1),
        BORDER_CORNER_BOTTOM(0, 60, 1, 1);

        public final int x;
        public final int y;
        public final int w;
        public final int h;

        Texture(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }
}