package org.dspace.importer.external.arxiv.service;

import java.util.Collection;

import org.apache.axiom.om.OMElement;
import org.dspace.importer.external.metadatamapping.MetadatumDTO;
import org.dspace.importer.external.metadatamapping.contributor.SimpleXpathMetadatumContributor;

public class ArxivXpathMetadatumContributor extends SimpleXpathMetadatumContributor {

	@Override
	public Collection<MetadatumDTO> contributeMetadata(OMElement t) {
		Collection<MetadatumDTO> result = super.contributeMetadata(t);

		for (MetadatumDTO dto : result) {
			String value = dto.getValue();
			if (value != null) {
				// arXiv uses a lot of line breaks in its feed. Remove them!
				// Also remove leading and trailing spaces
				value = value.replaceAll("[\\s\\r\\n]+", " ").replaceAll("^\\s*|\\s*$", "");
			}
			dto.setValue(value);
		}

		return result;
	}
}
