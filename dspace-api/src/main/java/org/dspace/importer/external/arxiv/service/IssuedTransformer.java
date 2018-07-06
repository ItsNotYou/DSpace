package org.dspace.importer.external.arxiv.service;

import java.util.Calendar;
import java.util.function.Function;

import javax.xml.bind.DatatypeConverter;

public class IssuedTransformer implements Function<String, String> {

	@Override
	public String apply(String t) {
		Calendar issued = DatatypeConverter.parseDate(t);

		int year = issued.get(Calendar.YEAR);
		int month = issued.get(Calendar.MONTH) + 1;
		int day = issued.get(Calendar.DAY_OF_MONTH);

		return String.format("%04d-%02d-%02d", year, month, day);
	}
}
