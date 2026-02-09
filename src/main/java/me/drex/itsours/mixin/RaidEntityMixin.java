package me.drex.itsours.mixin;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.claim.flags.Flags;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PatrolEntity;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.village.raid.Raid;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(RaiderEntity.class)
public abstract class RaidEntityMixin extends PatrolEntity {

    @Shadow
    @Nullable
    public abstract Raid getRaid();

    protected RaidEntityMixin(EntityType<? extends PatrolEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "tickMovement", at = @At("HEAD"))
    protected void tick(CallbackInfo ci) {
        Raid raid = this.getRaid();
        Optional<AbstractClaim> claim = ClaimList.getClaimAt(this.getEntityWorld(), this.getBlockPos());
        if (claim.isPresent() && !claim.get().checkAction(null, Flags.MOB_SPAWN)) {
            assert raid != null;
            raid.getAllRaiders().forEach(raiderEntity -> raiderEntity.remove(RemovalReason.DISCARDED));
            raid.invalidate();
        }
    }
}