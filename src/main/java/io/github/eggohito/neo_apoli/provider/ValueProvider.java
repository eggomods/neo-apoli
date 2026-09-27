package io.github.eggohito.neo_apoli.provider;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextUser;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface ValueProvider<V> extends ContextUser {

	@NotNull
	Type<?> getType();

	Optional<V> getValue(Context context);

	interface Type<P extends ValueProvider<?>> {

		MapCodec<P> mapCodec();

		StreamCodec<RegistryFriendlyByteBuf, P> streamCodec();

	}

}
