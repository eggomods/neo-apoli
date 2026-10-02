package io.github.eggohito.neo_apoli.context;

import io.github.eggohito.neo_apoli.util.MiscUtil;

import java.util.List;
import java.util.function.IntFunction;

public interface ContextValidatable {

	void validate(ContextValidator validator);

	static <U extends ContextValidatable> void validate(List<U> users, ContextValidator validator, IntFunction<String> pathFunction) {
		MiscUtil.iterateList(users, (index, user) -> user.validate(validator.forChild(pathFunction.apply(index))));
	}

}
