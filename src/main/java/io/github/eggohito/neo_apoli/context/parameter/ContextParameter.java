package io.github.eggohito.neo_apoli.context.parameter;

import com.mojang.serialization.Codec;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.provider.ValueProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistries;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistryKeys;
import io.github.eggohito.neo_apoli.util.ResourceLocationUtil;
import io.github.eggohito.neo_apoli.util.alias.FixedRegistryAlias;
import io.github.eggohito.neo_apoli.util.alias.ResourceLocationAlias;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;
import java.util.function.Predicate;

public interface ContextParameter<T> {

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
