package io.github.eggohito.neo_apoli.condition.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.provider.custom.block.BlockProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliConditionTypes;
import io.github.eggohito.neo_apoli.util.CachedBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Block;

public record IsBlockOfTypeCondition(Block blockType, BlockProvider block) implements Condition {

	public static final MapCodec<IsBlockOfTypeCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block_type").forGetter(IsBlockOfTypeCondition::blockType),
		BlockProvider.CODEC.fieldOf("block").forGetter(IsBlockOfTypeCondition::block)
	).apply(instance, IsBlockOfTypeCondition::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, IsBlockOfTypeCondition> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.registry(Registries.BLOCK), IsBlockOfTypeCondition::blockType,
		BlockProvider.STREAM_CODEC, IsBlockOfTypeCondition::block,
		IsBlockOfTypeCondition::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliConditionTypes.IS_BLOCK_OF_TYPE;
	}

	@Override
	public boolean test(Context context) {
		return block().getValue(context.forChild(".block"))
			.stream()
			.map(CachedBlock::state)
			.anyMatch(state -> state.is(this.blockType()));
	}

	@Override
	public void validate(Context.Validator validator) {
		Condition.super.validate(validator);
		block().validate(validator.forChild(".block"));
	}

}
