package io.github.eggohito.neo_apoli.condition.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.comparison.Comparator;
import io.github.eggohito.neo_apoli.comparison.custom.IntComparison;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.provider.custom.item.ContextItemProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.ints.ConstantIntProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.ints.ItemCountIntProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliConditionTypes;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParams;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public record EntityHasItemEquippedCondition(Condition equippedCondition, EquipmentSlotGroup slot, EntityProvider entity) implements Condition {

	public static final Context.Parameter<ItemStack> EQUIPPED_ITEM = NeoApoliContextParams.registerSimpleInternal("equipped_item", ItemStack.class);
	public static final ContextKeySet CONDITION_PARAMETER_SET = new ContextKeySet.Builder().required(EQUIPPED_ITEM).build();

	public static final MapCodec<EntityHasItemEquippedCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Condition.CODEC.optionalFieldOf("equipped_condition", new CompareCondition(new IntComparison(Comparator.GREATER_THAN, new ItemCountIntProvider(new ContextItemProvider(EQUIPPED_ITEM)), new ConstantIntProvider(0)))).forGetter(EntityHasItemEquippedCondition::equippedCondition),
		EquipmentSlotGroup.CODEC.fieldOf("slot").forGetter(EntityHasItemEquippedCondition::slot),
		EntityProvider.CODEC.fieldOf("entity").forGetter(EntityHasItemEquippedCondition::entity)
	).apply(instance, EntityHasItemEquippedCondition::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, EntityHasItemEquippedCondition> STREAM_CODEC = StreamCodec.composite(
		Condition.STREAM_CODEC, EntityHasItemEquippedCondition::equippedCondition,
		EquipmentSlotGroup.STREAM_CODEC, EntityHasItemEquippedCondition::slot,
		EntityProvider.STREAM_CODEC, EntityHasItemEquippedCondition::entity,
		EntityHasItemEquippedCondition::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliConditionTypes.ENTITY_HAS_ITEM_EQUIPPED;
	}

	@Override
	public boolean test(Context context) {

		Context entityContext = context.forChild(".entity");
		Entity entity = entity().getValue(entityContext).orElse(null);

		if (entityContext.hasProblems() || !(entity instanceof LivingEntity livingEntity)) {
			return false;
		}

		for (var equipmentSlot : EquipmentSlot.values()) {

			if (!slot().test(equipmentSlot)) {
				continue;
			}

			Context equippedContext = new Context.Builder(context)
				.withRequired(EQUIPPED_ITEM, livingEntity.getItemBySlot(equipmentSlot))
				.build(context.level());

			if (equippedCondition().test(equippedContext.forChild(".equipped_condition"))) {
				return true;
			}

		}

		return false;

	}

	@Override
	public void validate(Context.Validator validator) {
		Condition.super.validate(validator);
		equippedCondition().validate(validator.withAdditionalKeysFromSets(CONDITION_PARAMETER_SET).forChild(".equipped_condition"));
		entity().validate(validator.forChild(".entity"));
	}

}
