package me.drex.itsours.user;

import me.drex.itsours.claim.Claim;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.List;

public interface ClaimTrackingPlayer {

    void addChunkBatch(List<WorldChunk> chunkBatch);

    void batchAcknowledged();

    void onChunkUnload(ChunkPos pos);

    Claim trackedClaim();

    boolean isTracked(BlockPos pos);

    void trackClaim(Claim claim);

    void unTrackClaim();

}