package org.dspace.importer.external.arxiv.service;

import java.util.Calendar;
import java.util.Collection;
import java.util.function.Consumer;

import javax.xml.bind.DatatypeConverter;

import org.apache.axiom.om.OMElement;
import org.dspace.importer.external.metadatamapping.MetadatumDTO;

public class ArxivIssuedMetadatumContributor extends ArxivXpathMetadatumContributor {

	@Override
	public Collection<MetadatumDTO> contributeMetadata(OMElement t) {
		Collection<MetadatumDTO> result = super.contributeMetadata(t);
		result.forEach(new AsSimpleDate());
		return result;
	}

	/**
	 * Converts from "2017-09-26T18:07:24Z" to "2017-09-26"
	 */
	private class AsSimpleDate implements Consumer<MetadatumDTO> {

		@Override
		public void accept(MetadatumDTO meta) {
			String t = meta.getValue();

			Calendar issued = DatatypeConverter.parseDate(t);

			int year = issued.get(Calendar.YEAR);
			int month = issued.get(Calendar.MONTH) + 1;
			int day = issued.get(Calendar.DAY_OF_MONTH);

			meta.setValue(String.format("%04d-%02d-%02d", year, month, day));
		}
	}
}
