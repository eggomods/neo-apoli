package io.github.eggohito.neo_apoli.condition.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.exception.PosOutOfBoundsException;
import io.github.eggohito.neo_apoli.exception.PosUnloadedException;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliConditionTypes;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParams;
import io.github.eggohito.neo_apoli.util.CachedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public record IsEntitySteppingOnBlockCondition(Condition steppedOnCondition, EntityProvider entity) implements Condition {

	public static final Context.Parameter<CachedBlock> STEPPED_ON_BLOCK = NeoApoliContextParams.registerSimpleInternal("stepped_on_block", CachedBlock.class);
	public static final ContextKeySet CONDITION_PARAMETER_SET = new ContextKeySet.Builder().required(STEPPED_ON_BLOCK).build();

	public static final MapCodec<IsEntitySteppingOnBlockCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Condition.CODEC.optionalFieldOf("stepped_on_condition", new ConstantCondition(true)).forGetter(IsEntitySteppingOnBlockCondition::steppedOnCondition),
		EntityProvider.CODEC.fieldOf("entity").forGetter(IsEntitySteppingOnBlockCondition::entity)
	).apply(instance, IsEntitySteppingOnBlockCondition::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, IsEntitySteppingOnBlockCondition> STREAM_CODEC = StreamCodec.composite(
		Condition.STREAM_CODEC, IsEntitySteppingOnBlockCondition::steppedOnCondition,
		EntityProvider.STREAM_CODEC, IsEntitySteppingOnBlockCondition::entity,
		IsEntitySteppingOnBlockCondition::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliConditionTypes.IS_ENTITY_STEPPING_ON_BLOCK;
	}

	@Override
	public boolean test(Context context) {

		Level level = context.level();
		Entity entity = entity().getValue(context.forChild(".entity")).orElse(null);

		try {

			if (!context.visitor().push(this)) {
				return false;
			}

			else if (entity == null || !entity.onGround()) {
				return false;
			}

			else {

				try {

					BlockPos steppingPos = entity.getOnPos();
					Context steppedOnContext = new Context.Builder(context)
						.withRequired(STEPPED_ON_BLOCK, CachedBlock.fromLoadedPos(level, steppingPos))
						.build(level);

					return steppedOnCondition().test(steppedOnContext.forChild(".stepped_on_condition"));

				}

				catch (PosUnloadedException | PosOutOfBoundsException ignored) {
					return false;
				}

			}

		}

		finally {
			context.visitor().pop(this);
		}

	}

	@Override
	public void validate(Context.Validator validator) {
		Condition.super.validate(validator);
		steppedOnCondition().validate(validator.withAdditionalKeysFromSets(CONDITION_PARAMETER_SET).forChild(".stepped_on_condition"));
		entity().validate(validator.forChild(".entity"));
	}

}
