package io.github.eggohito.neo_apoli.action.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliActionTypes;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;

import java.util.Optional;

public record DamageEntityAction(Holder<DamageType> damageType, FloatProvider amount, EntityProvider victim, Optional<EntityProvider> attacker) implements Action {

	public static final MapCodec<DamageEntityAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		DamageType.CODEC.fieldOf("damage_type").forGetter(DamageEntityAction::damageType),
		FloatProvider.CODEC.fieldOf("amount").forGetter(DamageEntityAction::amount),
		EntityProvider.CODEC.fieldOf("victim").forGetter(DamageEntityAction::victim),
		EntityProvider.CODEC.optionalFieldOf("attacker").forGetter(DamageEntityAction::attacker)
	).apply(instance, DamageEntityAction::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, DamageEntityAction> STREAM_CODEC = StreamCodec.composite(
		DamageType.STREAM_CODEC, DamageEntityAction::damageType,
		FloatProvider.STREAM_CODEC, DamageEntityAction::amount,
		EntityProvider.STREAM_CODEC, DamageEntityAction::victim,
		ByteBufCodecs.optional(EntityProvider.STREAM_CODEC), DamageEntityAction::attacker,
		DamageEntityAction::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliActionTypes.DAMAGE_ENTITY;
	}

	@Override
	public void execute(Context context) {

		if (!(context.level() instanceof ServerLevel serverLevel)) {
			return;
		}

		Entity victim = victim().getValue(context.forChild(".victim")).orElse(null);
		Entity attacker = attacker().flatMap(p -> p.getValue(context.forChild("attacker"))).orElse(null);

		if (victim == null) {
			return;
		}

		DamageSource source = new DamageSource(this.damageType(), attacker);
		float amount = amount().getFloat(context.forChild(".amount"));

		victim.hurtServer(serverLevel, source, amount);

	}

	@Override
	public void validate(Context.Validator validator) {
		Action.super.validate(validator);
		amount().validate(validator.forChild(".amount"));
		victim().validate(validator.forChild(".victim"));
		attacker().ifPresent(attacker -> attacker.validate(validator.forChild(".attacker")));
	}

}
