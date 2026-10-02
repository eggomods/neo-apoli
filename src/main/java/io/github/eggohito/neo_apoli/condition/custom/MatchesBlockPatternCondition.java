package io.github.eggohito.neo_apoli.condition.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.bool.BooleanProvider;
import io.github.eggohito.neo_apoli.provider.custom.bool.ConstantBooleanProvider;
import io.github.eggohito.neo_apoli.provider.custom.direction.DirectionProvider;
import io.github.eggohito.neo_apoli.provider.custom.vec3.Vec3Provider;
import io.github.eggohito.neo_apoli.registry.NeoApoliConditionTypes;
import io.github.eggohito.neo_apoli.util.ContextBlockPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record MatchesBlockPatternCondition(ContextBlockPattern pattern, Vec3Provider frontTopLeft, DirectionProvider forwards, DirectionProvider up, BooleanProvider partial) implements Condition {

	public static final MapCodec<MatchesBlockPatternCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		ContextBlockPattern.MAP_CODEC.forGetter(MatchesBlockPatternCondition::pattern),
		Vec3Provider.CODEC.fieldOf("front_top_left").forGetter(MatchesBlockPatternCondition::frontTopLeft),
		DirectionProvider.CODEC.fieldOf("forwards").forGetter(MatchesBlockPatternCondition::forwards),
		DirectionProvider.CODEC.fieldOf("up").forGetter(MatchesBlockPatternCondition::up),
		BooleanProvider.CODEC.optionalFieldOf("partial", new ConstantBooleanProvider(false)).forGetter(MatchesBlockPatternCondition::partial)
	).apply(instance, MatchesBlockPatternCondition::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, MatchesBlockPatternCondition> STREAM_CODEC = StreamCodec.composite(
		ContextBlockPattern.STREAM_CODEC, MatchesBlockPatternCondition::pattern,
		Vec3Provider.STREAM_CODEC, MatchesBlockPatternCondition::frontTopLeft,
		DirectionProvider.STREAM_CODEC, MatchesBlockPatternCondition::forwards,
		DirectionProvider.STREAM_CODEC, MatchesBlockPatternCondition::up,
		BooleanProvider.STREAM_CODEC, MatchesBlockPatternCondition::partial,
		MatchesBlockPatternCondition::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliConditionTypes.MATCHES_BLOCK_PATTERN;
	}

	@Override
	public boolean test(Context context) {

		BlockPos frontTopLeft = frontTopLeft().getValue(context.forChild(".front_top_left"))
			.map(BlockPos::containing)
			.orElse(null);

		if (frontTopLeft == null) {
			return false;
		}

		Direction forwards = forwards().getValue(context.forChild(".forwards")).orElse(null);
		Direction up = up().getValue(context.forChild(".up")).orElse(null);

		if (forwards == null || up == null || forwards == up || forwards == up.getOpposite()) {

			if (forwards != null && up != null) {
				context.reportProblem("The 'forwards' direction must not be the same as the 'up' direction or its opposite!");
			}

			return false;

		}

		ContextBlockPattern.Result result = pattern().check(context, frontTopLeft, forwards, up);
		boolean partial = partial().getBoolean(context.forChild(".partial"));

		return partial
			? !result.matches().isEmpty()
			: result.mismatches().isEmpty();

	}

	@Override
	public void validate(ContextValidator validator) {
		Condition.super.validate(validator);
		pattern().validate(validator);
		frontTopLeft().validate(validator.forChild(".front_top_left"));
		forwards().validate(validator.forChild(".forwards"));
		up().validate(validator.forChild(".up"));
		partial().validate(validator.forChild(".partial"));
	}

}
