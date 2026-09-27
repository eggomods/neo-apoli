package io.github.eggohito.neo_apoli.provider.custom.nbt;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.duck.internal.CommandStorageHolder;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliNbtProviderTypes;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record StorageNbtProvider(ResourceLocation id) implements NbtProvider {

	public static final MapCodec<StorageNbtProvider> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(ResourceLocation.CODEC.fieldOf("id").forGetter(StorageNbtProvider::id))
		.apply(instance, StorageNbtProvider::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, StorageNbtProvider> STREAM_CODEC = StreamCodec.composite(
		ResourceLocation.STREAM_CODEC, StorageNbtProvider::id,
		StorageNbtProvider::new
	);

	@Override
	public @NotNull NbtProvider.Type<?> getType() {
		return NeoApoliNbtProviderTypes.STORAGE;
	}

	@Override
	public Optional<Tag> getValue(Context context) {
		CommandStorageHolder holder = (CommandStorageHolder) context.level();
		return holder.neo_apoli$contains(this.id())
			? Optional.of(holder.neo_apoli$getStorage(this.id()))
			: Optional.empty();
	}

}
