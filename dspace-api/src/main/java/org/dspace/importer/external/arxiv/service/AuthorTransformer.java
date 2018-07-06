package org.dspace.importer.external.arxiv.service;

import java.util.function.Function;

public class AuthorTransformer implements Function<String, String> {

	@Override
	public String apply(String t) {
		String[] names = t.split("\\s");
		int lastNameStartIndex = names.length - 1;

		// Split on lower case name
		for (int count = 0; count < lastNameStartIndex; count++) {
			if (Character.isLowerCase(names[count].charAt(0))) {
				lastNameStartIndex = count;
			}
		}

		// Copy last name followed by first name
		StringBuilder result = new StringBuilder();
		for (int count = lastNameStartIndex; count < names.length; count++) {
			result.append(" ");
			result.append(names[count]);
		}
		result.append(",");
		for (int count = 0; count < lastNameStartIndex; count++) {
			result.append(" ");
			result.append(names[count]);
		}

		return result.toString().trim();
	}
}
