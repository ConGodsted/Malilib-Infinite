package fi.dy.masa.malilib.render;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import org.lwjgl.opengl.GL11;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.storage.MapData;
import fi.dy.masa.malilib.config.values.HudAlignment;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.interfaces.IBackgroundRenderer;
import fi.dy.masa.malilib.gui.util.GuiUtils;
import fi.dy.masa.malilib.util.Color4f;
import fi.dy.masa.malilib.util.IntBoundingBox;
import fi.dy.masa.malilib.util.InventoryUtils;
import fi.dy.masa.malilib.util.PositionUtils;
import fi.dy.masa.malilib.util.PositionUtils.HitPart;

public class RenderUtils
{
    public static final ResourceLocation TEXTURE_MAP_BACKGROUND = new ResourceLocation("textures/map/map_background.png");
    //private static final Vec3d LIGHT0_POS = (new Vec3d( 0.2D, 1.0D, -0.7D)).normalize();
    //private static final Vec3d LIGHT1_POS = (new Vec3d(-0.2D, 1.0D,  0.7D)).normalize();

    public static void setupBlend()
    {
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
    }

    public static void setupBlendSimple()
    {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }

    public static void bindTexture(ResourceLocation texture)
    {
        mc().getTextureManager().bindTexture(texture);
    }

    public static void color(float r, float g, float b, float a)
    {
        GlStateManager.color(r, g, b, a);
    }

    public static void disableItemLighting()
    {
        RenderHelper.disableStandardItemLighting();
    }

    public static void enableItemLighting()
    {
        RenderHelper.enableStandardItemLighting();
    }

    public static void enableGuiItemLighting()
    {
        RenderHelper.enableGUIStandardItemLighting();
    }

    public static void drawOutlinedBox(int x, int y, int width, int height, int colorBg, int colorBorder)
    {
        drawOutlinedBox(x, y, width, height, colorBg, colorBorder, 0f);
    }

    public static void drawOutlinedBox(int x, int y, int width, int height, int colorBg, int colorBorder, float zLevel)
    {
        // Draw the background
        drawRect(x, y, width, height, colorBg, zLevel);

        // Draw the border
        drawOutline(x - 1, y - 1, width + 2, height + 2, colorBorder, zLevel);
    }

    public static void drawOutline(int x, int y, int width, int height, int colorBorder)
    {
        drawOutline(x, y, width, height, 1, colorBorder, 0);
    }

    public static void drawOutline(int x, int y, int width, int height, int colorBorder, float zLevel)
    {
        drawOutline(x, y, width, height, 1, colorBorder, zLevel);
    }

    public static void drawOutline(int x, int y, int width, int height, int borderWidth, int colorBorder)
    {
        drawOutline(x, y, width, height, borderWidth, colorBorder, 0f);
    }

    public static void drawOutline(int x, int y, int width, int height, int borderWidth, int colorBorder, float zLevel)
    {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        GlStateManager.disableTexture2D();
        setupBlend();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        drawRectBatched(x                      ,                        y, borderWidth            , height     , colorBorder, zLevel, buffer); // left edge
        drawRectBatched(x + width - borderWidth,                        y, borderWidth            , height     , colorBorder, zLevel, buffer); // right edge
        drawRectBatched(x + borderWidth        ,                        y, width - 2 * borderWidth, borderWidth, colorBorder, zLevel, buffer); // top edge
        drawRectBatched(x + borderWidth        , y + height - borderWidth, width - 2 * borderWidth, borderWidth, colorBorder, zLevel, buffer); // bottom edge

        tessellator.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();

        color(1f, 1f, 1f, 1f);
    }

    public static void drawTexturedRect(int x, int y, int u, int v, int width, int height)
    {
        drawTexturedRect(x, y, u, v, width, height, 0);
    }

    public static void drawRect(int x, int y, int width, int height, int color)
    {
        drawRect(x, y, width, height, color, 0f);
    }

    public static void drawRect(int x, int y, int width, int height, int color, float zLevel)
    {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        GlStateManager.disableTexture2D();
        setupBlend();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        drawRectBatched(x, y, width, height, color, zLevel, buffer);

        tessellator.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();

        color(1f, 1f, 1f, 1f);
    }

    public static void drawRectBatched(int x, int y, int width, int height, int color, BufferBuilder buffer)
    {
        drawRectBatched(x, y, width, height, color, 0f, buffer);
    }

    public static void drawRectBatched(int x, int y, int width, int height, int color, float zLevel, BufferBuilder buffer)
    {
        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >>  8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;

        buffer.pos(x        , y         , zLevel).color(r, g, b, a).endVertex();
        buffer.pos(x        , y + height, zLevel).color(r, g, b, a).endVertex();
        buffer.pos(x + width, y + height, zLevel).color(r, g, b, a).endVertex();
        buffer.pos(x + width, y         , zLevel).color(r, g, b, a).endVertex();
    }

    public static void drawTexturedRect(int x, int y, int u, int v, int width, int height, float zLevel)
    {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        drawTexturedRectBatched(x, y, u, v, width, height, zLevel, buffer);

        tessellator.draw();
    }

    public static void drawTexturedRectBatched(int x, int y, int u, int v, int width, int height, BufferBuilder buffer)
    {
        drawTexturedRectBatched(x, y, u, v, width, height, 0, buffer);
    }

    public static void drawTexturedRectBatched(int x, int y, int u, int v, int width, int height, float zLevel, BufferBuilder buffer)
    {
        float pixelWidth = 0.00390625F;

        buffer.pos(x        , y + height, zLevel).tex( u          * pixelWidth, (v + height) * pixelWidth).endVertex();
        buffer.pos(x + width, y + height, zLevel).tex((u + width) * pixelWidth, (v + height) * pixelWidth).endVertex();
        buffer.pos(x + width, y         , zLevel).tex((u + width) * pixelWidth,  v           * pixelWidth).endVertex();
        buffer.pos(x        , y         , zLevel).tex( u          * pixelWidth,  v           * pixelWidth).endVertex();
    }

