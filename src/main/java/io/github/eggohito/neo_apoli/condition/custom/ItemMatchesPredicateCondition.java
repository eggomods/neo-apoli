package io.github.eggohito.neo_apoli.condition.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.codec.NeoApoliCodecs;
import io.github.eggohito.neo_apoli.codec.NeoApoliStreamCodecs;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.item.ItemProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliConditionTypes;
import io.github.eggohito.neo_apoli.util.ParsedArgument;
import net.minecraft.commands.arguments.item.ItemPredicateArgument;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record ItemMatchesPredicateCondition(ParsedArgument<ItemPredicateArgument.Result> predicate, ItemProvider item) implements Condition {

	public static final MapCodec<ItemMatchesPredicateCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		NeoApoliCodecs.ITEM_PREDICATE.fieldOf("predicate").forGetter(ItemMatchesPredicateCondition::predicate),
		ItemProvider.CODEC.fieldOf("item").forGetter(ItemMatchesPredicateCondition::item)
	).apply(instance, ItemMatchesPredicateCondition::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ItemMatchesPredicateCondition> STREAM_CODEC = StreamCodec.composite(
		NeoApoliStreamCodecs.ITEM_PREDICATE, ItemMatchesPredicateCondition::predicate,
		ItemProvider.STREAM_CODEC, ItemMatchesPredicateCondition::item,
		ItemMatchesPredicateCondition::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliConditionTypes.ITEM_MATCHES_PREDICATE;
	}

	@Override
	public boolean test(Context context) {
		return item().getValue(context.forChild(".item"))
			.map(predicate().argument()::test)
			.orElse(false);
	}

	@Override
	public void validate(ContextValidator validator) {
		Condition.super.validate(validator);
		item().validate(validator.forChild(".item"));
	}

}
