package fi.dy.masa.malilib.gui.button;

import java.util.List;
import javax.annotation.Nullable;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.widgets.WidgetBase;
import fi.dy.masa.malilib.util.StringUtils;

public abstract class ButtonBase extends WidgetBase
{
    protected static final ResourceLocation BUTTON_TEXTURES = new ResourceLocation("minecraft", "textures/gui/widgets.png");

    protected final ImmutableList<String> hoverHelp;
    protected String displayString;
    protected boolean automaticWidth;
    protected boolean enabled = true;
    protected boolean hovered;
    protected boolean hoverInfoRequiresShift;
    protected boolean playClickSound = true;
    protected boolean visible = true;
    @Nullable protected IButtonActionListener actionListener;

    public ButtonBase(int x, int y, int width, int height)
    {
        this(x, y, width, height, "");
    }

    public ButtonBase(int x, int y, int width, int height, String text)
    {
        this(x, y, width, height, text, null);
    }

    public ButtonBase(int x, int y, int width, int height, String text, @Nullable IButtonActionListener actionListener)
    {
        super(x, y, width, height);

        this.displayString = StringUtils.translate(text);
        this.hoverHelp = ImmutableList.of(StringUtils.translate("malilib.gui.button.hover.hold_shift_for_info"));

        if (width < 0)
        {
            this.automaticWidth = true;
            this.autoCalculateWidth(this.displayString);
        }
    }

    public ButtonBase setActionListener(@Nullable IButtonActionListener actionListener)
    {
        this.actionListener = actionListener;
        return this;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public void setPlayClickSound(boolean playSound)
    {
        this.playClickSound = playSound;
    }

    public void setDisplayString(String text)
    {
        this.displayString = text;
    }

    protected int autoCalculateWidth(String text)
    {
        this.setWidth(this.getStringWidth(text) + 10);
        return this.width;
    }

    public boolean isMouseOver()
    {
        return this.hovered;
    }

    @Override
    protected boolean onMouseClickedImpl(int mouseX, int mouseY, int mouseButton)
    {
        if (this.playClickSound)
        {
            this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }

        if (this.actionListener != null)
        {
            this.actionListener.actionPerformedWithButton(this, mouseButton);
        }

        return true;
    }

    @Override
    public boolean onMouseScrolledImpl(int mouseX, int mouseY, double mouseWheelDelta)
    {
        int mouseButton = mouseWheelDelta < 0 ? 1 : 0;
        return this.onMouseClickedImpl(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean isMouseOver(int mouseX, int mouseY)
    {
        return this.enabled && this.visible && super.isMouseOver(mouseX, mouseY);
    }

    public void updateDisplayString()
    {
        this.displayString = this.generateDisplayString();

        if (this.automaticWidth)
        {
            this.autoCalculateWidth(this.displayString);
        }
    }

    protected String generateDisplayString()
    {
        return this.displayString;
    }

    public void setHoverInfoRequiresShift(boolean requireShift)
    {
        this.hoverInfoRequiresShift = requireShift;
    }

    @Override
    public List<String> getHoverStrings()
    {
        if (this.hoverInfoRequiresShift && GuiBase.isShiftDown() == false)
        {
            return this.hoverHelp;
        }

        return super.getHoverStrings();
    }

    protected int getTextureOffset(boolean isMouseOver)
    {
        return (this.enabled == false) ? 0 : (isMouseOver ? 2 : 1);
    }
}
