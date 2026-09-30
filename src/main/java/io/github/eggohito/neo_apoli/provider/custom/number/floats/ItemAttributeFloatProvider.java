package io.github.eggohito.neo_apoli.provider.custom.number.floats;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.provider.custom.item.ItemProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliFloatProviderTypes;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record ItemAttributeFloatProvider(Holder<Attribute> attribute, ItemProvider item, Optional<EntityProvider> entity) implements FloatProvider {

	public static final MapCodec<ItemAttributeFloatProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Attribute.CODEC.fieldOf("attribute").forGetter(ItemAttributeFloatProvider::attribute),
		ItemProvider.CODEC.fieldOf("item").forGetter(ItemAttributeFloatProvider::item),
		EntityProvider.CODEC.optionalFieldOf("entity").forGetter(ItemAttributeFloatProvider::entity)
	).apply(instance, ItemAttributeFloatProvider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ItemAttributeFloatProvider> STREAM_CODEC = StreamCodec.composite(
		Attribute.STREAM_CODEC, ItemAttributeFloatProvider::attribute,
		ItemProvider.STREAM_CODEC, ItemAttributeFloatProvider::item,
		ByteBufCodecs.optional(EntityProvider.STREAM_CODEC), ItemAttributeFloatProvider::entity,
		ItemAttributeFloatProvider::new
	);

	@Override
	public @NotNull FloatProvider.Type<?> getType() {
		return NeoApoliFloatProviderTypes.ITEM_ATTRIBUTE;
	}

	@Override
	public Optional<Float> getValue(Context context) {

		ItemAttributeModifiers attributeModifiers = item().getValue(context.forChild(".item"))
			.flatMap(item -> Optional.ofNullable(item.get(DataComponents.ATTRIBUTE_MODIFIERS)))
			.orElse(null);

		if (attributeModifiers == null) {
			return Optional.empty();
		}

		List<ItemAttributeModifiers.Entry> sorted = new ObjectArrayList<>(attributeModifiers.modifiers());
		sorted.sort(Comparator.comparing(entry -> entry.modifier().operation()));

		Entity entity = entity()
			.flatMap(self -> self.getValue(context.forChild(".entity")))
			.orElse(null);

		double baseValue = entity instanceof LivingEntity livingEntity && livingEntity.getAttributes().hasAttribute(attribute())
			? livingEntity.getAttributeBaseValue(attribute())
			: 0.0D;
		double totalValue = baseValue;

		for (var entry : sorted) {

			Holder<Attribute> attribute = entry.attribute();
			AttributeModifier modifier = entry.modifier();

			if (!Objects.equals(attribute, this.attribute())) {
				continue;
			}

			totalValue += switch (modifier.operation()) {
				case ADD_VALUE ->
					modifier.amount();
				case ADD_MULTIPLIED_BASE ->
					modifier.amount() * baseValue;
				case ADD_MULTIPLIED_TOTAL ->
					baseValue * totalValue;
			};

		}

		return Optional.of((float) attribute().value().sanitizeValue(totalValue));

	}

	@Override
	public void validate(ContextValidator validator) {
		FloatProvider.super.validate(validator);
		item().validate(validator.forChild(".item"));
		entity().ifPresent(entity -> entity.validate(validator.forChild(".entity")));
	}

}
