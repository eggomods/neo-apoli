package io.github.eggohito.neo_apoli.context.parameter;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.*;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.provider.ValueProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistries;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistryKeys;
import io.github.eggohito.neo_apoli.util.ResourceLocationUtil;
import io.github.eggohito.neo_apoli.util.alias.FixedRegistryAlias;
import io.github.eggohito.neo_apoli.util.alias.ResourceLocationAlias;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import lombok.Getter;
import net.minecraft.Util;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public interface ContextParameter<T> {

	@SuppressWarnings("unchecked")
	Codec<Map<ContextParameter<?>, ValueProvider<?>>> VALUE_MAP_CODEC = new Codec<>() {

		private final static MapCodec<Type<?>> TYPE_CODEC = Type.CODEC.fieldOf("type");

		@Override
		public <I> DataResult<Pair<Map<ContextParameter<?>, ValueProvider<?>>, I>> decode(DynamicOps<I> ops, I input) {
			return ops.getMap(input).flatMap(map -> this.decodeMap(ops, map)).map(r -> Pair.of(r, input));
		}

		@Override
		public <O> DataResult<O> encode(Map<ContextParameter<?>, ValueProvider<?>> input, DynamicOps<O> ops, O prefix) {
			return this.encodeMap(input, ops, ops.mapBuilder()).build(prefix);
		}

		private <I> DataResult<Map<ContextParameter<?>, ValueProvider<?>>> decodeMap(DynamicOps<I> ops, MapLike<I> input) {

			Map<ContextParameter<?>, ValueProvider<?>> succeeded = new Object2ObjectLinkedOpenHashMap<>();
			DataResult<Unit> result = input.entries().reduce(
				DataResult.success(Unit.INSTANCE, Lifecycle.stable()),
				(identity, rawKeyAndValue) -> {

					I rawKey = rawKeyAndValue.getFirst();
					DataResult<String> keyResult = Codec.STRING.parse(ops, rawKey);

					if (keyResult.isError()) {
						return identity.apply2stable((unit, o) -> unit, keyResult);
					}

					String key = keyResult.getOrThrow();
					DataResult<MapLike<I>> valueResult = ops.getMap(rawKeyAndValue.getSecond());

					if (valueResult.isError()) {
						return identity.apply2stable((unit, o) -> unit, valueResult);
					}

					MapLike<I> valueMap = valueResult.getOrThrow();
					DataResult<Type<?>> typeResult = TYPE_CODEC.decode(ops, valueMap);

					if (typeResult.isError()) {
						return identity.apply2stable((unit, o) -> unit, typeResult);
					}

					Type<?> type = typeResult.getOrThrow();

					if (!(type instanceof ContextParameter.TypeWithProvider<?, ?> typeWithProvider)) {
						return identity.apply2stable((unit, o) -> unit, DataResult.error(() -> "Parameter type \"" + Util.getRegisteredName(NeoApoliRegistries.CONTEXT_PARAMETER_TYPE, type) + "\" doesn't have a corresponding value provider!"));
					}

					TypeWithProvider<ContextParameter<?>, ValueProvider<?>> castedTypeWithProvider = (TypeWithProvider<ContextParameter<?>, ValueProvider<?>>) typeWithProvider;
					DataResult<ValueProvider<?>> providerResult = castedTypeWithProvider.providerCodec().parse(ops, valueMap.get("value"));

					if (providerResult.isError()) {
						return identity.apply2stable((unit, o) -> unit, providerResult);
					}

					ValueProvider<?> provider = providerResult.getOrThrow();
					DataResult<ContextParameter<?>> parameterResult = castedTypeWithProvider.codec().parse(ops, rawKey);

					if (parameterResult.isSuccess() && succeeded.putIfAbsent(parameterResult.getOrThrow(), provider) != null) {
						return identity.apply2stable((unit, o) -> unit, DataResult.error(() -> "Duplicate key: \"" + key + "\""));
					}

					return identity.apply2stable((unit, o) -> unit, parameterResult);

				},
				(first, second) ->
					first.apply2stable((a, b) -> a, second)
			);

			Map<ContextParameter<?>, ValueProvider<?>> elements = ImmutableMap.copyOf(succeeded);
			return result.map(unit -> elements).setPartial(elements);

		}

		private <O> RecordBuilder<O> encodeMap(Map<ContextParameter<?>, ValueProvider<?>> input, DynamicOps<O> ops, RecordBuilder<O> prefix) {

			for (var entry : input.entrySet()) {

				ContextParameter<?> parameter = entry.getKey();
				ValueProvider<?> provider = entry.getValue();

				if (!(parameter.getType() instanceof ContextParameter.TypeWithProvider<?, ?> type)) {
					continue;
				}

				TypeWithProvider<?, ValueProvider<?>> casted = (TypeWithProvider<?, ValueProvider<?>>) type;
				RecordBuilder<O> mapBuilder = TYPE_CODEC
					.encode(type, ops, ops.mapBuilder()
						.add("value", casted.providerCodec().encodeStart(ops, provider)));

				prefix.add(entry.getKey().name().toString(), mapBuilder.build(ops.empty()));

			}

			return prefix;

		}

	};

	@SuppressWarnings("unchecked")
	StreamCodec<RegistryFriendlyByteBuf, Map<ContextParameter<?>, ValueProvider<?>>> VALUE_MAP_STREAM_CODEC = new StreamCodec<>() {

		@Override
		public @NotNull Map<ContextParameter<?>, ValueProvider<?>> decode(RegistryFriendlyByteBuf buf) {

			int size = buf.readInt();
			Map<ContextParameter<?>, ValueProvider<?>> map = new Object2ObjectLinkedOpenHashMap<>(size);

			for (int i = 0; i < size; i++) {

				Type<?> type = Type.STREAM_CODEC.decode(buf);

				if (!(type instanceof ContextParameter.TypeWithProvider<?,?> typeWithProvider)) {
					throw new IllegalStateException("Received parameter type \"" + Util.getRegisteredName(NeoApoliRegistries.CONTEXT_PARAMETER_TYPE, type) + "\", which doesn't have a corresponding value provider!");
				}

				ContextParameter<?> parameter = typeWithProvider.create(buf.readResourceLocation());
				ValueProvider<?> provider = typeWithProvider.providerStreamCodec().decode(buf);

				map.put(parameter, provider);

			}

			return map;

		}

		@Override
		public void encode(RegistryFriendlyByteBuf buf, Map<ContextParameter<?>, ValueProvider<?>> map) {

			buf.writeInt(map.size());

			for (var entry : map.entrySet()) {

				ContextParameter<?> parameter = entry.getKey();
				ValueProvider<?> provider = entry.getValue();

				if (!(parameter.getType() instanceof ContextParameter.TypeWithProvider<?, ?> typeWithProvider)) {
					throw new IllegalStateException("Couldn't send parameter \"" + parameter.name() + "\" with type \"" + Util.getRegisteredName(NeoApoliRegistries.CONTEXT_PARAMETER_TYPE, parameter.getType()) + "\", as it doesn't have a corresponding value provider!");
				}

				TypeWithProvider<?, ValueProvider<?>> casted = (TypeWithProvider<?, ValueProvider<?>>) typeWithProvider;
				Type.STREAM_CODEC.encode(buf, casted);

				buf.writeResourceLocation(parameter.name());
				casted.providerStreamCodec().encode(buf, provider);

			}

		}

	};

	ResourceLocation name();

	Type<?> getType();

	@Getter
	sealed class Type<Parameter extends ContextParameter<?>> {

		public static final FixedRegistryAlias<Type<?>> ALIASES = FixedRegistryAlias.of(NeoApoliRegistries.CONTEXT_PARAMETER_TYPE);
		public static final Codec<Type<?>> CODEC = ALIASES.createCodec(NeoApoli.MOD_NAMESPACE);
		public static final StreamCodec<RegistryFriendlyByteBuf, Type<?>> STREAM_CODEC = ByteBufCodecs.registry(NeoApoliRegistryKeys.CONTEXT_PARAMETER_TYPE);

		private final Codec<Parameter> codec;
		private final StreamCodec<ByteBuf, Parameter> streamCodec;

		private final ResourceLocationAlias aliases;
		private final Function<ResourceLocation, Parameter> factory;

		public Type(ResourceLocationAlias aliases, Function<ResourceLocation, Parameter> factory) {
			this.codec = ResourceLocationUtil.codecWithDefaultNamespace(NeoApoli.MOD_NAMESPACE).xmap(id -> aliases.resolve(id, Predicate.not(id::equals)), Function.identity()).xmap(factory, ContextParameter::name);
			this.streamCodec = ResourceLocation.STREAM_CODEC.map(factory, ContextParameter::name);
			this.aliases = aliases;
			this.factory = factory;
		}

		public Parameter create(ResourceLocation name) {
			return factory().apply(name);
		}

	}

	@Getter
	final class TypeWithProvider<Parameter extends ContextParameter<?>, Provider extends ValueProvider<?>> extends Type<Parameter> {

		private final Codec<Provider> providerCodec;
		private final StreamCodec<RegistryFriendlyByteBuf, Provider> providerStreamCodec;

		public TypeWithProvider(Codec<Provider> providerCodec, StreamCodec<RegistryFriendlyByteBuf, Provider> providerStreamCodec, ResourceLocationAlias aliases, Function<ResourceLocation, Parameter> factory) {
			super(aliases, factory);
			this.providerCodec = providerCodec;
			this.providerStreamCodec = providerStreamCodec;
		}

		public TypeWithProvider(Codec<Provider> providerCodec, StreamCodec<RegistryFriendlyByteBuf, Provider> providerStreamCodec, Function<ResourceLocation, Parameter> factory) {
			this(providerCodec, providerStreamCodec, new ResourceLocationAlias(), factory);
		}

	}

	@Getter
	final class SimpleType<Parameter extends ContextParameter<?>> extends Type<Parameter> {

		public SimpleType(ResourceLocationAlias aliases, Function<ResourceLocation, Parameter> factory) {
			super(aliases, factory);
		}

		public SimpleType(Function<ResourceLocation, Parameter> factory) {
			this(new ResourceLocationAlias(), factory);
		}

	}

}