    public static void drawGradientRect(int left, int top, int right, int bottom, double zLevel, int startColor, int endColor)
    {
        float sa = (float)(startColor >> 24 & 0xFF) / 255.0F;
        float sr = (float)(startColor >> 16 & 0xFF) / 255.0F;
        float sg = (float)(startColor >>  8 & 0xFF) / 255.0F;
        float sb = (float)(startColor & 0xFF) / 255.0F;

        float ea = (float)(endColor >> 24 & 0xFF) / 255.0F;
        float er = (float)(endColor >> 16 & 0xFF) / 255.0F;
        float eg = (float)(endColor >>  8 & 0xFF) / 255.0F;
        float eb = (float)(endColor & 0xFF) / 255.0F;

        GlStateManager.disableTexture2D();
        GlStateManager.disableAlpha();
        setupBlend();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        buffer.pos(right, top,    zLevel).color(sr, sg, sb, sa).endVertex();
        buffer.pos(left,  top,    zLevel).color(sr, sg, sb, sa).endVertex();
        buffer.pos(left,  bottom, zLevel).color(er, eg, eb, ea).endVertex();
        buffer.pos(right, bottom, zLevel).color(er, eg, eb, ea).endVertex();

        tessellator.draw();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void drawHorizontalLine(int x, int y, int width, int color)
    {
        drawRect(x, y, width, 1, color);
    }

    public static void drawVerticalLine(int x, int y, int height, int color)
    {
        drawRect(x, y, 1, height, color);
    }

    public static void renderSprite(int x, int y, int width, int height, String texture)
    {
        if (texture != null)
        {
            GlStateManager.disableLighting();
            TextureAtlasSprite sprite = mc().getTextureMapBlocks().getAtlasSprite(texture);
            mc().ingameGUI.drawTexturedModalRect(x, y, sprite, width, height);
        }
    }

    public static void draw9SplicedTexture(int x, int y, int u, int v, int width, int height, int texWidth, int texHeight, int edgeThickness, float zLevel)
    {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        int e = edgeThickness;
        
        RenderUtils.drawTexturedRectBatched(x, y             , u, v                , e, e, zLevel, buffer); // top left
        RenderUtils.drawTexturedRectBatched(x, y + height - e, u, v + texHeight - e, e, e, zLevel, buffer); // bottom left

        RenderUtils.drawTexturedRectBatched(x + width - e, y             , u + texWidth - e, v                , e, e, zLevel, buffer); // top right
        RenderUtils.drawTexturedRectBatched(x + width - e, y + height - e, u + texWidth - e, v + texHeight - e, e, e, zLevel, buffer); // bottom right

        // Texture is smaller than the requested width, repeat stuff horizontally
        if (texWidth < width)
        {
            final int repeatableWidth = texWidth - 2 * e;
            final int requiredWidth = width - 2 * e;

            for (int doneWidth = 0, tmpX = x + e, tmpW = 0; doneWidth < requiredWidth; )
            {
                tmpW = Math.min(repeatableWidth, requiredWidth - doneWidth);

                RenderUtils.drawTexturedRectBatched(tmpX, y             , u + e, v                , tmpW, e, zLevel, buffer); // top center
                RenderUtils.drawTexturedRectBatched(tmpX, y + height - e, u + e, v + texHeight - e, tmpW, e, zLevel, buffer); // bottom center

                tmpX += tmpW;
                doneWidth += tmpW;
            }
        }
        // Texture is wide enough, no need to repeat horizontally
        else
        {
            RenderUtils.drawTexturedRectBatched(x + e, y             , u + e, v                , width - 2 * e, e, zLevel, buffer); // top center
            RenderUtils.drawTexturedRectBatched(x + e, y + height - e, u + e, v + texHeight - e, width - 2 * e, e, zLevel, buffer); // bottom center
        }

        // Texture is smaller than the requested height, repeat stuff vertically
        if (texHeight < height)
        {
            final int repeatableHeight = texHeight - 2 * e;
            final int requiredHeight = height - 2 * e;

            for (int doneHeight = 0, tmpY = y + e, tmpH = 0; doneHeight < requiredHeight; )
            {
                tmpH = Math.min(repeatableHeight, requiredHeight - doneHeight);

                RenderUtils.drawTexturedRectBatched(x            , tmpY, u               , v + e, e, tmpH, zLevel, buffer); // left center
                RenderUtils.drawTexturedRectBatched(x + width - e, tmpY, u + texWidth - e, v + e, e, tmpH, zLevel, buffer); // right center

                tmpY += tmpH;
                doneHeight += tmpH;
            }
        }
        // Texture is tall enough, no need to repeat vertically
        else
        {
            RenderUtils.drawTexturedRectBatched(x            , y + e, u               , v + e, e, height - 2 * e, zLevel, buffer); // left center
            RenderUtils.drawTexturedRectBatched(x + width - e, y + e, u + texWidth - e, v + e, e, height - 2 * e, zLevel, buffer); // right center
        }

        // The center part needs to be repeated
        if (texWidth < width || texHeight < height)
        {
            final int repeatableWidth = texWidth - 2 * e;
            final int requiredWidth = width - 2 * e;

            for (int doneWidth = 0, tmpX = x + e, tmpW = 0; doneWidth < requiredWidth; )
            {
                final int repeatableHeight = texHeight - 2 * e;
                final int requiredHeight = height - 2 * e;
                tmpW = Math.min(repeatableWidth, requiredWidth - doneWidth);

                for (int doneHeight = 0, tmpY = y + e, tmpH = 0; doneHeight < requiredHeight; )
                {
                    tmpH = Math.min(repeatableHeight, requiredHeight - doneHeight);

                    RenderUtils.drawTexturedRectBatched(tmpX, tmpY, u + e, v + e, tmpW, tmpH, zLevel, buffer); // center

                    tmpY += tmpH;
                    doneHeight += tmpH;
                }

                tmpX += tmpW;
                doneWidth += tmpW;
            }
        }
        else
        {
            RenderUtils.drawTexturedRectBatched(x + e, y + e, u + e, v + e, width - 2 * e, height - 2 * e, zLevel, buffer); // center
        }

        tessellator.draw();
    }

    public static void renderText(int x, int y, int color, String text)
    {
        String[] parts = text.split("\\\\n");
        FontRenderer textRenderer = mc().fontRenderer;

        for (String line : parts)
        {
            textRenderer.drawString(line, x, y, color);
            y += textRenderer.FONT_HEIGHT + 1;
        }
    }

    public static void renderText(int x, int y, int color, List<String> lines)
    {
        if (lines.isEmpty() == false)
        {
            FontRenderer textRenderer = mc().fontRenderer;

            for (String line : lines)
            {
                textRenderer.drawString(line, x, y, color);
                y += textRenderer.FONT_HEIGHT + 2;
            }
        }
    }

    public static int renderText(int xOff, int yOff, double scale, int textColor, int bgColor,
            HudAlignment alignment, boolean useBackground, boolean useShadow, List<String> lines)
    {
        FontRenderer fontRenderer = mc().fontRenderer;
        final int scaledWidth = GuiUtils.getScaledWindowWidth();
        final int lineHeight = fontRenderer.FONT_HEIGHT + 2;
        final int contentHeight = lines.size() * lineHeight - 2;
        int bgMargin = 2;

        // Only Chuck Norris can divide by zero
        if (scale == 0d)
        {
            return 0;
        }

        if (scale != 1d)
        {
            if (scale != 0)
            {
                xOff = (int) (xOff * scale);
                yOff = (int) (yOff * scale);
            }

            GlStateManager.pushMatrix();
            GlStateManager.scale(scale, scale, 0);
        }

        double posX = xOff + bgMargin;
        double posY = yOff + bgMargin;

        posY = getHudPosY((int) posY, yOff, contentHeight, scale, alignment);
        posY += getHudOffsetForPotions(alignment, scale, mc().player);

        for (String line : lines)
        {
            final int width = fontRenderer.getStringWidth(line);

            switch (alignment)
            {
                case TOP_RIGHT:
                case BOTTOM_RIGHT:
                    posX = (scaledWidth / scale) - width - xOff - bgMargin;
                    break;
                case CENTER:
                    posX = (scaledWidth / scale / 2) - (width / 2) - xOff;
                    break;
                default:
            }

            final int x = (int) posX;
            final int y = (int) posY;
            posY += lineHeight;

            if (useBackground)
            {
                drawRect(x - bgMargin, y - bgMargin, width + bgMargin, bgMargin + fontRenderer.FONT_HEIGHT, bgColor);
            }

            if (useShadow)
            {
                fontRenderer.drawStringWithShadow(line, x, y, textColor);
            }
            else
            {
                fontRenderer.drawString(line, x, y, textColor);
            }
        }

        if (scale != 1d)
        {
            GlStateManager.popMatrix();
        }

        return contentHeight + bgMargin * 2;
    }

    public static void drawHoverText(int x, int y, List<String> textLines)
    {
        drawHoverText(x, y, textLines, 0xFFC0C0C0 , RenderUtils::renderHoverTextBackground);
    }

    public static void drawHoverText(int x, int y, List<String> textLines, int textColor, IBackgroundRenderer backgroundRenderer)
    {
        Minecraft mc = mc();

        if (textLines.isEmpty() == false && GuiUtils.getCurrentScreen() != null)
        {
            FontRenderer font = mc.fontRenderer;
            GlStateManager.disableRescaleNormal();
            disableItemLighting();
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            int maxLineLength = 0;
            int maxWidth = GuiUtils.getCurrentScreen().width;
            List<String> linesNew = new ArrayList<>();

            for (String lineOrig : textLines)
            {
                String[] lines = lineOrig.split("\\\\n");

                for (String line : lines)
                {
                    int length = font.getStringWidth(line);

                    if (length > maxLineLength)
                    {
                        maxLineLength = length;
                    }

                    linesNew.add(line);
                }
            }

            textLines = linesNew;

            final int lineHeight = font.FONT_HEIGHT + 1;
            int textHeight = textLines.size() * lineHeight - 2;
            int textStartX = x + 4;
            int textStartY = Math.max(8, y - textHeight - 6);

            // The text can't fit from the cursor to the right edge of the screen
            if (textStartX + maxLineLength + 6 > maxWidth)
            {
                int leftX = x - maxLineLength - 8;

                // If the text fits from the cursor to the left edge of the screen...
                if (leftX >= 4)
                {
                    textStartX = leftX;
                }
                // otherwise move it to touching the edge of the screen that the cursor is closest to
                else
                {
                    textStartX = x < (maxWidth / 2) ? 4 : Math.max(4, maxWidth - maxLineLength - 6);
                }
            }

            // The hover info would overlap the cursor vertically
            // (because the hover info was clamped to the top of the screen),
            // move it below the cursor instead
            if (textStartY < y && y < textStartY + textHeight)
            {
                textStartY = y + 16;
            }

            backgroundRenderer.renderBackground(textStartX, textStartY, maxLineLength, textHeight);

            for (int i = 0; i < textLines.size(); ++i)
            {
                String str = textLines.get(i);
                font.drawStringWithShadow(str, textStartX, textStartY, textColor);
                textStartY += lineHeight;
            }

            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
            RenderHelper.enableStandardItemLighting();
            GlStateManager.enableRescaleNormal();
        }
    }

    public static void renderHoverTextBackground(int x, int y, int width, int height)
    {
        double zLevel = 300;
        int borderColor = 0xF0100010;
        drawGradientRect(x - 3, y - 4, x + width + 3, y - 3, zLevel, borderColor, borderColor);
        drawGradientRect(x - 3, y + height + 3, x + width + 3, y + height + 4, zLevel, borderColor, borderColor);
        drawGradientRect(x - 3, y - 3, x + width + 3, y + height + 3, zLevel, borderColor, borderColor);
        drawGradientRect(x - 4, y - 3, x - 3, y + height + 3, zLevel, borderColor, borderColor);
        drawGradientRect(x + width + 3, y - 3, x + width + 4, y + height + 3, zLevel, borderColor, borderColor);

        int fillColor1 = 0x505000FF;
        int fillColor2 = 0x5028007F;
        drawGradientRect(x - 3, y - 3 + 1, x - 3 + 1, y + height + 3 - 1, zLevel, fillColor1, fillColor2);
        drawGradientRect(x + width + 2, y - 3 + 1, x + width + 3, y + height + 3 - 1, zLevel, fillColor1, fillColor2);
        drawGradientRect(x - 3, y - 3, x + width + 3, y - 3 + 1, zLevel, fillColor1, fillColor1);
        drawGradientRect(x - 3, y + height + 2, x + width + 3, y + height + 3, zLevel, fillColor2, fillColor2);
    }

    public static int getHudOffsetForPotions(HudAlignment alignment, double scale, EntityPlayer player)
    {
        if (alignment == HudAlignment.TOP_RIGHT)
        {
            // Only Chuck Norris can divide by zero
            if (scale == 0d)
            {
                return 0;
            }

            Collection<PotionEffect> effects = player.getActivePotionEffects();

            if (effects.isEmpty() == false)
            {
                int y1 = 0;
                int y2 = 0;

                for (PotionEffect effect : effects)
                {
                    Potion potion = effect.getPotion();

                    if (effect.doesShowParticles() && potion.hasStatusIcon())
                    {
                        if (potion.isBeneficial())
                        {
                            y1 = 26;
                        }
                        else
                        {
                            y2 = 52;
                            break;
                        }
                    }
                }

                return (int) (Math.max(y1, y2) / scale);
            }
        }

        return 0;
    }

    public static int getHudPosY(int yOrig, int yOffset, int contentHeight, double scale, HudAlignment alignment)
    {
        int scaledHeight = GuiUtils.getScaledWindowHeight();
        int posY = yOrig;

        switch (alignment)
        {
            case BOTTOM_LEFT:
            case BOTTOM_RIGHT:
                posY = (int) ((scaledHeight / scale) - contentHeight - yOffset);
                break;
            case CENTER:
                posY = (int) ((scaledHeight / scale / 2.0d) - (contentHeight / 2.0d) + yOffset);
                break;
            default:
        }

        return posY;
    }

    /**
     * Assumes a BufferBuilder in GL_QUADS mode has been initialized
     */
    public static void drawBlockSpaceAllSidesBatchedQuads(BlockPos pos, Color4f color, double expand, BufferBuilder buffer)
    {
        double minX = pos.getX() - expand;
        double minY = pos.getY() - expand;
        double minZ = pos.getZ() - expand;
        double maxX = pos.getX() + expand + 1;
        double maxY = pos.getY() + expand + 1;
        double maxZ = pos.getZ() + expand + 1;

        drawBoxAllSidesBatchedQuads(minX, minY, minZ, maxX, maxY, maxZ, color, buffer);
    }

    /**
     * Assumes a BufferBuilder in GL_LINES mode has been initialized
     */
    public static void drawBlockSpaceAllOutlinesBatchedLines(BlockPos pos, Color4f color, double expand, BufferBuilder buffer)
    {
        double minX = pos.getX() - expand;
        double minY = pos.getY() - expand;
        double minZ = pos.getZ() - expand;
        double maxX = pos.getX() + expand + 1;
        double maxY = pos.getY() + expand + 1;
        double maxZ = pos.getZ() + expand + 1;

        drawBoxAllEdgesBatchedLines(minX, minY, minZ, maxX, maxY, maxZ, color, buffer);
    }

    /**
     * Assumes a BufferBuilder in GL_QUADS mode has been initialized
     */
    public static void drawBoxAllSidesBatchedQuads(double minX, double minY, double minZ, double maxX, double maxY, double maxZ,
            Color4f color, BufferBuilder buffer)
    {
        drawBoxHorizontalSidesBatchedQuads(minX, minY, minZ, maxX, maxY, maxZ, color, buffer);
        drawBoxTopBatchedQuads(minX, minZ, maxX, maxY, maxZ, color, buffer);
        drawBoxBottomBatchedQuads(minX, minY, minZ, maxX, maxZ, color, buffer);
    }

    /**
     * Draws a box with outlines around the given corner positions.
     * Takes in buffers initialized for GL_QUADS and GL_LINES modes.
     * @param posMin
     * @param posMax
     * @param colorLines
     * @param colorSides
     * @param bufferQuads
     * @param bufferLines
     */
    public static void drawBoxWithEdgesBatched(BlockPos posMin, BlockPos posMax, Color4f colorLines, Color4f colorSides, BufferBuilder bufferQuads, BufferBuilder bufferLines)
    {
        final double x1 = posMin.getX();
        final double y1 = posMin.getY();
        final double z1 = posMin.getZ();
        final double x2 = posMax.getX() + 1;
        final double y2 = posMax.getY() + 1;
        final double z2 = posMax.getZ() + 1;

        fi.dy.masa.malilib.render.RenderUtils.drawBoxAllSidesBatchedQuads(x1, y1, z1, x2, y2, z2, colorSides, bufferQuads);
        fi.dy.masa.malilib.render.RenderUtils.drawBoxAllEdgesBatchedLines(x1, y1, z1, x2, y2, z2, colorLines, bufferLines);
    }

    /**
     * Assumes a BufferBuilder in GL_QUADS mode has been initialized
     */
    public static void drawBoxHorizontalSidesBatchedQuads(double minX, double minY, double minZ, double maxX, double maxY, double maxZ,
            Color4f color, BufferBuilder buffer)
    {
        // West side
        buffer.pos(minX, minY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.pos(minX, minY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.pos(minX, maxY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.pos(minX, maxY, minZ).color(color.r, color.g, color.b, color.a).endVertex();

        // East side
        buffer.pos(maxX, minY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.pos(maxX, minY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.pos(maxX, maxY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.pos(maxX, maxY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();

        // North side
        buffer.pos(maxX, minY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.pos(minX, minY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.pos(minX, maxY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.pos(maxX, maxY, minZ).color(color.r, color.g, color.b, color.a).endVertex();

        // South side
        buffer.pos(minX, minY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.pos(maxX, minY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.pos(maxX, maxY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.pos(minX, maxY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
    }

    /**
     * Assumes a BufferBuilder in GL_QUADS mode has been initialized
     */
    public static void drawBoxTopBatchedQuads(double minX, double minZ, double maxX, double maxY, double maxZ, Color4f color, BufferBuilder buffer)
    {
        // Top side
        buffer.pos(minX, maxY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.pos(maxX, maxY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.pos(maxX, maxY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.pos(minX, maxY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
    }

    /**
     * Assumes a BufferBuilder in GL_QUADS mode has been initialized
     */
    public static void drawBoxBottomBatchedQuads(double minX, double minY, double minZ, double maxX, double maxZ, Color4f color, BufferBuilder buffer)
    {
        // Bottom side
        buffer.pos(maxX, minY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.pos(minX, minY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.pos(minX, minY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.pos(maxX, minY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
    }

    /**
     * Assumes a BufferBuilder in GL_LINES mode has been initialized
     */
    public static void drawBoxAllEdgesBatchedLines(double minX, double minY, double minZ, double maxX, double maxY, double maxZ,
            Color4f color, BufferBuilder buffer)
    {
        // West side
        buffer.pos(minX, minY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.pos(minX, minY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();

        buffer.pos(minX, minY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.pos(minX, maxY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();

        buffer.pos(minX, maxY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.pos(minX, maxY, minZ).color(color.r, color.g, color.b, color.a).endVertex();

        buffer.pos(minX, maxY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.pos(minX, minY, minZ).color(color.r, color.g, color.b, color.a).endVertex();

        // East side
        buffer.pos(maxX, minY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.pos(maxX, minY, minZ).color(color.r, color.g, color.b, color.a).endVertex();

        buffer.pos(maxX, minY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.pos(maxX, maxY, minZ).color(color.r, color.g, color.b, color.a).endVertex();

        buffer.pos(maxX, maxY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.pos(maxX, maxY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();

        buffer.pos(maxX, maxY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.pos(maxX, minY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();

        // North side (don't repeat the vertical lines that are done by the east/west sides)
        buffer.pos(maxX, minY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.pos(minX, minY, minZ).color(color.r, color.g, color.b, color.a).endVertex();

        buffer.pos(minX, maxY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.pos(maxX, maxY, minZ).color(color.r, color.g, color.b, color.a).endVertex();

        // South side (don't repeat the vertical lines that are done by the east/west sides)
        buffer.pos(minX, minY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.pos(maxX, minY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();

        buffer.pos(maxX, maxY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.pos(minX, maxY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
    }

    /**
     * Assumes a BufferBuilder in GL_QUADS mode has been initialized
     */
    public static void drawBlockSpaceSideBatchedQuads(BlockPos pos, EnumFacing side, Color4f color, double expand, BufferBuilder buffer)
    {
        double minX = pos.getX() - expand;
        double minY = pos.getY() - expand;
        double minZ = pos.getZ() - expand;
        double maxX = pos.getX() + expand + 1;
        double maxY = pos.getY() + expand + 1;
        double maxZ = pos.getZ() + expand + 1;

        switch (side)
        {
            case DOWN:
                buffer.pos(maxX, minY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
                buffer.pos(minX, minY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
                buffer.pos(minX, minY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
                buffer.pos(maxX, minY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
                break;

            case UP:
                buffer.pos(minX, maxY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
                buffer.pos(maxX, maxY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
                buffer.pos(maxX, maxY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
                buffer.pos(minX, maxY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
                break;

            case NORTH:
                buffer.pos(maxX, minY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
                buffer.pos(minX, minY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
                buffer.pos(minX, maxY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
                buffer.pos(maxX, maxY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
                break;

            case SOUTH:
                buffer.pos(minX, minY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
                buffer.pos(maxX, minY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
                buffer.pos(maxX, maxY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
                buffer.pos(minX, maxY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
                break;

            case WEST:
                buffer.pos(minX, minY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
                buffer.pos(minX, minY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
                buffer.pos(minX, maxY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
                buffer.pos(minX, maxY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
                break;

            case EAST:
                buffer.pos(maxX, minY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
                buffer.pos(maxX, minY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
                buffer.pos(maxX, maxY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
                buffer.pos(maxX, maxY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
                break;
        }
    }

    public static void drawBox(IntBoundingBox bb, Color4f color, BufferBuilder bufferQuads, BufferBuilder bufferLines)
    {
        double minX = bb.minX;
        double minY = bb.minY;
        double minZ = bb.minZ;
        double maxX = bb.maxX + 1;
        double maxY = bb.maxY + 1;
        double maxZ = bb.maxZ + 1;

        drawBoxAllSidesBatchedQuads(minX, minY, minZ, maxX, maxY, maxZ, color, bufferQuads);
        drawBoxAllEdgesBatchedLines(minX, minY, minZ, maxX, maxY, maxZ, color, bufferLines);
    }

    public static void drawBox(StructureBoundingBox bb, Color4f color, BufferBuilder bufferQuads, BufferBuilder bufferLines)
    {
        double minX = bb.minX;
        double minY = bb.minY;
        double minZ = bb.minZ;
        double maxX = bb.maxX + 1;
        double maxY = bb.maxY + 1;
        double maxZ = bb.maxZ + 1;

        drawBoxAllSidesBatchedQuads(minX, minY, minZ, maxX, maxY, maxZ, color, bufferQuads);
        drawBoxAllEdgesBatchedLines(minX, minY, minZ, maxX, maxY, maxZ, color, bufferLines);
    }

    /**
     * Renders a text plate/billboard, similar to the player name plate.<br>
     * The plate will always face towards the viewer.
     * @param text
     * @param x
     * @param y
     * @param z
     * @param scale
     * @param mc
     */
    public static void drawTextPlate(List<String> text, double x, double y, double z, float scale)
    {
        Entity entity = mc().getRenderViewEntity();

        if (entity != null)
        {
            drawTextPlate(text, x, y, z, entity.rotationYaw, entity.rotationPitch, scale, 0xFFFFFFFF, 0x40000000, true);
        }
    }

    public static void drawTextPlate(List<String> text, double x, double y, double z, float yaw, float pitch,
            float scale, int textColor, int bgColor, boolean disableDepth)
    {
        FontRenderer textRenderer = mc().fontRenderer;

        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);

        GlStateManager.rotate(-yaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(pitch, 1.0F, 0.0F, 0.0F);

        GlStateManager.scale(-scale, -scale, scale);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();

        setupBlend();
        GlStateManager.disableTexture2D();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        int maxLineLen = 0;

        for (String line : text)
        {
            maxLineLen = Math.max(maxLineLen, textRenderer.getStringWidth(line));
        }

        int strLenHalf = maxLineLen / 2;
        int textHeight = textRenderer.FONT_HEIGHT * text.size() - 1;
        float bga = ((bgColor >>> 24) & 0xFF) * 255f;
        float bgr = ((bgColor >>> 16) & 0xFF) * 255f;
        float bgg = ((bgColor >>>  8) & 0xFF) * 255f;
        float bgb = (bgColor          & 0xFF) * 255f;

        if (disableDepth)
        {
            GlStateManager.depthMask(false);
            GlStateManager.disableDepth();
        }

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(-strLenHalf - 1,          -1, 0.0D).color(bgr, bgg, bgb, bga).endVertex();
        buffer.pos(-strLenHalf - 1,  textHeight, 0.0D).color(bgr, bgg, bgb, bga).endVertex();
        buffer.pos( strLenHalf    ,  textHeight, 0.0D).color(bgr, bgg, bgb, bga).endVertex();
        buffer.pos( strLenHalf    ,          -1, 0.0D).color(bgr, bgg, bgb, bga).endVertex();
        tessellator.draw();

        GlStateManager.enableTexture2D();
        int textY = 0;

        // translate the text a bit infront of the background
        if (disableDepth == false)
        {
            GlStateManager.enablePolygonOffset();
            GlStateManager.doPolygonOffset(-0.6f, -1.2f);
            //GlStateManager.translate(0, 0, -0.02);

            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
        }

        for (String line : text)
        {
            if (disableDepth)
            {
                GlStateManager.depthMask(false);
                GlStateManager.disableDepth();

                // Render the faint version that will also show through blocks
                textRenderer.drawString(line, -strLenHalf, textY, 0x20000000 | (textColor & 0xFFFFFF));

                GlStateManager.enableDepth();
                GlStateManager.depthMask(true);
            }

            // Render the actual fully opaque text, that will not show through blocks
            textRenderer.drawString(line, -strLenHalf, textY, textColor);
            textY += textRenderer.FONT_HEIGHT;
        }

        if (disableDepth == false)
        {
            GlStateManager.doPolygonOffset(0f, 0f);
            GlStateManager.disablePolygonOffset();
        }

        color(1f, 1f, 1f, 1f);
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static void renderBlockTargetingOverlay(Entity entity, BlockPos pos, EnumFacing side, Vec3d hitVec,
            Color4f color, float partialTicks)
    {
        EnumFacing playerFacing = entity.getHorizontalFacing();
        HitPart part = PositionUtils.getHitPart(side, playerFacing, pos, hitVec);

        double dx = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
        double dy = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
        double dz = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;

        double x = pos.getX() + 0.5d - dx;
        double y = pos.getY() + 0.5d - dy;
        double z = pos.getZ() + 0.5d - dz;

        GlStateManager.pushMatrix();

        blockTargetingOverlayTranslations(x, y, z, side, playerFacing);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        float quadAlpha = 0.18f;
        float ha = color.a;
        float hr = color.r;
        float hg = color.g;
        float hb = color.b;

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        // White full block background
        buffer.pos(x - 0.5, y - 0.5, z).color(1f, 1f, 1f, quadAlpha).endVertex();
        buffer.pos(x + 0.5, y - 0.5, z).color(1f, 1f, 1f, quadAlpha).endVertex();
        buffer.pos(x + 0.5, y + 0.5, z).color(1f, 1f, 1f, quadAlpha).endVertex();
        buffer.pos(x - 0.5, y + 0.5, z).color(1f, 1f, 1f, quadAlpha).endVertex();

        switch (part)
        {
            case CENTER:
                buffer.pos(x - 0.25, y - 0.25, z).color(hr, hg, hb, ha).endVertex();
                buffer.pos(x + 0.25, y - 0.25, z).color(hr, hg, hb, ha).endVertex();
                buffer.pos(x + 0.25, y + 0.25, z).color(hr, hg, hb, ha).endVertex();
                buffer.pos(x - 0.25, y + 0.25, z).color(hr, hg, hb, ha).endVertex();
                break;
            case LEFT:
                buffer.pos(x - 0.50, y - 0.50, z).color(hr, hg, hb, ha).endVertex();
                buffer.pos(x - 0.25, y - 0.25, z).color(hr, hg, hb, ha).endVertex();
                buffer.pos(x - 0.25, y + 0.25, z).color(hr, hg, hb, ha).endVertex();
                buffer.pos(x - 0.50, y + 0.50, z).color(hr, hg, hb, ha).endVertex();
                break;
            case RIGHT:
                buffer.pos(x + 0.50, y - 0.50, z).color(hr, hg, hb, ha).endVertex();
                buffer.pos(x + 0.25, y - 0.25, z).color(hr, hg, hb, ha).endVertex();
                buffer.pos(x + 0.25, y + 0.25, z).color(hr, hg, hb, ha).endVertex();
                buffer.pos(x + 0.50, y + 0.50, z).color(hr, hg, hb, ha).endVertex();
                break;
            case TOP:
                buffer.pos(x - 0.50, y + 0.50, z).color(hr, hg, hb, ha).endVertex();
                buffer.pos(x - 0.25, y + 0.25, z).color(hr, hg, hb, ha).endVertex();
                buffer.pos(x + 0.25, y + 0.25, z).color(hr, hg, hb, ha).endVertex();
                buffer.pos(x + 0.50, y + 0.50, z).color(hr, hg, hb, ha).endVertex();
                break;
            case BOTTOM:
                buffer.pos(x - 0.50, y - 0.50, z).color(hr, hg, hb, ha).endVertex();
                buffer.pos(x - 0.25, y - 0.25, z).color(hr, hg, hb, ha).endVertex();
                buffer.pos(x + 0.25, y - 0.25, z).color(hr, hg, hb, ha).endVertex();
                buffer.pos(x + 0.50, y - 0.50, z).color(hr, hg, hb, ha).endVertex();
                break;
            default:
        }

        tessellator.draw();

        GlStateManager.glLineWidth(1.6f);

        buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);

        // Middle small rectangle
        buffer.pos(x - 0.25, y - 0.25, z).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(x + 0.25, y - 0.25, z).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(x + 0.25, y + 0.25, z).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(x - 0.25, y + 0.25, z).color(1f, 1f, 1f, 1f).endVertex();
        tessellator.draw();

        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        // Bottom left
        buffer.pos(x - 0.50, y - 0.50, z).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(x - 0.25, y - 0.25, z).color(1f, 1f, 1f, 1f).endVertex();

        // Top left
        buffer.pos(x - 0.50, y + 0.50, z).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(x - 0.25, y + 0.25, z).color(1f, 1f, 1f, 1f).endVertex();

        // Bottom right
        buffer.pos(x + 0.50, y - 0.50, z).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(x + 0.25, y - 0.25, z).color(1f, 1f, 1f, 1f).endVertex();

        // Top right
        buffer.pos(x + 0.50, y + 0.50, z).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(x + 0.25, y + 0.25, z).color(1f, 1f, 1f, 1f).endVertex();
        tessellator.draw();

        GlStateManager.popMatrix();
    }

    public static void renderBlockTargetingOverlaySimple(Entity entity, BlockPos pos, EnumFacing side,
            Color4f color, float partialTicks)
    {
        EnumFacing playerFacing = entity.getHorizontalFacing();

        double dx = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
        double dy = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
        double dz = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;

        double x = pos.getX() + 0.5d - dx;
        double y = pos.getY() + 0.5d - dy;
        double z = pos.getZ() + 0.5d - dz;

        GlStateManager.pushMatrix();

        blockTargetingOverlayTranslations(x, y, z, side, playerFacing);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        float a = color.a;
        float r = color.r;
        float g = color.g;
        float b = color.b;

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        // Simple colored quad
        buffer.pos(x - 0.5, y - 0.5, z).color(r, g, b, a).endVertex();
        buffer.pos(x + 0.5, y - 0.5, z).color(r, g, b, a).endVertex();
        buffer.pos(x + 0.5, y + 0.5, z).color(r, g, b, a).endVertex();
        buffer.pos(x - 0.5, y + 0.5, z).color(r, g, b, a).endVertex();

        tessellator.draw();

        GlStateManager.glLineWidth(1.6f);

        buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);

        // Middle rectangle
        buffer.pos(x - 0.375, y - 0.375, z).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(x + 0.375, y - 0.375, z).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(x + 0.375, y + 0.375, z).color(1f, 1f, 1f, 1f).endVertex();
        buffer.pos(x - 0.375, y + 0.375, z).color(1f, 1f, 1f, 1f).endVertex();

        tessellator.draw();

        GlStateManager.popMatrix();
    }

    private static void blockTargetingOverlayTranslations(double x, double y, double z, EnumFacing side, EnumFacing playerFacing)
    {
        GlStateManager.translate(x, y, z);

        switch (side)
        {
            case DOWN:
                GlStateManager.rotate(180f - playerFacing.getHorizontalAngle(), 0, 1f, 0);
                GlStateManager.rotate( 90f, 1f, 0, 0);
                break;
            case UP:
                GlStateManager.rotate(180f - playerFacing.getHorizontalAngle(), 0, 1f, 0);
                GlStateManager.rotate(-90f, 1f, 0, 0);
                break;
            case NORTH:
                GlStateManager.rotate(180f, 0, 1f, 0);
                break;
            case SOUTH:
                GlStateManager.rotate(   0, 0, 1f, 0);
                break;
            case WEST:
                GlStateManager.rotate(-90f, 0, 1f, 0);
                break;
            case EAST:
                GlStateManager.rotate( 90f, 0, 1f, 0);
                break;
        }

        GlStateManager.translate(-x, -y, -z + 0.501);
    }

    public static void renderMapPreview(ItemStack stack, int x, int y, int dimensions)
    {
        if (stack.getItem() instanceof ItemMap && GuiBase.isShiftDown())
        {
            GlStateManager.pushMatrix();
            GlStateManager.disableLighting();
            color(1f, 1f, 1f, 1f);

            int y1 = y - dimensions - 20;
            int y2 = y1 + dimensions;
            int x1 = x + 8;
            int x2 = x1 + dimensions;
            int z = 300;

            bindTexture(fi.dy.masa.malilib.render.RenderUtils.TEXTURE_MAP_BACKGROUND);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();

            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            buffer.pos(x1, y2, z).tex(0.0D, 1.0D).endVertex();
            buffer.pos(x2, y2, z).tex(1.0D, 1.0D).endVertex();
            buffer.pos(x2, y1, z).tex(1.0D, 0.0D).endVertex();
            buffer.pos(x1, y1, z).tex(0.0D, 0.0D).endVertex();

            tessellator.draw();

            MapData mapdata = Items.FILLED_MAP.getMapData(stack, mc().world);

            if (mapdata != null)
            {
                x1 += 8;
                y1 += 8;
                z = 310;
                double scale = (double) (dimensions - 16) / 128.0D;
                GlStateManager.translate(x1, y1, z);
                GlStateManager.scale(scale, scale, 0);
                mc().entityRenderer.getMapItemRenderer().renderMap(mapdata, false);
            }

            GlStateManager.enableLighting();
            GlStateManager.popMatrix();

            color(1f, 1f, 1f, 1f);
        }
    }

    public static void renderShulkerBoxPreview(ItemStack stack, int x, int y, boolean useBgColors)
    {
        if (stack.hasTagCompound())
        {
            NonNullList<ItemStack> items = InventoryUtils.getStoredItems(stack, -1);

            if (items.size() == 0)
            {
                return;
            }

            GlStateManager.pushMatrix();
            disableItemLighting();
            GlStateManager.translate(0F, 0F, 700F);

            InventoryOverlay.InventoryRenderType type = InventoryOverlay.getInventoryType(stack);
            InventoryOverlay.InventoryProperties props = InventoryOverlay.getInventoryPropsTemp(type, items.size());

            x += 8;
            y -= (props.height + 18);

            if (stack.getItem() instanceof ItemShulkerBox)
            {
                setShulkerboxBackgroundTintColor((BlockShulkerBox) ((ItemBlock) stack.getItem()).getBlock(), useBgColors);
            }
            else
            {
                color(1f, 1f, 1f, 1f);
            }

            InventoryOverlay.renderInventoryBackground(type, x, y, props.slotsPerRow, items.size(), mc());

            enableGuiItemLighting();
            GlStateManager.enableDepth();
            GlStateManager.enableRescaleNormal();

            IInventory inv = fi.dy.masa.malilib.util.InventoryUtils.getAsInventory(items);
            InventoryOverlay.renderInventoryStacks(type, inv, x + props.slotOffsetX, y + props.slotOffsetY, props.slotsPerRow, 0, -1, mc());

            GlStateManager.disableDepth();
            GlStateManager.popMatrix();

            color(1f, 1f, 1f, 1f);
        }
    }

    /**
     * Calls RenderUtils.color() with the dye color of the provided shulker box block's color
     * @param block
     * @param useBgColors
     */
    public static void setShulkerboxBackgroundTintColor(@Nullable BlockShulkerBox block, boolean useBgColors)
    {
        // In 1.13+ there is the separate uncolored Shulker Box variant, which returns null from getColor().
        // In that case don't tint the background.
        if (useBgColors && block != null && block.getColor() != null)
        {
            final EnumDyeColor dye = block.getColor();
            final float[] colors = dye.getColorComponentValues();
            color(colors[0], colors[1], colors[2], 1f);
        }
        else
        {
            color(1f, 1f, 1f, 1f);
        }
    }

    public static void renderModelInGui(int x, int y, IBakedModel model, IBlockState state, float zLevel)
    {
        if (state.getBlock() == Blocks.AIR)
        {
            return;
        }

        GlStateManager.pushMatrix();

        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        mc().getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);

        GlStateManager.enableRescaleNormal();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        color(1f, 1f, 1f, 1f);

        setupGuiTransform(x, y, model.isGui3d(), zLevel);
        //model.getItemCameraTransforms().applyTransform(ItemCameraTransforms.TransformType.GUI);
        GlStateManager.rotate( 30, 1, 0, 0);
        GlStateManager.rotate(225, 0, 1, 0);
        GlStateManager.scale(0.625, 0.625, 0.625);

        renderModel(model, state);

        GlStateManager.disableAlpha();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableLighting();
        GlStateManager.popMatrix();

        color(1f, 1f, 1f, 1f);
    }

    public static void setupGuiTransform(int xPosition, int yPosition, boolean isGui3d, float zLevel)
    {
        GlStateManager.translate(xPosition, yPosition, 100.0F + zLevel);
        GlStateManager.translate(8.0F, 8.0F, 0.0F);
        GlStateManager.scale(1.0F, -1.0F, 1.0F);
        GlStateManager.scale(16.0F, 16.0F, 16.0F);

        if (isGui3d)
        {
            GlStateManager.enableLighting();
        }
        else
        {
            GlStateManager.disableLighting();
        }
    }

    private static void renderModel(IBakedModel model, IBlockState state)
    {
        GlStateManager.pushMatrix();
        GlStateManager.translate(-0.5F, -0.5F, -0.5F);
        int color = 0xFFFFFFFF;

        if (model.isBuiltInRenderer() == false)
        {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);

            for (EnumFacing enumfacing : EnumFacing.values())
            {
                renderQuads(bufferbuilder, model.getQuads(state, enumfacing, 0L), state, color);
            }

            renderQuads(bufferbuilder, model.getQuads(state, null, 0L), state, color);
            tessellator.draw();
        }

        GlStateManager.popMatrix();
    }

    private static void renderQuads(BufferBuilder renderer, List<BakedQuad> quads, IBlockState state, int color)
    {
        final int quadCount = quads.size();

        for (int i = 0; i < quadCount; ++i)
        {
            BakedQuad quad = quads.get(i);
            renderQuad(renderer, quad, state, 0xFFFFFFFF);
        }
    }

    private static void renderQuad(BufferBuilder buffer, BakedQuad quad, IBlockState state, int color)
    {
        buffer.addVertexData(quad.getVertexData());
        buffer.putColor4(color);

        if (quad.hasTintIndex())
        {
            BlockColors blockColors = mc().getBlockColors();
            int m = blockColors.colorMultiplier(state, null, null, quad.getTintIndex());

            float r = (float) (m >>> 16 & 0xFF) / 255F;
            float g = (float) (m >>>  8 & 0xFF) / 255F;
            float b = (float) (m        & 0xFF) / 255F;
            buffer.putColorMultiplier(r, g, b, 4);
            buffer.putColorMultiplier(r, g, b, 3);
            buffer.putColorMultiplier(r, g, b, 2);
            buffer.putColorMultiplier(r, g, b, 1);
        }

        putQuadNormal(buffer, quad);
    }

    private static void putQuadNormal(BufferBuilder renderer, BakedQuad quad)
    {
        Vec3i direction = quad.getFace().getDirectionVec();
        renderer.putNormal(direction.getX(), direction.getY(), direction.getZ());
    }

    private static Minecraft mc()
    {
        return Minecraft.getMinecraft();
    }

    /*
    public static void enableGUIStandardItemLighting(float scale)
    {
        GlStateManager.pushMatrix();
        GlStateManager.rotate(-30.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(165.0F, 1.0F, 0.0F, 0.0F);

        enableStandardItemLighting(scale);

        GlStateManager.popMatrix();
    }

    public static void enableStandardItemLighting(float scale)
    {
        GlStateManager.enableLighting();
        GlStateManager.enableLight(0);
        GlStateManager.enableLight(1);
        GlStateManager.enableColorMaterial();
        RenderUtils.colorMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT_AND_DIFFUSE);
        GlStateManager.glLight(GL11.GL_LIGHT0, GL11.GL_POSITION, RenderHelper.setColorBuffer((float) LIGHT0_POS.x, (float) LIGHT0_POS.y, (float) LIGHT0_POS.z, 0.0f));

        float lightStrength = 0.3F * scale;
        GlStateManager.glLight(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, RenderHelper.setColorBuffer(lightStrength, lightStrength, lightStrength, 1.0F));
        GlStateManager.glLight(GL11.GL_LIGHT0, GL11.GL_AMBIENT, RenderHelper.setColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));
        GlStateManager.glLight(GL11.GL_LIGHT0, GL11.GL_SPECULAR, RenderHelper.setColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));
        GlStateManager.glLight(GL11.GL_LIGHT1, GL11.GL_POSITION, RenderHelper.setColorBuffer((float) LIGHT1_POS.x, (float) LIGHT1_POS.y, (float) LIGHT1_POS.z, 0.0f));
        GlStateManager.glLight(GL11.GL_LIGHT1, GL11.GL_DIFFUSE, RenderHelper.setColorBuffer(lightStrength, lightStrength, lightStrength, 1.0F));
        GlStateManager.glLight(GL11.GL_LIGHT1, GL11.GL_AMBIENT, RenderHelper.setColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));
        GlStateManager.glLight(GL11.GL_LIGHT1, GL11.GL_SPECULAR, RenderHelper.setColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));

        GlStateManager.shadeModel(GL11.GL_FLAT);

        float ambientLightStrength = 0.4F;
        GlStateManager.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, RenderHelper.setColorBuffer(ambientLightStrength, ambientLightStrength, ambientLightStrength, 1.0F));
    }
    */
}
