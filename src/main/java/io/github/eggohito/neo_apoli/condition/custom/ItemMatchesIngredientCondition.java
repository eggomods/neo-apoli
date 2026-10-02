package io.github.eggohito.neo_apoli.condition.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.item.ItemProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliConditionTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;

public record ItemMatchesIngredientCondition(Ingredient ingredient, ItemProvider item) implements Condition {

	public static final MapCodec<ItemMatchesIngredientCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Ingredient.CODEC.fieldOf("ingredient").forGetter(ItemMatchesIngredientCondition::ingredient),
		ItemProvider.CODEC.fieldOf("item").forGetter(ItemMatchesIngredientCondition::item)
	).apply(instance, ItemMatchesIngredientCondition::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ItemMatchesIngredientCondition> STREAM_CODEC = StreamCodec.composite(
		Ingredient.CONTENTS_STREAM_CODEC, ItemMatchesIngredientCondition::ingredient,
		ItemProvider.STREAM_CODEC, ItemMatchesIngredientCondition::item,
		ItemMatchesIngredientCondition::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliConditionTypes.ITEM_MATCHES_INGREDIENT;
	}

	@Override
	public boolean test(Context context) {
		return item().getValue(context.forChild(".item"))
			.map(ingredient()::test)
			.orElse(false);
	}

	@Override
	public void validate(ContextValidator validator) {
		Condition.super.validate(validator);
		item().validate(validator.forChild(".item"));
	}

}
