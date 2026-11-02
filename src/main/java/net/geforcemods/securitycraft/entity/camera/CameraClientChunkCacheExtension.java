package net.geforcemods.securitycraft.entity.camera;

import java.util.function.Consumer;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

public class CameraClientChunkCacheExtension {
	private static final Long2ObjectOpenHashMap<LevelChunk> CHUNK_MAP = new Long2ObjectOpenHashMap<>();
	private static final Long2ObjectOpenHashMap<LevelChunk> CHUNK_MAP_OTHER_THREADS = new Long2ObjectOpenHashMap<>();
	private static final boolean IS_SODIUM_LOADED = FabricLoader.getInstance().isModLoaded("sodium");

	private CameraClientChunkCacheExtension() {}

	public static void drop(ClientLevel level, ChunkPos chunkPos) {
		if (Minecraft.getInstance().isSameThread()) {
			long chunkPosLong = chunkPos.pack();
			LevelChunk chunk = CHUNK_MAP.get(chunkPosLong);

			if (chunk != null) {
				modifyChunkMaps(map -> map.remove(chunkPosLong));
				level.unload(chunk);
			}
		}
	}

	public static LevelChunk getChunk(int x, int z) {
		long chunkPos = ChunkPos.pack(x, z);

		if (Minecraft.getInstance().isSameThread())
			return CHUNK_MAP.get(chunkPos);
		else {
			synchronized (CHUNK_MAP_OTHER_THREADS) {
				return CHUNK_MAP_OTHER_THREADS.get(chunkPos);
			}
		}
	}

	public static LevelChunk replaceWithPacketData(ClientLevel level, int x, int z, ClientboundLevelChunkPacketData data) {
		if (!Minecraft.getInstance().isSameThread())
			throw new UnsupportedOperationException("replaceWithPacketData called off-thread, this shouldn't happen!");

		ChunkPos chunkPos = new ChunkPos(x, z);
		long longChunkPos = chunkPos.pack();
		LevelChunk chunk = CHUNK_MAP.get(longChunkPos);

		if (chunk == null) {
			LevelChunk newChunk = new LevelChunk(level, chunkPos);

			chunk = newChunk;
			chunk.replaceWithPacketData(x, z, data);
			modifyChunkMaps(map -> map.put(longChunkPos, newChunk));
		}

		level.onChunkLoaded(chunkPos);
		return chunk;
	}

	private static void modifyChunkMaps(Consumer<Long2ObjectOpenHashMap<LevelChunk>> operation) {
		operation.accept(CHUNK_MAP);

		synchronized (CHUNK_MAP_OTHER_THREADS) {
			operation.accept(CHUNK_MAP_OTHER_THREADS);
		}
	}

	public static void clear() {
		CHUNK_MAP.clear();
		CHUNK_MAP_OTHER_THREADS.clear();
	}
}
