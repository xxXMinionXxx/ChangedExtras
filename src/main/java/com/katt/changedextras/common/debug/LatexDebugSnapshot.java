package com.katt.changedextras.common.debug;

import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;
import java.util.List;

public record LatexDebugSnapshot(
        int entityId,
        @Nullable BlockPos targetPos,
        @Nullable BlockPos lastSeenPos,
        @Nullable BlockPos buildPos,
        @Nullable BlockPos breakPos,
        boolean requiresBreak,
        List<BlockPos> imaginedBuildPath,
        List<BlockPos> pathNodes
) {
    public LatexDebugSnapshot {
        imaginedBuildPath = List.copyOf(imaginedBuildPath);
        pathNodes = List.copyOf(pathNodes);
    }
}
