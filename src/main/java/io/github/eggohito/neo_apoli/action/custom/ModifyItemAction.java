package io.github.eggohito.neo_apoli.action.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.provider.custom.slot.SlotProvider;
import io.github.eggohito.neo_apoli.provider.custom.vec3.Vec3Provider;
import io.github.eggohito.neo_apoli.registry.NeoApoliActionTypes;
import io.github.eggohito.neo_apoli.util.RegistryUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public record ModifyItemAction(ResourceKey<LootItemFunction> modifier, SlotProvider slot, Optional<Vec3Provider> position, Optional<EntityProvider> entity) implements Action {

	public static final MapCodec<ModifyItemAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		ResourceKey.codec(Registries.ITEM_MODIFIER).fieldOf("modifier").forGetter(ModifyItemAction::modifier),
		SlotProvider.CODEC.fieldOf("slot").forGetter(ModifyItemAction::slot),
		Vec3Provider.CODEC.optionalFieldOf("position").forGetter(ModifyItemAction::position),
		EntityProvider.CODEC.optionalFieldOf("entity").forGetter(ModifyItemAction::entity)
	).apply(instance, ModifyItemAction::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ModifyItemAction> STREAM_CODEC = StreamCodec.composite(
		ResourceKey.streamCodec(Registries.ITEM_MODIFIER), ModifyItemAction::modifier,
		SlotProvider.STREAM_CODEC, ModifyItemAction::slot,
		ByteBufCodecs.optional(Vec3Provider.STREAM_CODEC), ModifyItemAction::position,
		ByteBufCodecs.optional(EntityProvider.STREAM_CODEC), ModifyItemAction::entity,
		ModifyItemAction::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliActionTypes.MODIFY_ITEM;
	}

	@Override
	public void execute(Context context) {

		if (!(context.level() instanceof ServerLevel serverLevel)) {
			return;
		}

		SlotAccess slotAccess = slot().getValue(context.forChild(".slot")).orElse(SlotAccess.NULL);
		ItemStack stack = slotAccess.get();

		if (slotAccess == SlotAccess.NULL || stack.isEmpty()) {
			return;
		}

		LootItemFunction modifier = serverLevel.getServer().reloadableRegistries().lookup().get(this.modifier())
			.map(Holder.Reference::value)
			.orElse(null);
		LootParams lootParams = new LootParams.Builder(serverLevel)
			.withOptionalParameter(LootContextParams.THIS_ENTITY, entity().flatMap(p -> p.getValue(context.forChild(".entity"))).orElse(null))
			.withParameter(LootContextParams.ORIGIN, position().flatMap(p -> p.getValue(context.forChild(".position"))).orElse(Vec3.ZERO))
			.create(LootContextParamSets.COMMAND);

		if (modifier == null) {
			return;
		}

		LootContext lootContext = new LootContext.Builder(lootParams).create(Optional.empty());
		lootContext.pushVisitedElement(LootContext.createVisitedEntry(modifier));

		ItemStack newStack = modifier.apply(stack.copy(), lootContext);
		newStack.limitSize(newStack.getMaxStackSize());

		slotAccess.set(newStack);

	}

	@Override
	public void validate(ContextValidator validator) {
		Action.super.validate(validator);
		RegistryUtil.validateKey(validator.forChild(".modifier"), modifier());
		slot().validate(validator.forChild(".slot"));
		position().ifPresent(p -> p.validate(validator.forChild(".position")));
		entity().ifPresent(p -> p.validate(validator.forChild(".entity")));
	}

}
