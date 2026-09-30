package io.github.eggohito.neo_apoli.provider.custom.nbt;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.item.ItemProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliNbtProviderTypes;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record ItemNbtProvider(ItemProvider item) implements NbtProvider {

	public static final MapCodec<ItemNbtProvider> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(ItemProvider.CODEC.fieldOf("item").forGetter(ItemNbtProvider::item))
		.apply(instance, ItemNbtProvider::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ItemNbtProvider> STREAM_CODEC = StreamCodec.composite(
		ItemProvider.STREAM_CODEC, ItemNbtProvider::item,
		ItemNbtProvider::new
	);

	@Override
	public @NotNull NbtProvider.Type<?> getType() {
		return NeoApoliNbtProviderTypes.ITEM;
	}

	@Override
	public Optional<Tag> getValue(Context context) {
		RegistryOps<Tag> ops = context.level().registryAccess().createSerializationContext(NbtOps.INSTANCE);
		return item().getValue(context.forChild(".item"))
			.flatMap(item -> ItemStack.OPTIONAL_CODEC.encodeStart(ops, item)
				.mapError(error -> "Error providing item as NBT: " + error)
				.resultOrPartial(context::reportProblem));
	}

	@Override
	public void validate(ContextValidator validator) {
		NbtProvider.super.validate(validator);
		item().validate(validator.forChild(".item"));
	}

}
