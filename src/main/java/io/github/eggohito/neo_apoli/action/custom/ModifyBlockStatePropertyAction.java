package io.github.eggohito.neo_apoli.action.custom;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.block.BlockProvider;
import io.github.eggohito.neo_apoli.provider.custom.bool.BooleanProvider;
import io.github.eggohito.neo_apoli.provider.custom.string.StringProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliActionTypes;
import io.github.eggohito.neo_apoli.util.CachedBlock;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Optional;

public record ModifyBlockStatePropertyAction(StringProvider property, BlockProvider block, Optional<StringProvider> value, Optional<BooleanProvider> cycle) implements Action {

	private static final MapCodec<ModifyBlockStatePropertyAction> UNVALIDATED_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		StringProvider.CODEC.fieldOf("property").forGetter(ModifyBlockStatePropertyAction::property),
		BlockProvider.CODEC.fieldOf("block").forGetter(ModifyBlockStatePropertyAction::block),
		StringProvider.CODEC.optionalFieldOf("value").forGetter(ModifyBlockStatePropertyAction::value),
		BooleanProvider.CODEC.optionalFieldOf("cycle").forGetter(ModifyBlockStatePropertyAction::cycle)
	).apply(instance, ModifyBlockStatePropertyAction::new));

	public static final MapCodec<ModifyBlockStatePropertyAction> CODEC = UNVALIDATED_CODEC.mapResult(new MapCodec.ResultFunction<>() {

		@Override
		public <T> DataResult<ModifyBlockStatePropertyAction> apply(DynamicOps<T> ops, MapLike<T> input, DataResult<ModifyBlockStatePropertyAction> result) {
			return result.flatMap(
				action -> {

					var valueProp = action.value();
					var cycleProp = action.cycle();

					if (valueProp.isEmpty() && cycleProp.isEmpty()) {
						return DataResult.error(() -> "Any of 'value' or 'cycle' fields must be present in input: " + input);
					}

					else if (valueProp.isPresent() && cycleProp.isPresent()) {
						return DataResult.error(() -> "Both 'value' and 'cycle' fields cannot be present at the same time in input: " + input);
					}

					else {
						return DataResult.success(action);
					}

				}
			);
		}

		@Override
		public <T> RecordBuilder<T> coApply(DynamicOps<T> ops, ModifyBlockStatePropertyAction input, RecordBuilder<T> prefix) {
			return prefix;
		}

	});

	public static final StreamCodec<RegistryFriendlyByteBuf, ModifyBlockStatePropertyAction> STREAM_CODEC = StreamCodec.composite(
		StringProvider.STREAM_CODEC, ModifyBlockStatePropertyAction::property,
		BlockProvider.STREAM_CODEC, ModifyBlockStatePropertyAction::block,
		ByteBufCodecs.optional(StringProvider.STREAM_CODEC), ModifyBlockStatePropertyAction::value,
		ByteBufCodecs.optional(BooleanProvider.STREAM_CODEC), ModifyBlockStatePropertyAction::cycle,
		ModifyBlockStatePropertyAction::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliActionTypes.MODIFY_BLOCK_STATE_PROPERTY;
	}

	@Override
	public void execute(Context context) {

		if (context.level().isClientSide()) {
			return;
		}

		CachedBlock block = block()
			.getValue(context.forChild(".block"))
			.orElse(null);

		if (block == null) {
			return;
		}

		String propertyName = property()
			.getValue(context.forChild(".property"))
			.orElse(null);

		if (propertyName == null) {
			return;
		}

		BlockState state = block.state();
		Property<?> property = state.getBlock().getStateDefinition().getProperty(propertyName);

		if (property == null) {
			context.reportProblem("Block \"" + Util.getRegisteredName(BuiltInRegistries.BLOCK, state.getBlock()) + "\" didn't have the \"" + propertyName + "\" state property!");
		}

		else {
			this.cycleOrSetValue(context, block.pos(), state, property);
		}

	}

	@Override
	public void validate(ContextValidator validator) {
		Action.super.validate(validator);
		property().validate(validator.forChild(".property"));
		block().validate(validator.forChild(".block"));
		value().ifPresent(value -> value.validate(validator.forChild(".value")));
		cycle().ifPresent(cycle -> cycle.validate(validator.forChild(".cycle")));
	}

	private <T extends Comparable<T>> void cycleOrSetValue(Context context, BlockPos pos, BlockState state, Property<T> property) {

		Optional<String> value = value().flatMap(self -> self.getValue(context.forChild(".value")));
		Optional<Boolean> cycle = cycle().map(self -> self.getBoolean(context.forChild(".cycle")));

		if (cycle.isPresent() != value.isPresent()) {

			if (value.isPresent()) {
				property.getValue(value.get()).ifPresent(t -> context.level().setBlockAndUpdate(pos, state.setValue(property, t)));
			}

			else if (cycle.get()) {
				context.level().setBlockAndUpdate(pos, state.cycle(property));

			}

		}

	}


}
