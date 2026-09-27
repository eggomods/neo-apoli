package io.github.eggohito.neo_apoli.action.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliActionTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public record ApplyEffectsAction(List<MobEffectInstance> effects, EntityProvider entity) implements Action {

	public static final MapCodec<ApplyEffectsAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		ExtraCodecs.nonEmptyList(MobEffectInstance.CODEC.listOf()).fieldOf("effects").forGetter(ApplyEffectsAction::effects),
		EntityProvider.CODEC.fieldOf("entity").forGetter(ApplyEffectsAction::entity)
	).apply(instance, ApplyEffectsAction::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ApplyEffectsAction> STREAM_CODEC = StreamCodec.composite(
		MobEffectInstance.STREAM_CODEC.apply(ByteBufCodecs.list()), ApplyEffectsAction::effects,
		EntityProvider.STREAM_CODEC, ApplyEffectsAction::entity,
		ApplyEffectsAction::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliActionTypes.APPLY_EFFECTS;
	}

	@Override
	public void execute(Context context) {

		if (!context.level().isClientSide() && entity().getValue(context.forChild(".entity")).orElse(null) instanceof LivingEntity livingEntity) {
			effects().forEach(livingEntity::addEffect);
		}

	}

	@Override
	public void validate(Context.Validator validator) {
		Action.super.validate(validator);
		entity().validate(validator.forChild(".entity"));
	}

}
