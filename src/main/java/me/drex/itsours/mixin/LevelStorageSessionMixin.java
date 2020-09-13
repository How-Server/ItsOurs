package me.drex.itsours.mixin;

import me.drex.itsours.ItsOursMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelStorage.Session.class)
public class LevelStorageSessionMixin {

    @Inject(method = "method_27426", at = @At("HEAD"))
    public void itsours$onsave(DynamicRegistryManager dynamicRegistryManager, SaveProperties saveProperties, CompoundTag compoundTag, CallbackInfo ci) {
        if (ItsOursMod.INSTANCE != null) ItsOursMod.INSTANCE.save();
    }

}
