package io.github.eggohito.neo_apoli.action.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.parameter.ItemContextParameter;
import io.github.eggohito.neo_apoli.context.parameter.SlotAccessContextParameter;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliActionTypes;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParams;
import io.github.eggohito.neo_apoli.util.IndexedStack;
import io.github.eggohito.neo_apoli.util.InventoryUtil;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record GiveItemsAction(Action giveAction, List<IndexedStack> stacks, EntityProvider entity) implements Action {

	public static final Context.Parameter<SlotAccess> GIVEN_SLOT = NeoApoliContextParams.registerInternal("given_slot", SlotAccessContextParameter::new);
	public static final Context.Parameter<ItemStack> GIVEN_ITEM = NeoApoliContextParams.registerInternal("given_item", ItemContextParameter::new);

	public static final ContextKeySet ACTION_PARAMETER_SET = new ContextKeySet.Builder()
		.required(GIVEN_SLOT)
		.required(GIVEN_ITEM)
		.build();

	public static final MapCodec<GiveItemsAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Action.CODEC.optionalFieldOf("give_action", NothingAction.INSTANCE).forGetter(GiveItemsAction::giveAction),
		IndexedStack.LIST_CODEC.fieldOf("stacks").forGetter(GiveItemsAction::stacks),
		EntityProvider.CODEC.fieldOf("entity").forGetter(GiveItemsAction::entity)
	).apply(instance, GiveItemsAction::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, GiveItemsAction> STREAM_CODEC = StreamCodec.composite(
		Action.STREAM_CODEC, GiveItemsAction::giveAction,
		IndexedStack.LIST_STREAM_CODEC, GiveItemsAction::stacks,
		EntityProvider.STREAM_CODEC, GiveItemsAction::entity,
		GiveItemsAction::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliActionTypes.GIVE_ITEMS;
	}

	@Override
	public void execute(Context context) {

		if (!(context.level() instanceof ServerLevel serverLevel)) {
			return;
		}

		Entity entity = entity()
			.getValue(context.forChild(".entity"))
			.orElse(null);

		if (entity == null) {
			return;
		}

		giveLoop:
		for (var indexedStack : stacks()) {

			SlotAccess givenStackAccess = InventoryUtil.createSingletonSlot(indexedStack.stack());
			Context itemContext = new Context.Builder(context)
				.withRequired(GIVEN_SLOT, givenStackAccess)
				.withRequired(GIVEN_ITEM, givenStackAccess.get())
				.build(serverLevel);

			giveAction().execute(itemContext.forChild(".give_action"));

			IntList slotIds = indexedStack.slotIds().orElseGet(IntArrayList::new);
			ItemStack givenStack = givenStackAccess.get();

			for (var slotId : slotIds) {

				SlotAccess slotAccess = entity.getSlot(slotId);
				ItemStack slotStack = slotAccess.get();

				if (slotStack.isEmpty() && slotAccess.set(givenStack)) {
					continue giveLoop;
				}

				else if (ItemStack.matches(slotStack, givenStack) && slotStack.getCount() < slotStack.getMaxStackSize()) {

					int insertAmount = Math.min(slotStack.getMaxStackSize() - slotStack.getCount(), givenStack.getCount());

					slotStack.grow(insertAmount);
					givenStack.shrink(insertAmount);

					if (givenStack.isEmpty()) {
						continue giveLoop;
					}

				}

			}

			if (entity instanceof Player player) {
				player.getInventory().placeItemBackInInventory(givenStack);
			}

			else {
				InventoryUtil.dropItem(serverLevel, entity, givenStack, false, false, 0);
			}

		}

	}

	@Override
	public void validate(Context.Validator validator) {
		Action.super.validate(validator);
		giveAction().validate(validator.withAdditionalKeysFromSets(ACTION_PARAMETER_SET).forChild(".give_action"));
		entity().validate(validator.forChild(".entity"));
	}

}
