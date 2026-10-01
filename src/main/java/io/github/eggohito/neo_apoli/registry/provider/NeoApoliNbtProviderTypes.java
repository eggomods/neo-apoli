package io.github.eggohito.neo_apoli.registry.provider;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.provider.custom.nbt.*;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistries;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public final class NeoApoliNbtProviderTypes {

	public static final NbtProvider.Type<CompositeConditionalNbtProvider> COMPOSITE_CONDITIONAL = registerInternal("conditional/composite", CompositeConditionalNbtProvider.MAP_CODEC, CompositeConditionalNbtProvider.STREAM_CODEC);
	public static final NbtProvider.Type<ConditionalNbtProvider> CONDITIONAL = registerInternal("conditional", ConditionalNbtProvider.MAP_CODEC, ConditionalNbtProvider.STREAM_CODEC);
	public static final NbtProvider.Type<ConstantNbtProvider> CONSTANT = registerInternal("constant", ConstantNbtProvider.MAP_CODEC, ConstantNbtProvider.STREAM_CODEC);
	public static final NbtProvider.Type<ContextNbtProvider> CONTEXT = registerInternal("context", ContextNbtProvider.CODEC, ContextNbtProvider.STREAM_CODEC);

	public static final NbtProvider.Type<BlockNbtProvider> BLOCK = registerInternal("block", BlockNbtProvider.MAP_CODEC, BlockNbtProvider.STREAM_CODEC);
	public static final NbtProvider.Type<EntityNbtProvider> ENTITY = registerInternal("entity", EntityNbtProvider.MAP_CODEC, EntityNbtProvider.STREAM_CODEC);
	public static final NbtProvider.Type<ItemNbtProvider> ITEM = registerInternal("item", ItemNbtProvider.MAP_CODEC, ItemNbtProvider.STREAM_CODEC);
	public static final NbtProvider.Type<PowerNbtProvider> POWER = registerInternal("power", PowerNbtProvider.MAP_CODEC, PowerNbtProvider.STREAM_CODEC);
	public static final NbtProvider.Type<StorageNbtProvider> STORAGE = registerInternal("storage", StorageNbtProvider.MAP_CODEC, StorageNbtProvider.STREAM_CODEC);

	public static void registerAll() {

	}

	private static <P extends NbtProvider> NbtProvider.Type<P> registerInternal(String path, MapCodec<P> mapCodec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) {
		return register(NeoApoli.id(path), mapCodec, streamCodec);
	}

	public static <P extends NbtProvider> NbtProvider.Type<P> register(ResourceLocation id, MapCodec<P> mapCodec, StreamCodec<RegistryFriendlyByteBuf, P> streamCodec) {
		return Registry.register(NeoApoliRegistries.NBT_PROVIDER_TYPE, id, new NbtProvider.Type<>(mapCodec, streamCodec));
	}

}
