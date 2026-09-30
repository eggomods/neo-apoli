package io.github.eggohito.neo_apoli.condition.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.block.BlockProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliConditionTypes;
import io.github.eggohito.neo_apoli.util.CachedBlock;
import io.github.eggohito.neo_apoli.util.RegistryUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public record IsBlockInTagCondition(TagKey<Block> tag, BlockProvider block) implements Condition {

	public static final MapCodec<IsBlockInTagCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		TagKey.hashedCodec(Registries.BLOCK).fieldOf("tag").forGetter(IsBlockInTagCondition::tag),
		BlockProvider.CODEC.fieldOf("block").forGetter(IsBlockInTagCondition::block)
	).apply(instance, IsBlockInTagCondition::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, IsBlockInTagCondition> STREAM_CODEC = StreamCodec.composite(
		TagKey.streamCodec(Registries.BLOCK), IsBlockInTagCondition::tag,
		BlockProvider.STREAM_CODEC, IsBlockInTagCondition::block,
		IsBlockInTagCondition::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliConditionTypes.IS_BLOCK_IN_TAG;
	}

	@Override
	public boolean test(Context context) {
		return block().getValue(context.forChild(".block"))
			.stream()
			.map(CachedBlock::state)
			.anyMatch(state -> state.is(this.tag()));
	}

	@Override
	public void validate(ContextValidator validator) {
		Condition.super.validate(validator);
		RegistryUtil.validateTag(validator.forChild(".tag"), tag());
		block().validate(validator.forChild(".block"));
	}

}
