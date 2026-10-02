package io.github.eggohito.neo_apoli.context;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.*;
import io.github.eggohito.neo_apoli.context.parameter.ContextParameter;
import io.github.eggohito.neo_apoli.provider.ValueProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistries;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.Util;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

public record ContextParameterMap(Map<ContextParameter<?>, ValueProvider<?>> map) implements ContextValidatable {

	@SuppressWarnings("unchecked")
	private static final Codec<Map<ContextParameter<?>, ValueProvider<?>>> VALUE_MAP_CODEC = new Codec<>() {

		private final static MapCodec<ContextParameter.Type<?>> TYPE_CODEC = ContextParameter.Type.CODEC.fieldOf("type");

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
						return identity.apply2stable((unit, ignored) -> unit, keyResult);
					}

					String key = keyResult.getOrThrow();
					DataResult<MapLike<I>> valueResult = ops.getMap(rawKeyAndValue.getSecond());

					if (valueResult.isError()) {
						return identity.apply2stable((unit, ignored) -> unit, valueResult);
					}

					MapLike<I> valueMap = valueResult.getOrThrow();
					DataResult<ContextParameter.Type<?>> typeResult = TYPE_CODEC.decode(ops, valueMap);

					if (typeResult.isError()) {
						return identity.apply2stable((unit, ignored) -> unit, typeResult);
					}

					ContextParameter.Type<?> type = typeResult.getOrThrow();

					if (!(type instanceof ContextParameter.TypeWithProvider<?, ?> typeWithProvider)) {
						return identity.apply2stable((unit, ignored) -> unit, DataResult.error(() -> "Parameter \"" + key + "\" with type \"" + Util.getRegisteredName(NeoApoliRegistries.CONTEXT_PARAMETER_TYPE, type) + "\" doesn't have a corresponding value provider!"));
					}

					ContextParameter.TypeWithProvider<ContextParameter<?>, ValueProvider<?>> castedTypeWithProvider = (ContextParameter.TypeWithProvider<ContextParameter<?>, ValueProvider<?>>) typeWithProvider;
					DataResult<ValueProvider<?>> providerResult = castedTypeWithProvider.providerCodec().parse(ops, valueMap.get("value"));

					if (providerResult.isError()) {
						return identity.apply2stable((unit, ignored) -> unit, providerResult);
					}

					ValueProvider<?> provider = providerResult.getOrThrow();
					DataResult<ContextParameter<?>> parameterResult = castedTypeWithProvider.codec().parse(ops, rawKey);

					if (parameterResult.isSuccess() && succeeded.putIfAbsent(parameterResult.getOrThrow(), provider) != null) {
						return identity.apply2stable((unit, ignored) -> unit, DataResult.error(() -> "Duplicate key: \"" + key + "\""));
					}

					return identity.apply2stable((unit, ignored) -> unit, parameterResult);

				},
				(first, second) ->
					first.apply2stable((firstUnit, ignored) -> firstUnit, second)
			);

			Map<ContextParameter<?>, ValueProvider<?>> elements = ImmutableMap.copyOf(succeeded);
			return result.map(ignored -> elements).setPartial(elements);

		}

		private <O> RecordBuilder<O> encodeMap(Map<ContextParameter<?>, ValueProvider<?>> input, DynamicOps<O> ops, RecordBuilder<O> prefix) {

			for (var entry : input.entrySet()) {

				ContextParameter<?> parameter = entry.getKey();
				ValueProvider<?> provider = entry.getValue();

				if (!(parameter.getType() instanceof ContextParameter.TypeWithProvider<?, ?> type)) {
					continue;
				}

				ContextParameter.TypeWithProvider<?, ValueProvider<?>> casted = (ContextParameter.TypeWithProvider<?, ValueProvider<?>>) type;
				RecordBuilder<O> mapBuilder = TYPE_CODEC
					.encode(type, ops, ops.mapBuilder()
						.add("value", casted.providerCodec().encodeStart(ops, provider)));

				prefix.add(entry.getKey().name().toString(), mapBuilder.build(ops.empty()));

			}

			return prefix;

		}

	};

	@SuppressWarnings("unchecked")
	private static final StreamCodec<RegistryFriendlyByteBuf, Map<ContextParameter<?>, ValueProvider<?>>> VALUE_MAP_STREAM_CODEC = new StreamCodec<>() {

		@Override
		public @NotNull Map<ContextParameter<?>, ValueProvider<?>> decode(RegistryFriendlyByteBuf buf) {

			int size = buf.readInt();
			Map<ContextParameter<?>, ValueProvider<?>> map = new Object2ObjectLinkedOpenHashMap<>(size);

			for (int i = 0; i < size; i++) {

				ContextParameter.Type<?> type = ContextParameter.Type.STREAM_CODEC.decode(buf);

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

				ContextParameter.TypeWithProvider<?, ValueProvider<?>> casted = (ContextParameter.TypeWithProvider<?, ValueProvider<?>>) typeWithProvider;
				ContextParameter.Type.STREAM_CODEC.encode(buf, casted);

				buf.writeResourceLocation(parameter.name());
				casted.providerStreamCodec().encode(buf, provider);

			}

		}

	};


	public static final ContextParameterMap EMPTY = new ContextParameterMap(Map.of());

	public static final Codec<ContextParameterMap> CODEC = VALUE_MAP_CODEC.xmap(ContextParameterMap::new, ContextParameterMap::map);
	public static final StreamCodec<RegistryFriendlyByteBuf, ContextParameterMap> STREAM_CODEC = VALUE_MAP_STREAM_CODEC.map(ContextParameterMap::new, ContextParameterMap::map);

	@Override
	public void validate(ContextValidator validator) {
		map().forEach((parameter, provider) -> provider.validate(validator.forChild(".\"" + parameter.name() + "\"")));
	}

	public ContextValidator.Parameters forValidation() {

		var builder = new ContextValidator.Parameters.Builder();
		map().keySet().forEach(builder::required);

		return builder.build();

	}

	@SuppressWarnings("unchecked")
	public Context forUser(Context baseContext, UnaryOperator<Context> parametersResolver) {

		var builder = new Context.Builder(baseContext);
		var resolved = parametersResolver.apply(baseContext);

		map().forEach((parameter, provider) -> builder.withOptional((ContextParameter<Object>) parameter, (Optional<Object>) provider.getValue(resolved.forChild(".\"" + parameter.name() + "\""))));
		return builder.build(baseContext.level());

	}

}
