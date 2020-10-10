package me.drex.itsours.mixin;

import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.user.ClaimPlayer;
import me.drex.itsours.util.WorldUtil;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {

    private AbstractClaim pclaim = null;

    @Inject(method = "setPos", at = @At("HEAD"))
    public void doPrePosActions(double x, double y, double z, CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
            if (player.getBlockPos() == null) return;
            pclaim = ItsOursMod.INSTANCE.getClaimList().get((ServerWorld) player.world, player.getBlockPos());
        }
    }

    @Inject(method = "setPos", at = @At("RETURN"))
    public void doPostPosActions(double x, double y, double z, CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
            if (player.getBlockPos() == null) return;
            AbstractClaim claim = ItsOursMod.INSTANCE.getClaimList().get((ServerWorld) player.world, player.getBlockPos());
            if (pclaim != claim && player instanceof ServerPlayerEntity) {
                if (player.networkHandler != null) {
                    ClaimPlayer claimPlayer = (ClaimPlayer) player;
                    Text message = null;
                    if (pclaim != null && claim == null) {
                        message = new LiteralText("You left " + pclaim.getFullName()).formatted(Formatting.YELLOW);
                        //TODO: Make configurable
                        boolean cachedFlying = player.abilities.flying;
                        //update abilities for respective gamemode
                        player.interactionManager.getGameMode().setAbilities(player.abilities);
                        //check if the player was flying before they entered the claim
                        if (claimPlayer.getFlightCache()) {
                            player.abilities.flying = cachedFlying;
                            player.abilities.allowFlying = true;
                        }
                        if (cachedFlying && !player.abilities.flying) {
                            BlockPos pos = getPosOnGround(player.getBlockPos(), player.getServerWorld());
                            if (pos.getY() + 3 < player.getY())
                            player.teleport((ServerWorld) WorldUtil.DEFAULT_WORLD, player.getX(), pos.getY(), player.getZ(), player.yaw, player.pitch);
                        }
                        player.sendAbilitiesUpdate();
                    } else if (claim != null) {
                        if (pclaim == null) claimPlayer.cacheFlight(player.abilities.allowFlying);
                        boolean cachedFlying = player.abilities.flying;
                        //update abilities for respective gamemode
                        player.interactionManager.getGameMode().setAbilities(player.abilities);
                        //enable flying if player enabled it
                        if (!player.abilities.allowFlying) player.abilities.allowFlying = claimPlayer.flightEnabled();
                        //set the flight state to what it was before entering
                        if (player.abilities.allowFlying) player.abilities.flying = cachedFlying;
                        player.sendAbilitiesUpdate();
                        message = new LiteralText("Welcome to " + claim.getFullName()).formatted(Formatting.YELLOW);
                    }

                    if (message != null) {
                        player.networkHandler.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.ACTIONBAR, message, -1, 20, -1));
                    }
                }
            }
        }
    }

    public BlockPos getPosOnGround(BlockPos pos, World world) {
        BlockPos blockPos = new BlockPos(pos.getX(), pos.getY(), pos.getZ());
        do {
            blockPos = blockPos.down();
            if (blockPos.getY() < 1) {
                return pos;
            }
        } while (world.getBlockState(blockPos).isAir());

        return blockPos.up();
    }

}
