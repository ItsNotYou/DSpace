package org.dspace.importer.external.arxiv.service;

import java.util.Collection;
import java.util.function.Consumer;

import org.apache.axiom.om.OMElement;
import org.dspace.importer.external.metadatamapping.MetadatumDTO;

public class ArxivAuthorMetadatumContributor extends ArxivXpathMetadatumContributor {

	@Override
	public Collection<MetadatumDTO> contributeMetadata(OMElement t) {
		Collection<MetadatumDTO> result = super.contributeMetadata(t);
		result.forEach(new AsDSpaceAuthor());
		return result;
	}

	/**
	 * Converts from "<first name> <last name>" to "<last name>, <first name>"
	 */
	private class AsDSpaceAuthor implements Consumer<MetadatumDTO> {

		@Override
		public void accept(MetadatumDTO meta) {
			String t = meta.getValue();

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

			meta.setValue(result.toString().trim());
		}
	}
}
