package io.github.eggohito.neo_apoli.power.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.parameter.ItemContextParameter;
import io.github.eggohito.neo_apoli.context.visitor.ClearableVisitor;
import io.github.eggohito.neo_apoli.power.Power;
import io.github.eggohito.neo_apoli.power.entity.Powers;
import io.github.eggohito.neo_apoli.provider.custom.bool.BooleanProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliPowerTypes;
import io.github.eggohito.neo_apoli.util.conditional.CompositeConditional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

public record ModifyItemWearablePower(EnumMap<EquipmentSlot, CompositeConditional.Entry<BooleanProvider>> slots) implements Power {

	public static final ClearableVisitor<Instance> VISITOR = ClearableVisitor.createThreadLocalized();
	public static final ItemContextParameter WORN_ITEM = new ItemContextParameter(NeoApoli.id("worn_item"));

	private static final Codec<EnumMap<EquipmentSlot, CompositeConditional.Entry<BooleanProvider>>> SLOTS_CODEC = ExtraCodecs.nonEmptyMap(Codec.unboundedMap(EquipmentSlot.CODEC, CompositeConditional.Entry.codec(Condition.CODEC.fieldOf("condition"), BooleanProvider.CODEC.fieldOf("allow")))).xmap(EnumMap::new, Function.identity());
	private static final StreamCodec<RegistryFriendlyByteBuf, EnumMap<EquipmentSlot, CompositeConditional.Entry<BooleanProvider>>> SLOTS_STREAM_CODEC = ByteBufCodecs.map(size -> new EnumMap<>(EquipmentSlot.class), EquipmentSlot.STREAM_CODEC, CompositeConditional.Entry.streamCodec(Condition.STREAM_CODEC, BooleanProvider.STREAM_CODEC));

	public static final MapCodec<ModifyItemWearablePower> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(SLOTS_CODEC.fieldOf("slots").forGetter(ModifyItemWearablePower::slots))
		.apply(instance, ModifyItemWearablePower::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ModifyItemWearablePower> STREAM_CODEC = StreamCodec.composite(
		SLOTS_STREAM_CODEC, ModifyItemWearablePower::slots,
		ModifyItemWearablePower::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliPowerTypes.MODIFY_ITEM_WEARABLE;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	@Override
	public void validate(Context.Validator validator) {

		Power.super.validate(validator);
		Context.Validator slotsValidator = validator.forChild(".slots");

		this.slots().forEach((slot, entry) -> {

			Context.Validator entryValidator = slotsValidator.forChild("." + slot.getSerializedName());

			entry.condition().validate(entryValidator.forChild(".condition"));
			entry.value().validate(entryValidator.forChild(".allow"));

		});

	}

	//	TODO: Unequip all previously equipped items and either drop or insert them to the inventory
	public static class Instance extends Power.Instance<ModifyItemWearablePower> {

		protected Instance(@NotNull ModifyItemWearablePower power) {
			super(power);
		}

		public Context createContext(Entity holder, ItemStack stack) {
			return this.createHolderContextBuilder(holder)
				.withRequired(WORN_ITEM, stack)
				.build(holder.level());
		}

		public EnumMap<EquipmentSlot, CompositeConditional.Entry<BooleanProvider>> slots() {
			return power.slots();
		}

	}

	public static boolean modify(Entity equipper, ItemStack equippedStack, EquipmentSlot targetSlot, BooleanSupplier defaultValue) {

		boolean allowed = false;
		for (var instance : Powers.getInstances(equipper, Instance.class)) {

			Context instanceContext = instance.createContext(equipper, equippedStack);

			try {

				if (!VISITOR.push(instance) || !instance.isActive(instanceContext)) {
					continue;
				}

				for (var entry : instance.slots().entrySet()) {

					EquipmentSlot slot = entry.getKey();
					CompositeConditional.Entry<BooleanProvider> aEntry = entry.getValue();

					if (slot != targetSlot) {
						continue;
					}

					Context slotsContext = instanceContext.forChild(".slots");
					Context entryContext = slotsContext.forChild("." + slot.getSerializedName());

					if (aEntry.condition().test(entryContext.forChild(".condition"))) {

						if (aEntry.value().getBoolean(entryContext.forChild(".allow"))) {
							allowed = true;
						}

						else {
							return false;
						}

					}

				}

			}

			finally {
				VISITOR.pop(instance);
			}

		}

		return allowed
			|| defaultValue.getAsBoolean();

	}

}
