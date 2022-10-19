package malilib.mixin.input;

import malilib.event.dispatch.TickEventDispatcherImpl;
import malilib.input.KeyBindImpl;
import malilib.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.MinecraftClient;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin
{
    @Inject(method = "tick()V", at = @At("RETURN"))
    private void malilib_onPostKeyboardInput(CallbackInfo ci)
    {
        KeyBindImpl.reCheckPressedKeys();
        ((TickEventDispatcherImpl) Registry.TICK_EVENT_DISPATCHER).onClientTick();
    }
}