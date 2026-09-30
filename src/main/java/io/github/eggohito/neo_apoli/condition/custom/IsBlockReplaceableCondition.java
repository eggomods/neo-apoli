package io.github.eggohito.neo_apoli.condition.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.block.BlockProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliConditionTypes;
import io.github.eggohito.neo_apoli.util.CachedBlock;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.state.BlockBehaviour;

public record IsBlockReplaceableCondition(BlockProvider block) implements Condition {

	public static final MapCodec<IsBlockReplaceableCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(BlockProvider.CODEC.fieldOf("block").forGetter(IsBlockReplaceableCondition::block))
		.apply(instance, IsBlockReplaceableCondition::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, IsBlockReplaceableCondition> STREAM_CODEC = StreamCodec.composite(
		BlockProvider.STREAM_CODEC, IsBlockReplaceableCondition::block,
		IsBlockReplaceableCondition::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliConditionTypes.IS_BLOCK_REPLACEABLE;
	}

	@Override
	public boolean test(Context context) {
		return block().getValue(context.forChild(".block"))
			.map(CachedBlock::state)
			.map(BlockBehaviour.BlockStateBase::canBeReplaced)
			.orElse(false);
	}

	@Override
	public void validate(ContextValidator validator) {
		Condition.super.validate(validator);
		block().validate(validator.forChild(".block"));
	}

}
