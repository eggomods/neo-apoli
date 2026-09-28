package io.github.eggohito.neo_apoli.registry.context;

import com.mojang.serialization.Codec;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.parameter.*;
import io.github.eggohito.neo_apoli.provider.ValueProvider;
import io.github.eggohito.neo_apoli.provider.custom.block.BlockProvider;
import io.github.eggohito.neo_apoli.provider.custom.direction.DirectionProvider;
import io.github.eggohito.neo_apoli.provider.custom.effect.EffectProvider;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.provider.custom.item.ItemProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;
import io.github.eggohito.neo_apoli.provider.custom.slot.SlotProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public final class NeoApoliContextParameterTypes {

	public static final Context.Parameter.Type<BlockContextParameter, BlockProvider> BLOCK = registerInternal("block", BlockProvider.CODEC, BlockContextParameter::new);
	public static final Context.Parameter.Type<DirectionContextParameter, DirectionProvider> DIRECTION = registerInternal("direction", DirectionProvider.CODEC, DirectionContextParameter::new);
	public static final Context.Parameter.Type<EffectContextParameter, EffectProvider> EFFECT = registerInternal("effect", EffectProvider.CODEC, EffectContextParameter::new);
	public static final Context.Parameter.Type<EntityContextParameter, EntityProvider> ENTITY = registerInternal("entity", EntityProvider.CODEC, EntityContextParameter::new);
	public static final Context.Parameter.Type<FloatContextParameter, FloatProvider> FLOAT = registerInternal("float", FloatProvider.CODEC, FloatContextParameter::new);
	public static final Context.Parameter.Type<IntContextParameter, IntProvider> INT = registerInternal("int", IntProvider.CODEC, IntContextParameter::new);
	public static final Context.Parameter.Type<ItemContextParameter, ItemProvider> ITEM = registerInternal("item", ItemProvider.CODEC, ItemContextParameter::new);
	public static final Context.Parameter.Type<SlotContextParameter, SlotProvider> SLOT = registerInternal("slot", SlotProvider.CODEC, SlotContextParameter::new);

	public static void registerAll() {

	}

	public static <V, Parameter extends Context.Parameter<V>, Provider extends ValueProvider<V>> Context.Parameter.Type<Parameter, Provider> register(ResourceLocation id, Codec<Provider> providerCodec, Function<ResourceLocation, Parameter> factory) {
		return Registry.register(NeoApoliRegistries.CONTEXT_PARAMETER_TYPE, id, new Context.Parameter.Type<>(providerCodec, factory));
	}

	private static <V, Parameter extends Context.Parameter<V>, Provider extends ValueProvider<V>> Context.Parameter.Type<Parameter, Provider> registerInternal(String path, Codec<Provider> providerCodec, Function<ResourceLocation, Parameter> factory) {
		return register(NeoApoli.id(path), providerCodec, factory);
	}

}
