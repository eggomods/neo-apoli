package io.github.eggohito.neo_apoli.action.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextUser;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.provider.custom.vec3.Vec3Provider;
import io.github.eggohito.neo_apoli.registry.NeoApoliActionTypes;
import io.github.eggohito.neo_apoli.util.CodecUtil;
import io.github.eggohito.neo_apoli.util.MapCodecUtil;
import io.github.eggohito.neo_apoli.util.Space;
import io.github.eggohito.neo_apoli.util.StreamCodecUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public record AddVelocityAction(Method method, Vec3Provider velocity) implements Action {

	public static final MapCodec<AddVelocityAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Method.CODEC.forGetter(AddVelocityAction::method),
		Vec3Provider.CODEC.fieldOf("velocity").forGetter(AddVelocityAction::velocity)
	).apply(instance, AddVelocityAction::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, AddVelocityAction> STREAM_CODEC = StreamCodec.composite(
		Method.STREAM_CODEC, AddVelocityAction::method,
		Vec3Provider.STREAM_CODEC, AddVelocityAction::velocity,
		AddVelocityAction::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliActionTypes.ADD_VELOCITY;
	}

	@Override
	public void execute(Context context) {
		velocity().getValue(context.forChild(".velocity"))
			.map(Vec3::toVector3f)
			.ifPresent(velocity -> method().apply(context, velocity));
	}

	@Override
	public void validate(ContextValidator validator) {
		Action.super.validate(validator);
		method().validate(validator);
		velocity().validate(validator.forChild(".velocity"));
	}

	//  TODO: Expose this interface for addon developers
	public sealed interface Method extends ContextUser {

		MapCodec<Method> CODEC = Type.CODEC.dispatchMap("method", Method::getType, Type::mapCodec);

		StreamCodec<RegistryFriendlyByteBuf, Method> STREAM_CODEC = Type.STREAM_CODEC.dispatch(Method::getType, Type::streamCodec);

		Type getType();

		void apply(Context context, Vector3f velocity);

		record SingleBased(Space space, EntityProvider entity) implements Method {

			public static final MapCodec<SingleBased> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Space.CODEC.optionalFieldOf("space", Space.LOCAL).forGetter(SingleBased::space),
				EntityProvider.CODEC.fieldOf("entity").forGetter(SingleBased::entity)
			).apply(instance, SingleBased::new));

			public static final StreamCodec<RegistryFriendlyByteBuf, SingleBased> STREAM_CODEC = StreamCodec.composite(
				Space.STREAM_CODEC, SingleBased::space,
				EntityProvider.STREAM_CODEC, SingleBased::entity,
				SingleBased::new
			);

			@Override
			public Type getType() {
				return Type.SINGLE;
			}

			@Override
			public void apply(Context context, Vector3f velocity) {
				entity().getValue(context.forChild(".entity")).ifPresent(entity -> this.onAdd(entity, velocity));
			}

			@Override
			public void validate(ContextValidator validator) {
				Method.super.validate(validator);
				entity().validate(validator.forChild(".entity"));
			}

			private void onAdd(@NotNull Entity entity, Vector3f velocity) {

				space().globalize(velocity, entity);

				entity.push(velocity.x(), velocity.y(), velocity.z());
				entity.hurtMarked = true;

			}

		}

		record PairBased(Reference reference, EntityProvider first, EntityProvider second) implements Method {

			@Override
			public Type getType() {
				return Type.PAIR;
			}

			@Override
			public void apply(Context context, Vector3f velocity) {

				Entity first = first().getValue(context.forChild(".first")).orElse(null);
				Entity second = second().getValue(context.forChild(".second")).orElse(null);

				if (first == null || second == null) {
					return;
				}

				Vector3f referenceVector = reference().apply(first, second).toVector3f();
				Space.transformVectorToBase(referenceVector, velocity, first.getViewYRot(1.0F), true);

				second.push(velocity.x(), velocity.y(), velocity.z());
				second.hurtMarked = true;

			}

			@Override
			public void validate(ContextValidator validator) {
				Method.super.validate(validator);
				first().validate(validator.forChild(".first"));
				second().validate(validator.forChild((".second")));
			}

		}

		@Accessors(fluent = true)
		@AllArgsConstructor
		@Getter
		enum Type {

			SINGLE(MapCodecUtil.lazy(() -> SingleBased.CODEC), StreamCodecUtil.lazy(() -> SingleBased.STREAM_CODEC)),
			PAIR(MapCodecUtil.lazy(() -> PairBased.CODEC), StreamCodecUtil.lazy(() -> PairBased.STREAM_CODEC));

			public static final Codec<Type> CODEC = CodecUtil.enumType(Type.class);
			public static final StreamCodec<RegistryFriendlyByteBuf, Type> STREAM_CODEC = StreamCodecUtil.enumType(Type.class);

			final MapCodec<? extends Method> mapCodec;
			final StreamCodec<RegistryFriendlyByteBuf, ? extends Method> streamCodec;

		}

	}

	public enum Reference {

		POSITION {

			@Override
			public Vec3 apply(Entity first, Entity second) {
				return second.position().subtract(first.position());
			}

		},

		ROTATION {

			@Override
			public Vec3 apply(Entity first, Entity second) {
				return first.getViewVector(1.0F);
			}

		};

		public abstract Vec3 apply(Entity first, Entity second);

	}


}
