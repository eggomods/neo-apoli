package io.github.eggohito.neo_apoli.context.parameter;

import com.mojang.serialization.Codec;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameterTypes;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record ItemContextParameter(ResourceLocation name) implements ContextParameter<ItemStack> {

	public static final Codec<ItemContextParameter> CODEC = ContextParameter.codec(NeoApoli.MOD_NAMESPACE, ItemContextParameter::new);
	public static final StreamCodec<ByteBuf, ItemContextParameter> STREAM_CODEC = ContextParameter.streamCodec(ItemContextParameter::new);

	@Override
	public Type<?, ?> getType() {
		return NeoApoliContextParameterTypes.ITEM;
	}

}
