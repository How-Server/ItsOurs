package me.drex.itsours.mixin;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.claim.flags.Flags;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(value = ExperienceOrbEntity.class)
public abstract class XpAbsorbMixin {

    @Unique
    private int amount;

    @Inject(
            method = "onPlayerCollision",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void itsours$xpAbsorb(PlayerEntity player, CallbackInfo ci) {
        Optional<AbstractClaim> claim = ClaimList.getClaimAt(player.getEntityWorld(), player.getSteppingPos());
        if (claim.isPresent() && !claim.get().checkAction(player.getUuid(), Flags.XP_ABSORB)) {
            ci.cancel();
            amount = 0;
        }
    }
}
