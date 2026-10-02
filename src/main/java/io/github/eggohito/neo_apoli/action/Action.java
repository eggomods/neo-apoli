package io.github.eggohito.neo_apoli.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.action.custom.SequenceAction;
import io.github.eggohito.neo_apoli.codec.MultiAlternativeCodec;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextUser;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistries;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistryKeys;
import io.github.eggohito.neo_apoli.util.alias.FixedRegistryAlias;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public interface Action extends ContextUser {

	MapCodec<Action> MAP_CODEC = Type.CODEC.dispatchMap(Action::getType, Type::mapCodec);

	Codec<Action> CODEC = Codec.recursive(Action.class.getSimpleName(), codec -> new MultiAlternativeCodec<>(MAP_CODEC.codec(), codec.listOf().xmap(SequenceAction::new, SequenceAction::actions)));

	StreamCodec<RegistryFriendlyByteBuf, Action> STREAM_CODEC = Type.STREAM_CODEC.dispatch(Action::getType, Type::streamCodec);

	Type<?> getType();

	void execute(Context context);

	record Type<A extends Action>(MapCodec<A> mapCodec, StreamCodec<RegistryFriendlyByteBuf, A> streamCodec) {

		public static final FixedRegistryAlias<Type<?>> ALIASES = FixedRegistryAlias.of(NeoApoliRegistries.ACTION_TYPE);

		public static final Codec<Type<?>> CODEC = ALIASES.createCodec(NeoApoli.MOD_NAMESPACE);

		public static final StreamCodec<RegistryFriendlyByteBuf, Type<?>> STREAM_CODEC = ByteBufCodecs.registry(NeoApoliRegistryKeys.ACTION_TYPE);

	}

}
