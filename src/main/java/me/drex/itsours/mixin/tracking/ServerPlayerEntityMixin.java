package me.drex.itsours.mixin.tracking;

import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.Claim;
import me.drex.itsours.claim.Subzone;
import me.drex.itsours.user.ClaimTrackingPlayer;
import me.drex.itsours.util.ClaimBox;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.ChunkFilter;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.*;
import java.util.function.Predicate;

import static me.drex.itsours.claim.AbstractClaim.SHOW_BLOCKS;
import static me.drex.itsours.claim.AbstractClaim.SHOW_BLOCKS_CENTER;
import static net.minecraft.world.Heightmap.Type.OCEAN_FLOOR;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements ClaimTrackingPlayer {

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    private static final Predicate<BlockState> BLOCKS_MOVEMENT = AbstractBlock.AbstractBlockState::blocksMovement;

    @Shadow
    public ServerPlayNetworkHandler networkHandler;

    @Shadow
    public abstract ChunkFilter getChunkFilter();

    private final Long2ObjectMap<Set<BlockPos>> chunk2TrackedShowBlocks = new Long2ObjectArrayMap<>();
    private final Set<BlockPos> trackedShowBlocks = new HashSet<>();

    private final Deque<List<WorldChunk>> chunkBatches = new LinkedList<>();

    @Nullable
    private Claim trackedClaim = null;

    @Override
    public void addChunkBatch(List<WorldChunk> chunkBatch) {
        chunkBatches.addLast(chunkBatch);
    }

    @Override
    public void batchAcknowledged() {
        for (WorldChunk chunk : chunkBatches.pop()) {
            if (trackedClaim == null || !trackedClaim.getDimension().equals(getWorld().getRegistryKey())) continue;
            showChunk(trackedClaim, chunk.getPos());
        }
    }

    private void showChunk(AbstractClaim claim, ChunkPos pos) {
        BlockState blockState = SHOW_BLOCKS[Math.min(claim.getDepth(), SHOW_BLOCKS.length - 1)].getDefaultState();
        BlockState centerBlockState = SHOW_BLOCKS_CENTER[Math.min(claim.getDepth(), SHOW_BLOCKS_CENTER.length - 1)].getDefaultState();

        ClaimBox box = claim.getBox();
        for (int x = Math.max(box.getMinX(), pos.getStartX()); x < Math.min(box.getMinX() + box.getBlockCountX(), pos.getEndX() + 1); x++) {
            boolean lineCenter = box.isCenterBlock(box.getBlockCountX(), x - box.getMinX());
            BlockState state = lineCenter ? centerBlockState : blockState;

            sendFakeBlockIfInChunk(x, box.getMinZ(), pos, state);
            sendFakeBlockIfInChunk(x, box.getMaxZ(), pos, state);
        }
        for (int z = Math.max(box.getMinZ(), pos.getStartZ()); z < Math.min(box.getMinZ() + box.getBlockCountZ(), pos.getEndZ() + 1); z++) {
            boolean lineCenter = box.isCenterBlock(box.getBlockCountZ(), z - box.getMinZ());
            BlockState state = lineCenter ? centerBlockState : blockState;

            sendFakeBlockIfInChunk(box.getMinX(), z, pos, state);
            sendFakeBlockIfInChunk(box.getMaxX(), z, pos, state);
        }

        for (Subzone subzone : claim.getSubzones()) {
            showChunk(subzone, pos);
        }
    }

    private void sendFakeBlockIfInChunk(int x, int z, ChunkPos pos, BlockState blockState) {
        if (x >= pos.getStartX() && x <= pos.getEndX() && z >= pos.getStartZ() && z <= pos.getEndZ()) {
            sendFakeBlock(x, z, blockState);
        }
    }

    @Override
    public Claim trackedClaim() {
        return trackedClaim;
    }

    @Override
    public boolean isTracked(BlockPos pos) {
        return trackedShowBlocks.contains(pos);
    }

    @Override
    public void onChunkUnload(ChunkPos pos) {
        Set<BlockPos> removedBlocks = chunk2TrackedShowBlocks.remove(pos.toLong());
        if (removedBlocks != null) {
            trackedShowBlocks.removeAll(removedBlocks);
        }
    }

    @Override
    public void trackClaim(@NotNull Claim claim) {
        if (trackedClaim != null) {
            unTrackClaim();
        }
        trackedClaim = claim;
        if (trackedClaim.getDimension().equals(getWorld().getRegistryKey())) {
            showClaimInternal(claim);
        }
    }

    private void showClaimInternal(AbstractClaim claim) {
        BlockState blockState = SHOW_BLOCKS[Math.min(claim.getDepth(), SHOW_BLOCKS.length - 1)].getDefaultState();
        BlockState centerBlockState = SHOW_BLOCKS_CENTER[Math.min(claim.getDepth(), SHOW_BLOCKS_CENTER.length - 1)].getDefaultState();

        ClaimBox box = claim.getBox();
        for (int x = 0; x < box.getBlockCountX(); x++) {
            boolean lineCenter = box.isCenterBlock(box.getBlockCountX(), x);
            BlockState state = lineCenter ? centerBlockState : blockState;

            sendFakeBlockIfTracked(x + box.getMinX(), box.getMinZ(), state);
            sendFakeBlockIfTracked(x + box.getMinX(), box.getMaxZ(), state);
        }
        for (int z = 0; z < box.getBlockCountZ(); z++) {
            boolean lineCenter = box.isCenterBlock(box.getBlockCountZ(), z);
            BlockState state = lineCenter ? centerBlockState : blockState;

            sendFakeBlockIfTracked(box.getMinX(), z + box.getMinZ(), state);
            sendFakeBlockIfTracked(box.getMaxX(), z + box.getMinZ(), state);
        }

        for (Subzone subzone : claim.getSubzones()) {
            showClaimInternal(subzone);
        }
    }

    private void sendFakeBlockIfTracked(int x, int z, BlockState blockState) {
        if (getChunkFilter().isWithinDistance(ChunkSectionPos.getSectionCoord(x), ChunkSectionPos.getSectionCoord(z))) {
            sendFakeBlock(x, z, blockState);
        }
    }

    @Override
    public void unTrackClaim() {
        trackedClaim = null;
        for (BlockPos blockPos : trackedShowBlocks) {
            networkHandler.sendPacket(new BlockUpdateS2CPacket(getWorld(), blockPos));
        }
        chunk2TrackedShowBlocks.clear();
        trackedShowBlocks.clear();
    }

    private void sendFakeBlock(int x, int z, BlockState state) {
        World world = getWorld();
        int y = world.getTopY(OCEAN_FLOOR, x, z);
        int playerY = (int) getY();
        BlockPos.Mutable pos = new BlockPos.Mutable(x, y - 1, z);
        // Special handling for cave-like scenarios
        if (y - playerY > 16) {
            pos.setY(playerY + 16);
            if (BLOCKS_MOVEMENT.test(world.getBlockState(pos))) {
                while (BLOCKS_MOVEMENT.test(world.getBlockState(pos)) && pos.getY() > world.getBottomY()) {
                    pos.move(0, -1, 0);
                }
            }
            while (!BLOCKS_MOVEMENT.test(world.getBlockState(pos)) && pos.getY() > world.getBottomY()) {
                pos.move(0, -1, 0);
            }
        }

        BlockUpdateS2CPacket packet = new BlockUpdateS2CPacket(pos, state);
        networkHandler.sendPacket(packet);
        long chunkPos = ChunkPos.toLong(ChunkSectionPos.getSectionCoord(x), ChunkSectionPos.getSectionCoord(z));
        chunk2TrackedShowBlocks.computeIfAbsent(chunkPos, l -> new HashSet<>());
        chunk2TrackedShowBlocks.get(chunkPos).add(pos);
        trackedShowBlocks.add(pos);
    }

}
