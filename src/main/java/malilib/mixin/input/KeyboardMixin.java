package malilib.mixin.input;

import malilib.input.InputDispatcherImpl;
import malilib.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.Keyboard;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin// implements F3KeyStateSetter
{
    @Shadow private boolean switchF3State;

    //@Override
    public void setF3KeyState(boolean value)
    {
        this.switchF3State = value;
    }

    @Inject(method = "onKey", cancellable = true,
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/Keyboard;debugCrashStartTime:J", ordinal = 0))
    private void malilib_onKeyboardInput(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo ci)
    {
        if (((InputDispatcherImpl) Registry.INPUT_DISPATCHER).onKeyInput(key, scanCode, modifiers, action != 0))
        {
            ci.cancel();
        }
    }
}