package me.drex.itsours.mixin;

import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.claim.permission.PermissionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
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
    public void startRaid(ServerPlayerEntity player, CallbackInfoReturnable<Raid> cir) {
        long x = player.getBlockX();
        long y = player.getBlockY();
        long z = player.getBlockZ();
        long distance = 64;
        for (Entity entity : player.getServerWorld().getOtherEntities(player, new Box(
                x - distance, y - distance, z - distance, x + distance, y + distance, z + distance
        ))) {
            if (entity instanceof VillagerEntity && ClaimList.getClaimAt(entity).isPresent() &&  !ClaimList.getClaimAt(entity).get().hasPermission(null, PermissionManager.MOB_SPAWN)) {
                cir.setReturnValue(null);
                cir.cancel();
                break;
            }
        }
    }
}