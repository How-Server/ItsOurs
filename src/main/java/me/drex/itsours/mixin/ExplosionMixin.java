package me.drex.itsours.mixin;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.claim.flags.FlagsManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ListIterator;
import java.util.Optional;

@Mixin(value = Explosion.class, priority = 500)
public abstract class ExplosionMixin {

    @Shadow
    @Final
    private World world;

    @Shadow
    @Final
    private ObjectArrayList<BlockPos> affectedBlocks;

    @Inject(
        method = "affectWorld",
        at = @At("HEAD")
    )
    public void itsours$canExplosionAffectBlock(boolean bl, CallbackInfo ci) {
        ListIterator<BlockPos> iterator = this.affectedBlocks.listIterator();
        while (iterator.hasNext()) {
            BlockPos blockPos = iterator.next();
            Optional<AbstractClaim> claim = ClaimList.getClaimAt(this.world, blockPos);
            if (claim.isPresent() && !claim.get().checkAction(null, FlagsManager.EXPLOSIONS)) {
                iterator.remove();
            }
        }
    }

}
