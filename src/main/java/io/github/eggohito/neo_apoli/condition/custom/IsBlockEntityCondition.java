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

import java.util.Objects;

public record IsBlockEntityCondition(BlockProvider block) implements Condition {

	public static final MapCodec<IsBlockEntityCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(BlockProvider.CODEC.fieldOf("block").forGetter(IsBlockEntityCondition::block))
		.apply(instance, IsBlockEntityCondition::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, IsBlockEntityCondition> STREAM_CODEC = StreamCodec.composite(
		BlockProvider.STREAM_CODEC, IsBlockEntityCondition::block,
		IsBlockEntityCondition::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliConditionTypes.IS_BLOCK_ENTITY;
	}

	@Override
	public boolean test(Context context) {
		return block().getValue(context)
			.stream()
			.map(CachedBlock::entity)
			.anyMatch(Objects::nonNull);
	}

	@Override
	public void validate(ContextValidator validator) {
		Condition.super.validate(validator);
		block().validate(validator.forChild(".block"));
	}

}
