package org.dspace.importer.external.arxiv.service;

import java.util.function.Function;

public class IdentityTransformer implements Function<String, String> {

	@Override
	public String apply(String t) {
		return t;
	}
}
