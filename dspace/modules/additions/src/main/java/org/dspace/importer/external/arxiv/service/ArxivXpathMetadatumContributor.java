package org.dspace.importer.external.arxiv.service;

import java.util.Collection;

import org.apache.axiom.om.OMElement;
import org.dspace.importer.external.metadatamapping.MetadatumDTO;
import org.dspace.importer.external.metadatamapping.contributor.SimpleXpathMetadatumContributor;

public class ArxivXpathMetadatumContributor extends SimpleXpathMetadatumContributor {

	private boolean mathJaxMapped = false;

	public boolean isMathJaxMapped() {
		return mathJaxMapped;
	}

	public void setMathJaxMapped(boolean mathJaxMapped) {
		this.mathJaxMapped = mathJaxMapped;
	}

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
			if (value != null && mathJaxMapped) {
				// arXiv uses $...$ as MathJax delimiters. From DSpace 6.3 onwards, MathJax
				// expects \(...\) for in-line mathematics and $$...$$ for displayed
				// mathematics. See https://jira.duraspace.org/browse/DS-3087 for details
				int dollarIndex = -1;
				while ((dollarIndex = findNext(value, '$', dollarIndex + 1)) != -1) {
					int nextIndex = findNext(value, '$', dollarIndex + 1);
					if (nextIndex != -1) {
						value = value.substring(0, dollarIndex) + "\\(" + value.substring(dollarIndex + 1, nextIndex) + "\\)" + value.substring(nextIndex + 1);
						dollarIndex = nextIndex + 2;
					}
				}
			}
			dto.setValue(value);
		}

		return result;
	}

	private int findNext(String value, int sign, int startFrom) {
		int signIndex = value.indexOf('$', startFrom);
		if (signIndex != -1) {
			// Check for double sign
			int nextIndex = findNext(value, sign, signIndex + 1);
			if (nextIndex == signIndex + 1) {
				// Skip double sign
				return findNext(value, sign, signIndex + 2);
			}

			// Single sign confirmed
			return signIndex;
		} else {
			return -1;
		}
	}
}
