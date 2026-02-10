package me.drex.itsours.mixin;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.flags.Flags;
import me.drex.itsours.claim.list.ClaimList;
import net.minecraft.entity.decoration.BlockAttachedEntity;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(BlockAttachedEntity.class)
public abstract class BlockAttachedEntityMixin {
    @Inject(method = "isImmuneToExplosion", at = @At("HEAD"), cancellable = true)
    private void cancelExplosion(Explosion explosion, CallbackInfoReturnable<Boolean> cir) {
        Optional<AbstractClaim> claim = ClaimList.getClaimAt(explosion.getWorld(), explosion.getEntity().getBlockPos());
        if (claim.isEmpty()) return;
        if (!claim.get().checkAction(null, Flags.EXPLOSIONS)) {
            cir.setReturnValue(true);
        }
    }
}