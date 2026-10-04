package io.github.eggohito.neo_apoli.registry.context;

import com.mojang.serialization.Codec;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.context.parameter.*;
import io.github.eggohito.neo_apoli.provider.ValueProvider;
import io.github.eggohito.neo_apoli.provider.custom.block.BlockProvider;
import io.github.eggohito.neo_apoli.provider.custom.bool.BooleanProvider;
import io.github.eggohito.neo_apoli.provider.custom.box.BoxProvider;
import io.github.eggohito.neo_apoli.provider.custom.command_source.CommandSourceProvider;
import io.github.eggohito.neo_apoli.provider.custom.damage_source.DamageSourceProvider;
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

	public static final ContextParameter.TypeWithProvider<BlockContextParameter, BlockProvider> BLOCK = registerWithProviderInternal("block", BlockProvider.CODEC, BlockProvider.STREAM_CODEC, BlockContextParameter::new);
	public static final ContextParameter.TypeWithProvider<BooleanContextParameter, BooleanProvider> BOOLEAN = registerWithProviderInternal("boolean", BooleanProvider.CODEC, BooleanProvider.STREAM_CODEC, BooleanContextParameter::new);
	public static final ContextParameter.TypeWithProvider<BoxContextParameter, BoxProvider> BOX = registerWithProviderInternal("box", BoxProvider.CODEC, BoxProvider.STREAM_CODEC, BoxContextParameter::new);
	public static final ContextParameter.TypeWithProvider<CommandSourceContextParameter, CommandSourceProvider> COMMAND_SOURCE = registerWithProviderInternal("command_source", CommandSourceProvider.CODEC, CommandSourceProvider.STREAM_CODEC, CommandSourceContextParameter::new);
	public static final ContextParameter.Type<DamageSourceContextParameter> DAMAGE_SOURCE = registerWithProviderInternal("damage_source", DamageSourceProvider.CODEC, DamageSourceProvider.STREAM_CODEC, DamageSourceContextParameter::new);
	public static final ContextParameter.TypeWithProvider<DirectionContextParameter, DirectionProvider> DIRECTION = registerWithProviderInternal("direction", DirectionProvider.CODEC, DirectionProvider.STREAM_CODEC, DirectionContextParameter::new);
	public static final ContextParameter.TypeWithProvider<EffectContextParameter, EffectProvider> EFFECT = registerWithProviderInternal("effect", EffectProvider.CODEC, EffectProvider.STREAM_CODEC, EffectContextParameter::new);
	public static final ContextParameter.TypeWithProvider<EntityContextParameter, EntityProvider> ENTITY = registerWithProviderInternal("entity", EntityProvider.CODEC, EntityProvider.STREAM_CODEC, EntityContextParameter::new);
	public static final ContextParameter.TypeWithProvider<FloatContextParameter, FloatProvider> FLOAT = registerWithProviderInternal("float", FloatProvider.CODEC, FloatProvider.STREAM_CODEC, FloatContextParameter::new);
	public static final ContextParameter.TypeWithProvider<IntContextParameter, IntProvider> INT = registerWithProviderInternal("int", IntProvider.CODEC, IntProvider.STREAM_CODEC, IntContextParameter::new);
	public static final ContextParameter.TypeWithProvider<ItemContextParameter, ItemProvider> ITEM = registerWithProviderInternal("item", ItemProvider.CODEC, ItemProvider.STREAM_CODEC, ItemContextParameter::new);
	public static final ContextParameter.TypeWithProvider<NbtContextParameter, NbtProvider> NBT = registerWithProviderInternal("nbt", NbtProvider.CODEC, NbtProvider.STREAM_CODEC, NbtContextParameter::new);
	public static final ContextParameter.TypeWithProvider<SlotContextParameter, SlotProvider> SLOT = registerWithProviderInternal("slot", SlotProvider.CODEC, SlotProvider.STREAM_CODEC, SlotContextParameter::new);
	public static final ContextParameter.TypeWithProvider<StringContextParameter, StringProvider> STRING = registerWithProviderInternal("string", StringProvider.CODEC, StringProvider.STREAM_CODEC, StringContextParameter::new);
	public static final ContextParameter.TypeWithProvider<Vec3ContextParameter, Vec3Provider> VEC3 = registerWithProviderInternal("vec3", Vec3Provider.CODEC, Vec3Provider.STREAM_CODEC, Vec3ContextParameter::new);

	public static void registerAll() {

	}

	public static <V, Parameter extends ContextParameter<V>, Provider extends ValueProvider<V>> ContextParameter.TypeWithProvider<Parameter, Provider> registerWithProvider(ResourceLocation id, Codec<Provider> providerCodec, StreamCodec<RegistryFriendlyByteBuf, Provider> providerStreamCodec, Function<ResourceLocation, Parameter> factory) {
		return Registry.register(NeoApoliRegistries.CONTEXT_PARAMETER_TYPE, id, new ContextParameter.TypeWithProvider<>(providerCodec, providerStreamCodec, factory));
	}

	public static <V, Parameter extends ContextParameter<V>> ContextParameter.Type<Parameter> registerSimple(ResourceLocation id, Function<ResourceLocation, Parameter> factory) {
		return Registry.register(NeoApoliRegistries.CONTEXT_PARAMETER_TYPE, id, new ContextParameter.SimpleType<>(factory));
	}

	private static <V, Parameter extends ContextParameter<V>, Provider extends ValueProvider<V>> ContextParameter.TypeWithProvider<Parameter, Provider> registerWithProviderInternal(String path, Codec<Provider> providerCodec, StreamCodec<RegistryFriendlyByteBuf, Provider> providerStreamCodec, Function<ResourceLocation, Parameter> factory) {
		return registerWithProvider(NeoApoli.id(path), providerCodec, providerStreamCodec, factory);
	}

	private static <V, Parameter extends ContextParameter<V>> ContextParameter.Type<Parameter> registerSimpleInternal(String path, Function<ResourceLocation, Parameter> factory) {
		return registerSimple(NeoApoli.id(path), factory);
	}

}
