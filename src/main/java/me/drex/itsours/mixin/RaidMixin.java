package me.drex.itsours.mixin;

import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.claim.flags.Flags;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.village.raid.Raid;
import net.minecraft.village.raid.RaidManager;
import net.minecraft.world.PersistentState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RaidManager.class)
public abstract class RaidMixin extends PersistentState {
    @Inject(method = "startRaid", at = @At("HEAD"), cancellable = true)
    public void startRaid(ServerPlayerEntity player, BlockPos pos, CallbackInfoReturnable<Raid> cir) {
        long x = player.getBlockX();
        long y = player.getBlockY();
        long z = player.getBlockZ();
        long distance = 64;
        for (Entity entity : player.getWorld().getOtherEntities(player, new Box(
                x - distance, y - distance, z - distance, x + distance, y + distance, z + distance
        ))) {
            if (entity instanceof VillagerEntity && ClaimList.getClaimAt(entity).isPresent() &&  !ClaimList.getClaimAt(entity).get().checkAction(null, Flags.MOB_SPAWN)) {
                cir.setReturnValue(null);
                cir.cancel();
                break;
            }
        }
    }
}