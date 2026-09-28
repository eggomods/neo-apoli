package io.github.eggohito.neo_apoli.context;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.*;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.context.visitor.Visitor;
import io.github.eggohito.neo_apoli.provider.ValueProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistries;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistryKeys;
import io.github.eggohito.neo_apoli.util.Reporter;
import io.github.eggohito.neo_apoli.util.ResourceLocationUtil;
import io.github.eggohito.neo_apoli.util.alias.FixedRegistryAlias;
import io.github.eggohito.neo_apoli.util.alias.ResourceLocationAlias;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public final class Context implements ContextHolder {

	public static final ResourceLocationAlias ALIASES = new ResourceLocationAlias();

	@Getter
	private final Level level;
	@Getter
	private final Reporter reporter;

	private final ImmutableMap<Parameter<?>, Object> params;
	private final Set<ContextUser> visited;

	private Context(Level level, Reporter reporter, ImmutableMap<Parameter<?>, Object> params, Set<ContextUser> visited) {
		this.level = level;
		this.reporter = reporter;
		this.params = params;
		this.visited = visited;
	}

	@Override
	public ImmutableSet<Parameter<?>> parameters() {
		return params.keySet();
	}

	@Override
	public @Nullable <T> T getNullable(Parameter<T> parameter) {
		return (T) this.params.get(parameter);
	}

	public Visitor<ContextUser> visitor() {
		return new Visitor<>() {

			@Override
			public boolean contains(ContextUser element) {
				return visited.contains(element);
			}

			@Override
			public boolean push(ContextUser element) {
				return visited.add(element);
			}

			@Override
			public void pop(ContextUser element) {
				visited.remove(element);
			}

		};
	}

	public Context forChild(String path) {
		return new Context(this.level(), this.reporter().forChild(path), this.params, this.visited);
	}

	public void reportProblem(String message) {
		this.reporter.report(message);
	}

	public boolean hasProblems() {
		return reporter().hasProblems();
	}

	public static final class Builder implements ContextHolder {

		private final Map<Parameter<?>, Object> params;
		private final Set<ContextUser> visited;

		private Reporter reporter;

		Builder(Map<Parameter<?>, Object> params, Reporter reporter, Set<ContextUser> visited) {
			this.params = params;
			this.reporter = reporter;
			this.visited = visited;
		}

		public Builder(Context context) {
			this(new Object2ObjectLinkedOpenHashMap<>(context.params), context.reporter, context.visited);
		}

		public Builder() {
			this(new Object2ObjectLinkedOpenHashMap<>(), new Reporter(), new ObjectOpenHashSet<>());
		}

		@Override
		public Set<Parameter<?>> parameters() {
			return Set.copyOf(params.keySet());
		}

		@Override
		public @Nullable <T> T getNullable(Parameter<T> parameter) {
			return (T) this.params.get(parameter);
		}

		public <T> Builder withNullable(Parameter<T> parameter, @Nullable T value) {

			if (value != null) {
				this.params.put(parameter, value);
			}

			return this;

		}

		public <T> Builder withRequired(Parameter<T> key, @NotNull T value) {
			this.params.put(key, value);
			return this;
		}

		public <T> Builder withOptional(Parameter<T> key, Optional<T> value) {
			return this.withNullable(key, value.orElse(null));
		}

		public Builder withReporter(Reporter reporter) {
			this.reporter = reporter;
			return this;
		}

		public Context build(Level level) {
			return new Context(level, this.reporter, ImmutableMap.copyOf(this.params), this.visited);
		}

	}

	public static class Validator {

		@Getter
		private final ContextParams params;
		@Getter
		private final Reporter reporter;

		private final Optional<HolderLookup.Provider> resolver;
		private final Set<ResourceKey<?>> visited;

		Validator(ContextParams params, Reporter reporter, Optional<HolderLookup.Provider> resolver, Set<ResourceKey<?>> visited) {
			this.params = params;
			this.reporter = reporter;
			this.resolver = resolver;
			this.visited = visited;
		}

		public Validator(ContextParams params, Reporter reporter, @NotNull HolderLookup.Provider resolver) {
			this(params, reporter, Optional.of(resolver), new ObjectOpenHashSet<>());
		}

		public Validator(ContextParams params, Reporter reporter) {
			this(params, reporter, Optional.empty(), new ObjectOpenHashSet<>());
		}

		public Validator withParams(ContextParams... params) {
			return new Validator(this.params().mergeAll(params), this.reporter(), this.resolver, this.visited);
		}

		public Validator withResolver(@NotNull HolderLookup.Provider resolver) {
			return new Validator(this.params(), this.reporter(), Optional.of(resolver), this.visited);
		}

		public Validator forChild(String path) {
			return new Validator(this.params(), this.reporter().forChild(path), this.resolver, this.visited);
		}

		public Validator visitChild(String path, ResourceKey<?> key) {
			this.visited.add(key);
			return new Validator(this.params(), this.reporter().forChild(path), this.resolver, this.visited);
		}

		public HolderLookup.Provider resolver() {
			return resolver.orElseThrow(() -> new UnsupportedOperationException("References are not allowed!"));
		}

		public Visitor<ResourceKey<?>> visitor() {
			return new Visitor<>() {

				@Override
				public boolean contains(ResourceKey<?> element) {
					return visited.contains(element);
				}

				@Override
				public boolean push(ResourceKey<?> element) {
					return visited.add(element);
				}

				@Override
				public void pop(ResourceKey<?> element) {
					visited.remove(element);
				}

			};
		}

		public boolean allowsReferences() {
			return resolver.isPresent();
		}

		public boolean hasVisited(ResourceKey<?> key) {
			return visited.contains(key);
		}

		public void reportProblem(String message) {
			this.reporter.report(message);
		}

		public void validate(ContextUser user) {

			if (params() == ContextParams.INTENTIONALLY_EMPTY) {
				return;
			}

			Set<Context.Parameter<?>> missing = Sets.difference(user.getRequiredParameters(), params().allowed());

			if (!missing.isEmpty()) {
				this.reportProblem("The following parameters are not provided: [" + missing.stream().map(Parameter::name).map(ResourceLocation::toString).collect(Collectors.joining(", ")) + "]");
			}

		}

	}

	public interface Parameter<T> {

		Codec<Map<Parameter<?>, ValueProvider<?>>> VALUE_MAP_CODEC = new Codec<>() {

			private final static Codec<ResourceLocation> ID_CODEC = ResourceLocationUtil.codecWithDefaultNamespace(NeoApoli.MOD_NAMESPACE);
			private final static MapCodec<Type<?, ?>> TYPE_CODEC = Type.CODEC.fieldOf("type");

			@Override
			public <I> DataResult<Pair<Map<Parameter<?>, ValueProvider<?>>, I>> decode(DynamicOps<I> ops, I input) {
				return ops.getMap(input).flatMap(map -> this.decodeMap(ops, map)).map(r -> Pair.of(r, input));
			}

			@Override
			public <O> DataResult<O> encode(Map<Parameter<?>, ValueProvider<?>> input, DynamicOps<O> ops, O prefix) {
				return this.encodeMap(input, ops, ops.mapBuilder()).build(prefix);
			}

			private <I> DataResult<Map<Parameter<?>, ValueProvider<?>>> decodeMap(DynamicOps<I> ops, MapLike<I> input) {

				Map<Parameter<?>, ValueProvider<?>> succeeded = new Object2ObjectLinkedOpenHashMap<>();
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
							.map(id -> ALIASES.resolve(id, Predicate.not(id::equals)));

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

				Map<Parameter<?>, ValueProvider<?>> elements = ImmutableMap.copyOf(succeeded);
				return result.map(unit -> elements).setPartial(elements);

			}

			private <O> RecordBuilder<O> encodeMap(Map<Parameter<?>, ValueProvider<?>> input, DynamicOps<O> ops, RecordBuilder<O> prefix) {

				for (var entry : input.entrySet()) {

					Parameter<?> parameter = entry.getKey();
					ValueProvider<?> provider = entry.getValue();

					Parameter.Type<?, ValueProvider<?>> parameterType = (Type<?, ValueProvider<?>>) parameter.getType();

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

		ResourceLocation name();

		@Nullable
		Type<?, ?> getType();

		static <T, P extends Parameter<T>> Codec<P> codec(Function<ResourceLocation, P> constructor) {
			return ResourceLocation.CODEC.xmap(constructor, Parameter::name);
		}

		static <T, P extends Parameter<T>> Codec<P> codec(String defaultNamespace, Function<ResourceLocation, P> constructor) {
			return ResourceLocationUtil.codecWithDefaultNamespace(defaultNamespace)
				.xmap(id -> ALIASES.resolve(id, Predicate.not(id::equals)), Function.identity())
				.xmap(constructor, Parameter::name);
		}

		static <T, P extends Parameter<T>> StreamCodec<ByteBuf, P> streamCodec(Function<ResourceLocation, P> constructor) {
			return ResourceLocation.STREAM_CODEC.map(constructor, Parameter::name);
		}

		record Type<Parameter extends Context.Parameter<?>, Provider extends ValueProvider<?>>(Codec<Provider> providerCodec, Function<ResourceLocation, Parameter> factory) {

			public static final FixedRegistryAlias<Type<?, ?>> ALIASES = FixedRegistryAlias.of(NeoApoliRegistries.CONTEXT_PARAMETER_TYPE);

			public static final Codec<Type<?, ?>> CODEC = ALIASES.createCodec(NeoApoli.MOD_NAMESPACE);

			public static final StreamCodec<RegistryFriendlyByteBuf, Type<?, ?>> STREAM_CODEC = ByteBufCodecs.registry(NeoApoliRegistryKeys.CONTEXT_PARAMETER_TYPE);

			public Parameter create(ResourceLocation name) {
				return factory().apply(name);
			}

		}

	}

}
