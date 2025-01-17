package me.drex.itsours.mixin;

import com.mojang.authlib.GameProfile;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.data.DataManager;
import me.drex.itsours.user.ClaimSelectingPlayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements ClaimSelectingPlayer {

    @Nullable
    private BlockPos firstPos = null;
    @Nullable
    private BlockPos secondPos = null;
    private AbstractClaim lastShowClaim;
    private BlockPos lastShowPos;
    private ServerWorld lastShowWorld;

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Shadow
    public abstract void sendMessage(Text message);

    @Shadow @Final public MinecraftServer server;

    @Override
    public boolean arePositionsSet() {
        return firstPos != null && secondPos != null;
    }

    @Override
    public void resetSelection() {
        this.firstPos = null;
        this.secondPos = null;
        DataManager.updateUserData(getUuid()).setSelect(false);
    }

    @Override
    public BlockPos getSecondPosition() {
        return secondPos;
    }

    @Override
    public void setSecondPosition(BlockPos pos) {
        secondPos = pos;
    }

    @Override
    public BlockPos getFirstPosition() {
        return firstPos;
    }

    @Override
    public void setFirstPosition(BlockPos pos) {
        firstPos = pos;
    }
}
