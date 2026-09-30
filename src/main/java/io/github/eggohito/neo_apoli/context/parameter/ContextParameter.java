package io.github.eggohito.neo_apoli.context.parameter;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.*;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.provider.ValueProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistries;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistryKeys;
import io.github.eggohito.neo_apoli.util.ResourceLocationUtil;
import io.github.eggohito.neo_apoli.util.alias.FixedRegistryAlias;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public interface ContextParameter<T> {

	@SuppressWarnings("unchecked")
	Codec<Map<ContextParameter<?>, ValueProvider<?>>> VALUE_MAP_CODEC = new Codec<>() {

		private final static Codec<ResourceLocation> ID_CODEC = ResourceLocationUtil.codecWithDefaultNamespace(NeoApoli.MOD_NAMESPACE);
		private final static MapCodec<Type<?, ?>> TYPE_CODEC = Type.CODEC.fieldOf("type");

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
				(identity, keyAndValue) -> {

					DataResult<String> keyResult = Codec.STRING.parse(ops, keyAndValue.getFirst());
					DataResult<MapLike<I>> valueResult = ops.getMap(keyAndValue.getSecond());

					if (keyResult.isError()) {
						return identity.apply2stable((unit, o) -> unit, keyResult);
					}

					if (valueResult.isError()) {
						return identity.apply2stable((unit, o) -> unit, valueResult);
					}

					String key = keyResult.getOrThrow();
					MapLike<I> valueMap = valueResult.getOrThrow();

					DataResult<ResourceLocation> idResult = ID_CODEC
						.parse(ops, keyAndValue.getFirst())
						.map(id -> Context.ALIASES.resolve(id, Predicate.not(id::equals)));

					if (idResult.isError()) {
						return identity.apply2stable((unit, ignored) -> unit, idResult);
					}

					ResourceLocation id = idResult.getOrThrow();
					DataResult<Type<?, ?>> typeResult = TYPE_CODEC.decode(ops, valueMap);

					if (typeResult.isError()) {
						return identity.apply2stable((unit, ignored) -> unit, typeResult);
					}

					Type<?, ValueProvider<?>> type = (Type<?, ValueProvider<?>>) typeResult.getOrThrow();
					DataResult<ValueProvider<?>> providerResult = type.providerCodec().parse(ops, valueMap.get("value"));

					if (providerResult.isSuccess() && succeeded.putIfAbsent(type.create(id), providerResult.getOrThrow()) != null) {
						return identity.apply2stable((unit, o) -> unit, DataResult.error(() -> "Duplicate key: \"" + key + "\""));
					}

					return identity.apply2stable((unit, ignored) -> unit, providerResult);

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

				Type<?, ValueProvider<?>> parameterType = (Type<?, ValueProvider<?>>) parameter.getType();

				if (parameterType != null) {

					RecordBuilder<O> mapBuilder = TYPE_CODEC
						.encode(parameterType, ops, ops.mapBuilder()
							.add("value", parameterType.providerCodec().encodeStart(ops, provider)));

					prefix.add(entry.getKey().name().toString(), mapBuilder.build(ops.empty()));

				}

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

				Type<?, ?> type = Type.STREAM_CODEC.decode(buf);

				ContextParameter<?> parameter = type.create(buf.readResourceLocation());
				ValueProvider<?> provider = type.providerStreamCodec().decode(buf);

				map.put(parameter, provider);

			}

			return map;

		}

		@Override
		public void encode(RegistryFriendlyByteBuf buf, Map<ContextParameter<?>, ValueProvider<?>> map) {

			Map<ContextParameter<?>, ValueProvider<?>> filtered = new Object2ObjectLinkedOpenHashMap<>();

			for (var entry : map.entrySet()) {

				ContextParameter<?> parameter = entry.getKey();

				if (parameter.getType() != null) {
					filtered.put(parameter, entry.getValue());
				}

			}

			buf.writeInt(filtered.size());

			for (var entry : filtered.entrySet()) {

				ContextParameter<?> parameter = entry.getKey();
				ValueProvider<?> provider = entry.getValue();

				Type<?, ValueProvider<?>> type = (Type<?, ValueProvider<?>>) Objects.requireNonNull(parameter.getType(), "Unfiltered parameter \"" + parameter.name() + "\" without a type got through! This is not supposed to happen!");
				Type.STREAM_CODEC.encode(buf, parameter.getType());

				buf.writeResourceLocation(parameter.name());
				type.providerStreamCodec().encode(buf, provider);

			}

		}

	};

	ResourceLocation name();

	@Nullable
	Type<?, ?> getType();

	static <T, P extends ContextParameter<T>> Codec<P> codec(Function<ResourceLocation, P> constructor) {
		return ResourceLocation.CODEC.xmap(constructor, ContextParameter::name);
	}

	static <T, P extends ContextParameter<T>> Codec<P> codec(String defaultNamespace, Function<ResourceLocation, P> constructor) {
		return ResourceLocationUtil.codecWithDefaultNamespace(defaultNamespace)
			.xmap(id -> Context.ALIASES.resolve(id, Predicate.not(id::equals)), Function.identity())
			.xmap(constructor, ContextParameter::name);
	}

	static <T, P extends ContextParameter<T>> StreamCodec<ByteBuf, P> streamCodec(Function<ResourceLocation, P> constructor) {
		return ResourceLocation.STREAM_CODEC.map(constructor, ContextParameter::name);
	}

	record Type<Parameter extends ContextParameter<?>, Provider extends ValueProvider<?>>(Codec<Provider> providerCodec, StreamCodec<RegistryFriendlyByteBuf, Provider> providerStreamCodec, Function<ResourceLocation, Parameter> factory) {

		public static final FixedRegistryAlias<Type<?, ?>> ALIASES = FixedRegistryAlias.of(NeoApoliRegistries.CONTEXT_PARAMETER_TYPE);

		public static final Codec<Type<?, ?>> CODEC = ALIASES.createCodec(NeoApoli.MOD_NAMESPACE);

		public static final StreamCodec<RegistryFriendlyByteBuf, Type<?, ?>> STREAM_CODEC = ByteBufCodecs.registry(NeoApoliRegistryKeys.CONTEXT_PARAMETER_TYPE);

		public Parameter create(ResourceLocation name) {
			return factory().apply(name);
		}

	}

}
