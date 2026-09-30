package io.github.eggohito.neo_apoli.hud.element;

import com.mojang.datafixers.util.Function7;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.codec.MultiAlternativeCodec;
import io.github.eggohito.neo_apoli.color.Color;
import io.github.eggohito.neo_apoli.color.custom.Argb;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.hud.Sprite;
import io.github.eggohito.neo_apoli.provider.custom.bool.BooleanProvider;
import io.github.eggohito.neo_apoli.provider.custom.bool.ConstantBooleanProvider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public interface OverlayHudElement extends HudElement {

	ResourceLocation ATLAS_NAME = NeoApoli.id("overlay");

	ResourceLocation ATLAS_SHEET = ATLAS_NAME.withPath(path -> "textures/atlas/" + path + ".png");

	Codec<Sprite> SPRITE_CODEC = new MultiAlternativeCodec<>(Sprite.CODEC, ResourceLocation.CODEC.xmap(id -> new Sprite(ATLAS_SHEET, id), Sprite::id));

	Sprite sprite();

	Color color();

	RenderPhase renderPhase();

	BooleanProvider shouldRender();

	BooleanProvider hideWithHud();

	BooleanProvider visibleInThirdPerson();

	@Override
	default boolean shouldRender(Context context, RenderPhase renderPhase) {
		return this.renderPhase() == renderPhase
			&& this.shouldRender().getBoolean(context.forChild(".should_render"));
	}

	@Override
	default boolean hideWithHud(Context context) {
		return hideWithHud().getBoolean(context.forChild(".hide_with_hud"));
	}

	@Override
	default void validate(ContextValidator validator) {
		HudElement.super.validate(validator);
		color().validate(validator.forChild(".color"));
		shouldRender().validate(validator.forChild(".should_render"));
		hideWithHud().validate(validator.forChild(".hide_with_hud"));
		visibleInThirdPerson().validate(validator.forChild(".visible_in_third_person"));
	}

	static <H extends OverlayHudElement> MapCodec<H> createCommonOverlayCodec(Function7<Sprite, Color, RenderPhase, BooleanProvider, BooleanProvider, BooleanProvider, Integer, H> constructor) {
		return RecordCodecBuilder.mapCodec(instance -> instance.group(
			SPRITE_CODEC.fieldOf("sprite").forGetter(OverlayHudElement::sprite),
			Color.CODEC.optionalFieldOf("color", Argb.DEFAULT).forGetter(OverlayHudElement::color),
			RenderPhase.CODEC.fieldOf("render_phase").forGetter(OverlayHudElement::renderPhase),
			BooleanProvider.CODEC.optionalFieldOf("should_render", new ConstantBooleanProvider(true)).forGetter(OverlayHudElement::shouldRender),
			BooleanProvider.CODEC.optionalFieldOf("hide_with_hud", new ConstantBooleanProvider(true)).forGetter(OverlayHudElement::hideWithHud),
			BooleanProvider.CODEC.optionalFieldOf("visible_in_third_person", new ConstantBooleanProvider(true)).forGetter(OverlayHudElement::visibleInThirdPerson),
			Codec.INT.optionalFieldOf("order", 0).forGetter(OverlayHudElement::order)
		).apply(instance, constructor));
	}

	static <H extends OverlayHudElement> StreamCodec<RegistryFriendlyByteBuf, H> createCommonOverlayStreamCodec(Function7<Sprite, Color, RenderPhase, BooleanProvider, BooleanProvider, BooleanProvider, Integer, H> constructor) {
		return StreamCodec.composite(
			Sprite.STREAM_CODEC, OverlayHudElement::sprite,
			Color.STREAM_CODEC, OverlayHudElement::color,
			RenderPhase.STREAM_CODEC, OverlayHudElement::renderPhase,
			BooleanProvider.STREAM_CODEC, OverlayHudElement::shouldRender,
			BooleanProvider.STREAM_CODEC, OverlayHudElement::hideWithHud,
			BooleanProvider.STREAM_CODEC, OverlayHudElement::visibleInThirdPerson,
			ByteBufCodecs.INT, OverlayHudElement::order,
			constructor
		);
	}

}
