package io.github.eggohito.neo_apoli.context.parameter;

import com.mojang.serialization.Codec;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.context.Context;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.Nullable;

public record DamageSourceContextParameter(ResourceLocation name) implements Context.Parameter<DamageSource> {

	public static final Codec<DamageSourceContextParameter> CODEC = Context.Parameter.codec(NeoApoli.MOD_NAMESPACE, DamageSourceContextParameter::new);
	public static final StreamCodec<ByteBuf, DamageSourceContextParameter> STREAM_CODEC = Context.Parameter.streamCodec(DamageSourceContextParameter::new);

	@Override
	public @Nullable Type<?, ?> getType() {
		return null;
	}

}
