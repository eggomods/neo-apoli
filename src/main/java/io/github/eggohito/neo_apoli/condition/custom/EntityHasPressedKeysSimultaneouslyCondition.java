package io.github.eggohito.neo_apoli.condition.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidatable;
import io.github.eggohito.neo_apoli.key.KeyState;
import io.github.eggohito.neo_apoli.key.manager.KeyStateManager;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.ints.ConstantIntProvider;
import io.github.eggohito.neo_apoli.provider.custom.string.StringProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliConditionTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;

import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.UUID;

public record EntityHasPressedKeysSimultaneouslyCondition(List<StringProvider> keys, IntProvider offset, EntityProvider entity) implements Condition {

	public static final MapCodec<EntityHasPressedKeysSimultaneouslyCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		StringProvider.CODEC.listOf().fieldOf("keys").forGetter(EntityHasPressedKeysSimultaneouslyCondition::keys),
		IntProvider.CODEC.optionalFieldOf("offset", new ConstantIntProvider(3)).forGetter(EntityHasPressedKeysSimultaneouslyCondition::offset),
		EntityProvider.CODEC.fieldOf("entity").forGetter(EntityHasPressedKeysSimultaneouslyCondition::entity)
	).apply(instance, EntityHasPressedKeysSimultaneouslyCondition::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, EntityHasPressedKeysSimultaneouslyCondition> STREAM_CODEC = StreamCodec.composite(
		StringProvider.STREAM_CODEC.apply(ByteBufCodecs.list()), EntityHasPressedKeysSimultaneouslyCondition::keys,
		IntProvider.STREAM_CODEC, EntityHasPressedKeysSimultaneouslyCondition::offset,
		EntityProvider.STREAM_CODEC, EntityHasPressedKeysSimultaneouslyCondition::entity,
		EntityHasPressedKeysSimultaneouslyCondition::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliConditionTypes.ENTITY_HAS_PRESSED_KEYS_SIMULTANEOUSLY;
	}

	@Override
	public boolean test(Context context) {

		Context offsetContext = context.forChild(".offset");
		long offset = offset().getInt(offsetContext);

		if (offsetContext.hasProblems()) {
			return false;
		}

		UUID uuid = entity().getValue(context.forChild(".entity"))
			.map(Entity::getUUID)
			.orElse(null);

		if (uuid == null) {
			return false;
		}

		ListIterator<StringProvider> listIterator = keys().listIterator();
		long previousPressedTime = Long.MIN_VALUE;

		while (listIterator.hasNext()) {

			Context keyContext = context.forChild(".keys[" + listIterator.nextIndex() + "]");
			Optional<KeyState> optState = listIterator.next()
				.getValue(keyContext)
				.flatMap(id -> KeyStateManager.getInstance().getCurrentState(uuid, id));

			if (optState.isEmpty()) {
				continue;
			}

			KeyState state = optState.get();
			long currentPressedTime = state.pressedTime();

			if (state.pressed()) {

				if (previousPressedTime == Long.MIN_VALUE) {
					previousPressedTime = currentPressedTime;
				}

				else if (currentPressedTime < (previousPressedTime - offset) || currentPressedTime > (previousPressedTime + offset)) {
					return false;
				}

			}

		}

		return true;

	}

	@Override
	public void validate(Context.Validator validator) {
		Condition.super.validate(validator);
		ContextValidatable.validate(keys(), validator.forChild(".keys"), index -> ".keys[" + index + "]");
		offset().validate(validator.forChild(".offset"));
		entity().validate(validator.forChild(".entity"));
	}

}
