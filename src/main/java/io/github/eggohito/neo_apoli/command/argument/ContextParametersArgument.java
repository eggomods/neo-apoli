package io.github.eggohito.neo_apoli.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.eggohito.neo_apoli.context.ContextParameterMap;
import io.github.eggohito.neo_apoli.util.MiscUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;

//  TODO: Maybe improve parsing/suggestions for this argument type?
public record ContextParametersArgument(HolderLookup.Provider registries) implements ArgumentType<ContextParameterMap> {

	@Override
	public ContextParameterMap parse(StringReader reader) throws CommandSyntaxException {

		RegistryOps<Tag> ops = registries().createSerializationContext(NbtOps.INSTANCE);
		TagParser<Tag> parser = TagParser.create(ops);

		return ContextParameterMap.CODEC
			.parse(ops, parser.parseAsArgument(reader))
			.getOrThrow(error -> MiscUtil.createCommandException(Component.literal(error)));

	}

	public static ContextParametersArgument parameters(HolderLookup.Provider registries) {
		return new ContextParametersArgument(registries);
	}

	public static ContextParameterMap getParameters(CommandContext<CommandSourceStack> context, String name) {
		return context.getArgument(name, ContextParameterMap.class);
	}

}
