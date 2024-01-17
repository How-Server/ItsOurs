package me.drex.itsours.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.claim.permission.PermissionManager;
import me.drex.itsours.claim.permission.node.Node;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

import static me.drex.message.api.LocalizedMessage.localized;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class ServerPlayerInteractionManagerMixin {

    public BlockPos pos;
    @Shadow
    @Final
    protected ServerPlayerEntity player;
    @Shadow
    protected ServerWorld world;

    @ModifyExpressionValue(
            method = "tryBreakBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;isBlockBreakingRestricted(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/GameMode;)Z"
            )
    )
    private boolean itsours$canBreakBlock(boolean original, BlockPos pos) {
        Optional<AbstractClaim> claim = ClaimList.getClaimAt(world, pos);
        if (claim.isEmpty()) return original;
        if (!claim.get().hasPermission(this.player.getUuid(), PermissionManager.MINE, Node.registry(Registries.BLOCK, this.world.getBlockState(pos).getBlock()))) {
            player.sendMessage(localized("text.itsours.action.disallowed.break_block"), true);
            return true;
        }
        return original;
    }

    @WrapOperation(
            method = "interactBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/BlockState;onUse(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;"
            )
    )
    private ActionResult itsours$canInteractBlock(BlockState blockState, World world, PlayerEntity playerEntity, Hand hand, BlockHitResult hit, Operation<ActionResult> original) {
        Optional<AbstractClaim> claim = ClaimList.getClaimAt(world, hit.getBlockPos());
        if ((blockState.getBlock().toString().equals("Block{universal_shops:trade_block}")
                && (claim.isEmpty()
                || !claim.get().getMainClaim().getName().equals("City")))) {
            player.sendMessage(Text.of(" 請在City內交易 "), true);
            return ActionResult.FAIL;
        }
        if (claim.isEmpty()
                || !PermissionManager.INTERACT_BLOCK_PREDICATE.test(blockState.getBlock())
                || blockState.getBlock().toString().equals("Block{universal_shops:trade_block}")
                || blockState.getBlock().toString().equals("Block{universal_shops:admin_trade_block}"))
            return original.call(blockState, world, playerEntity, hand, hit);
        if (!claim.get().hasPermission(playerEntity.getUuid(), PermissionManager.INTERACT_BLOCK, Node.registry(Registries.BLOCK, blockState.getBlock()))) {
            player.sendMessage(localized("text.itsours.action.disallowed.interact_block"), true);
            return ActionResult.FAIL;
        }
        return original.call(blockState, world, playerEntity, hand, hit);
    }

    @WrapOperation(
            method = "processBlockBreakingAction",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/BlockState;onBlockBreakStart(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/PlayerEntity;)V"
            )
    )
    private void itsours$canInteractBlock2(BlockState blockState, World world, BlockPos pos, PlayerEntity playerEntity, Operation<Void> original) {
        Optional<AbstractClaim> claim = ClaimList.getClaimAt(world, pos);
        if (claim.isEmpty() || !PermissionManager.INTERACT_BLOCK_PREDICATE.test(blockState.getBlock())) {
            original.call(blockState, world, pos, playerEntity);
            return;
        }
        if (!claim.get().hasPermission(playerEntity.getUuid(), PermissionManager.INTERACT_BLOCK, Node.registry(Registries.BLOCK, blockState.getBlock()))) {
            player.sendMessage(localized("text.itsours.action.disallowed.interact_block"), true);
            return;
        }
        original.call(blockState, world, pos, playerEntity);
    }

    @WrapOperation(
            method = "interactBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;"
            )
    )
    private ActionResult itsours$canUseOnBlock(ItemStack itemStack, ItemUsageContext context, Operation<ActionResult> original) {
        Optional<AbstractClaim> claim = ClaimList.getClaimAt(context);
        if (claim.isEmpty() || !PermissionManager.USE_ON_BLOCK_PREDICATE.test(itemStack.getItem()))
            return original.call(itemStack, context);
        if (!claim.get().hasPermission(player.getUuid(), PermissionManager.USE_ON_BLOCK, Node.registry(Registries.ITEM, itemStack.getItem()))) {
            player.sendMessage(localized("text.itsours.action.disallowed.interact_item_on_block"), true);
            return ActionResult.FAIL;
        }
        return original.call(itemStack, context);
    }

}
