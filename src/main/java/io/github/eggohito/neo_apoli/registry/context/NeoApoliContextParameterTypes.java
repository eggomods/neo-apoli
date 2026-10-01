package io.github.eggohito.neo_apoli.registry.context;

import com.mojang.serialization.Codec;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.context.parameter.*;
import io.github.eggohito.neo_apoli.provider.ValueProvider;
import io.github.eggohito.neo_apoli.provider.custom.block.BlockProvider;
import io.github.eggohito.neo_apoli.provider.custom.bool.BooleanProvider;
import io.github.eggohito.neo_apoli.provider.custom.box.BoxProvider;
import io.github.eggohito.neo_apoli.provider.custom.command_source.CommandSourceProvider;
import io.github.eggohito.neo_apoli.provider.custom.direction.DirectionProvider;
import io.github.eggohito.neo_apoli.provider.custom.effect.EffectProvider;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.provider.custom.item.ItemProvider;
import io.github.eggohito.neo_apoli.provider.custom.nbt.NbtProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;
import io.github.eggohito.neo_apoli.provider.custom.slot.SlotProvider;
import io.github.eggohito.neo_apoli.provider.custom.string.StringProvider;
import io.github.eggohito.neo_apoli.provider.custom.vec3.Vec3Provider;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistries;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public final class NeoApoliContextParameterTypes {

	public static final ContextParameter.Type<BlockContextParameter, BlockProvider> BLOCK = registerInternal("block", BlockProvider.CODEC, BlockProvider.STREAM_CODEC, BlockContextParameter::new);
	public static final ContextParameter.Type<BooleanContextParameter, BooleanProvider> BOOLEAN = registerInternal("boolean", BooleanProvider.CODEC, BooleanProvider.STREAM_CODEC, BooleanContextParameter::new);
	public static final ContextParameter.Type<BoxContextParameter, BoxProvider> BOX = registerInternal("box", BoxProvider.CODEC, BoxProvider.STREAM_CODEC, BoxContextParameter::new);
	public static final ContextParameter.Type<CommandSourceContextParameter, CommandSourceProvider> COMMAND_SOURCE = registerInternal("command_source", CommandSourceProvider.CODEC, CommandSourceProvider.STREAM_CODEC, CommandSourceContextParameter::new);
	public static final ContextParameter.Type<DirectionContextParameter, DirectionProvider> DIRECTION = registerInternal("direction", DirectionProvider.CODEC, DirectionProvider.STREAM_CODEC, DirectionContextParameter::new);
	public static final ContextParameter.Type<EffectContextParameter, EffectProvider> EFFECT = registerInternal("effect", EffectProvider.CODEC, EffectProvider.STREAM_CODEC, EffectContextParameter::new);
	public static final ContextParameter.Type<EntityContextParameter, EntityProvider> ENTITY = registerInternal("entity", EntityProvider.CODEC, EntityProvider.STREAM_CODEC, EntityContextParameter::new);
	public static final ContextParameter.Type<FloatContextParameter, FloatProvider> FLOAT = registerInternal("float", FloatProvider.CODEC, FloatProvider.STREAM_CODEC, FloatContextParameter::new);
	public static final ContextParameter.Type<IntContextParameter, IntProvider> INT = registerInternal("int", IntProvider.CODEC, IntProvider.STREAM_CODEC, IntContextParameter::new);
	public static final ContextParameter.Type<ItemContextParameter, ItemProvider> ITEM = registerInternal("item", ItemProvider.CODEC, ItemProvider.STREAM_CODEC, ItemContextParameter::new);
	public static final ContextParameter.Type<NbtContextParameter, NbtProvider> NBT = registerInternal("nbt", NbtProvider.CODEC, NbtProvider.STREAM_CODEC, NbtContextParameter::new);
	public static final ContextParameter.Type<SlotContextParameter, SlotProvider> SLOT = registerInternal("slot", SlotProvider.CODEC, SlotProvider.STREAM_CODEC, SlotContextParameter::new);
	public static final ContextParameter.Type<StringContextParameter, StringProvider> STRING = registerInternal("string", StringProvider.CODEC, StringProvider.STREAM_CODEC, StringContextParameter::new);
	public static final ContextParameter.Type<Vec3ContextParameter, Vec3Provider> VEC3 = registerInternal("vec3", Vec3Provider.CODEC, Vec3Provider.STREAM_CODEC, Vec3ContextParameter::new);

	public static void registerAll() {

	}

	public static <V, Parameter extends ContextParameter<V>, Provider extends ValueProvider<V>> ContextParameter.Type<Parameter, Provider> register(ResourceLocation id, Codec<Provider> providerCodec, StreamCodec<RegistryFriendlyByteBuf, Provider> providerStreamCodec, Function<ResourceLocation, Parameter> factory) {
		return Registry.register(NeoApoliRegistries.CONTEXT_PARAMETER_TYPE, id, new ContextParameter.Type<>(providerCodec, providerStreamCodec, factory));
	}

	private static <V, Parameter extends ContextParameter<V>, Provider extends ValueProvider<V>> ContextParameter.Type<Parameter, Provider> registerInternal(String path, Codec<Provider> providerCodec, StreamCodec<RegistryFriendlyByteBuf, Provider> providerStreamCodec, Function<ResourceLocation, Parameter> factory) {
		return register(NeoApoli.id(path), providerCodec, providerStreamCodec, factory);
	}

}
