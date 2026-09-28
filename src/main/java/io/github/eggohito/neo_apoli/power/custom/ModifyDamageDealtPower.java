package io.github.eggohito.neo_apoli.power.custom;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.modifier.Modifier;
import io.github.eggohito.neo_apoli.power.Power;
import io.github.eggohito.neo_apoli.power.custom.misc.DamageModifyingPower;
import io.github.eggohito.neo_apoli.registry.NeoApoliPowerTypes;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameters;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public record ModifyDamageDealtPower(Optional<Condition> activeCondition, List<Modifier> modifiers, Action onModifyAction) implements DamageModifyingPower {

	public static final MapCodec<ModifyDamageDealtPower> CODEC = DamageModifyingPower.codec(ModifyDamageDealtPower::new);
	public static final StreamCodec<RegistryFriendlyByteBuf, ModifyDamageDealtPower> STREAM_CODEC = DamageModifyingPower.streamCodec(ModifyDamageDealtPower::new);

	@Override
	public Type<?> getType() {
		return NeoApoliPowerTypes.MODIFY_DAMAGE_DEALT;
	}

	@Override
	public Power.Instance<?> createInstance() {
		return new Instance(this);
	}

	public static class Instance extends DamageModifyingPower.Instance<ModifyDamageDealtPower> {

		protected Instance(@NotNull ModifyDamageDealtPower power) {
			super(power);
		}

		@Override
		public Context createDamageContext(Entity actor, Entity target, DamageSource source, float amount) {
			return this.createHolderContextBuilder(actor)
				.withRequired(NeoApoliContextParameters.ACTOR_ENTITY, actor)
				.withRequired(NeoApoliContextParameters.TARGET_ENTITY, target)
				.withRequired(NeoApoliContextParameters.DEALT_DAMAGE_SOURCE, source)
				.withRequired(NeoApoliContextParameters.DEALT_DAMAGE_AMOUNT, amount)
				.withNullable(NeoApoliContextParameters.DAMAGING_ENTITY, source.getEntity())
				.withNullable(NeoApoliContextParameters.DIRECT_DAMAGING_ENTITY, source.getDirectEntity())
				.build(actor.level());
		}

	}

	public static float modify(@NotNull Entity actor, @NotNull Entity target, DamageSource source, float amount) {
		return DamageModifyingPower.modify(NeoApoliPowerTypes.MODIFY_DAMAGE_DEALT, Instance.class, actor, actor, target, source, amount);
	}

}
