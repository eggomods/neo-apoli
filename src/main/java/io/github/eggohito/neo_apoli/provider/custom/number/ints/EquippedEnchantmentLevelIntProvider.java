package io.github.eggohito.neo_apoli.provider.custom.number.ints;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliIntProviderTypes;
import io.github.eggohito.neo_apoli.util.CodecUtil;
import io.github.eggohito.neo_apoli.util.StreamCodecUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

public record EquippedEnchantmentLevelIntProvider(Holder<Enchantment> enchantment, EquipmentSlotGroup slotGroup, Calculation calculation, EntityProvider entity) implements IntProvider {

	public static final MapCodec<EquippedEnchantmentLevelIntProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Enchantment.CODEC.fieldOf("enchantment").forGetter(EquippedEnchantmentLevelIntProvider::enchantment),
		EquipmentSlotGroup.CODEC.optionalFieldOf("slot_group", EquipmentSlotGroup.ANY).forGetter(EquippedEnchantmentLevelIntProvider::slotGroup),
		Calculation.CODEC.optionalFieldOf("calculation", Calculation.MAX).forGetter(EquippedEnchantmentLevelIntProvider::calculation),
		EntityProvider.CODEC.fieldOf("entity").forGetter(EquippedEnchantmentLevelIntProvider::entity)
	).apply(instance, EquippedEnchantmentLevelIntProvider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, EquippedEnchantmentLevelIntProvider> STREAM_CODEC = StreamCodec.composite(
		Enchantment.STREAM_CODEC, EquippedEnchantmentLevelIntProvider::enchantment,
		EquipmentSlotGroup.STREAM_CODEC, EquippedEnchantmentLevelIntProvider::slotGroup,
		Calculation.STREAM_CODEC, EquippedEnchantmentLevelIntProvider::calculation,
		EntityProvider.STREAM_CODEC, EquippedEnchantmentLevelIntProvider::entity,
		EquippedEnchantmentLevelIntProvider::new
	);

	@Override
	public @NotNull IntProvider.Type<?> getType() {
		return NeoApoliIntProviderTypes.EQUIPPED_ENCHANTMENT_LEVEL;
	}

	@Override
	public Optional<Integer> getValue(Context context) {
		return entity().getValue(context.forChild(".entity"))
			.filter(LivingEntity.class::isInstance)
			.map(LivingEntity.class::cast)
			.map(living -> calculation().getValue(living, enchantment(), slotGroup()));
	}

	@Override
	public void validate(Context.Validator validator) {
		IntProvider.super.validate(validator);
		entity().validate(validator.forChild(".entity"));
	}

	public enum Calculation {

		SUM {

			@Override
			public int getValue(LivingEntity wearer, Holder<Enchantment> enchantment, EquipmentSlotGroup slotGroup) {

				Map<EquipmentSlot, ItemStack> slotItems = enchantment.value().getSlotItems(wearer);
				int levels = 0;

				for (var entry : slotItems.entrySet()) {

					EquipmentSlot slot = entry.getKey();
					ItemStack stack = entry.getValue();

					if (slotGroup.test(slot)) {
						levels += stack.getEnchantments().getLevel(enchantment);
					}

				}

				return levels;

			}

		},

		MAX {

			@Override
			public int getValue(LivingEntity wearer, Holder<Enchantment> enchantment, EquipmentSlotGroup slotGroup) {

				Map<EquipmentSlot, ItemStack> slotItems = enchantment.value().getSlotItems(wearer);
				int levels = 0;

				for (var entry : slotItems.entrySet()) {

					EquipmentSlot slot = entry.getKey();
					ItemStack stack = entry.getValue();

					if (slotGroup.test(slot)) {
						levels = Math.max(levels, stack.getEnchantments().getLevel(enchantment));
					}

				}

				return levels;

			}

		};

		public static final Codec<Calculation> CODEC = CodecUtil.enumType(Calculation.class);
		public static final StreamCodec<ByteBuf, Calculation> STREAM_CODEC = StreamCodecUtil.enumType(Calculation.class);

		public abstract int getValue(LivingEntity wearer, Holder<Enchantment> enchantmentHolder, EquipmentSlotGroup slotGroup);

	}

}
