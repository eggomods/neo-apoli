package io.github.eggohito.neo_apoli.registry.context;

import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.parameter.*;

public final class NeoApoliContextParameters {

	public static final EntityContextParameter COMMAND_ENTITY = new EntityContextParameter(NeoApoli.id("command/entity"));
	public static final Vec3ContextParameter COMMAND_POSITION = new  Vec3ContextParameter(NeoApoli.id("command/position"));

	public static final EntityContextParameter ACTOR_ENTITY = new EntityContextParameter(NeoApoli.id("actor_entity"));
	public static final EntityContextParameter PROJECTILE_ENTITY = new EntityContextParameter(NeoApoli.id("projectile_entity"));
	public static final EntityContextParameter TARGET_ENTITY = new EntityContextParameter(NeoApoli.id("target_entity"));
	public static final EntityContextParameter THIS_ENTITY = new EntityContextParameter(NeoApoli.id("this_entity"));

	public static final DamageSourceContextParameter DEALT_DAMAGE_SOURCE = new DamageSourceContextParameter(NeoApoli.id("dealt_damage/source"));
	public static final DamageSourceContextParameter TAKEN_DAMAGE_SOURCE = new DamageSourceContextParameter(NeoApoli.id("taken_damage/source"));
	public static final FloatContextParameter DEALT_DAMAGE_AMOUNT = new FloatContextParameter(NeoApoli.id("dealt_damage/amount"));
	public static final FloatContextParameter TAKEN_DAMAGE_AMOUNT = new FloatContextParameter(NeoApoli.id("taken_damage/amount"));

	public static final EntityContextParameter DAMAGING_ENTITY = new EntityContextParameter(NeoApoli.id("damaging_entity"));
	public static final EntityContextParameter DIRECT_DAMAGING_ENTITY = new EntityContextParameter(NeoApoli.id("direct_damaging_entity"));

	public static final BlockContextParameter BROKEN_BLOCK = new BlockContextParameter(NeoApoli.id("broken_block"));
	public static final DirectionContextParameter BROKEN_SIDE = new DirectionContextParameter(NeoApoli.id("broken_side"));

	public static final BlockContextParameter MINING_BLOCK = new BlockContextParameter(NeoApoli.id("mining_block"));
	public static final BlockContextParameter SELECTED_BLOCK = new BlockContextParameter(NeoApoli.id("selected_block"));

	public static final EffectContextParameter APPLIED_EFFECT = new EffectContextParameter(NeoApoli.id("applied_effect"));

	public static final ItemContextParameter ITEM_IN_CONTAINER = new ItemContextParameter(NeoApoli.id("item_in_container"));
	public static final SlotContextParameter ITEM_IN_CONTAINER_SLOT = new SlotContextParameter(NeoApoli.id("item_in_container_slot"));
	public static final ItemContextParameter USED_ITEM = new ItemContextParameter(NeoApoli.id("used_item"));
	public static final SlotContextParameter USED_ITEM_SLOT = new SlotContextParameter(NeoApoli.id("used_item_slot"));

	public static void registerAll() {
		addPathAlias("actor", ACTOR_ENTITY);
		addPathAlias("projectile", PROJECTILE_ENTITY);
		addPathAlias("target", TARGET_ENTITY);
		addPathAlias("this", THIS_ENTITY);
	}

	public static <P extends ContextParameter<?>> void addPathAlias(String from, P to) {
		Context.ALIASES.getPaths().addAlias(from, to.name().getPath());
	}

}
