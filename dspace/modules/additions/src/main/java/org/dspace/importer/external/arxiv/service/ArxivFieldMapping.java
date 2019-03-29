package org.dspace.importer.external.arxiv.service;

import java.util.Map;

import javax.annotation.Resource;

import org.dspace.importer.external.metadatamapping.AbstractMetadataFieldMapping;

public class ArxivFieldMapping extends AbstractMetadataFieldMapping {

	@Override
	@Resource(name = "arxivMetadataFieldMap")
	public void setMetadataFieldMap(Map metadataFieldMap) {
		super.setMetadataFieldMap(metadataFieldMap);
	}
}
