package org.dspace.importer.external.arxiv.service;

import java.io.StringReader;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.regex.Pattern;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ItemService;
import org.dspace.importer.external.datamodel.ImportRecord;
import org.dspace.importer.external.datamodel.Query;
import org.dspace.importer.external.exception.MetadataSourceException;
import org.dspace.importer.external.metadatamapping.MetadatumDTO;
import org.dspace.importer.external.pubmed.service.PubmedImportMetadataSourceServiceImpl;
import org.dspace.importer.external.service.AbstractImportMetadataSourceService;
import org.jaxen.JaxenException;

public class ArxivImportMetadataSourceServiceImpl extends AbstractImportMetadataSourceService<OMElement> {

	private WebTarget arxivWebTarget;

	@Override
	public int getNbRecords(String query) throws MetadataSourceException {
		return retry(new GetNbRecords(query));
	}

	@Override
	public int getNbRecords(Query query) throws MetadataSourceException {
		return retry(new GetNbRecords(query));
	}

	@Override
	public Collection<ImportRecord> getRecords(String query, int start, int count) throws MetadataSourceException {
		return retry(new GetRecords(query, start, count));
	}

	@Override
	public Collection<ImportRecord> getRecords(Query query) throws MetadataSourceException {
		return retry(new GetRecords(query));
	}

	@Override
	public ImportRecord getRecord(String id) throws MetadataSourceException {
		return retry(new GetRecord(id));
	}

	@Override
	public ImportRecord getRecord(Query query) throws MetadataSourceException {
		return retry(new GetRecord(query));
	}

	@Override
	public String getImportSource() {
		return "http://export.arxiv.org/api/";
	}

	@Override
	public Collection<ImportRecord> findMatchingRecords(Item item) throws MetadataSourceException {
		return retry(new FindMatchingRecords(item));
	}

	@Override
	public Collection<ImportRecord> findMatchingRecords(Query query) throws MetadataSourceException {
		return retry(new FindMatchingRecords(query));
	}

	@Override
	public void init() throws Exception {
		Client client = ClientBuilder.newClient();
		arxivWebTarget = client.target("http://export.arxiv.org/api/query");
	}

	/**
	 * @see {@link PubmedImportMetadataSourceServiceImpl#getSingleElementValue(String, String)}
	 */
	private String getSingleElementValue(String src, String elementName) {
		OMXMLParserWrapper records = OMXMLBuilderFactory.createOMBuilder(new StringReader(src));
		OMElement element = records.getDocumentElement();
		String value = null;
		try {
			AXIOMXPath xpath = new AXIOMXPath("//" + elementName);
			xpath.addNamespace("opensearch", "http://a9.com/-/spec/opensearch/1.1/");

			List<OMElement> recordsList = xpath.selectNodes(element);
			if (!recordsList.isEmpty()) {
				value = recordsList.get(0).getText();
			}
		} catch (JaxenException e) {
			value = null;
		}
		return value;
	}

	/**
	 * @see {@link PubmedImportMetadataSourceServiceImpl#splitToRecords(String)}
	 */
	private List<OMElement> splitToRecords(String src) {
		OMXMLParserWrapper records = OMXMLBuilderFactory.createOMBuilder(new StringReader(src));
		OMElement element = records.getDocumentElement();
		try {
			AXIOMXPath xpath = new AXIOMXPath("//atom:entry");
			xpath.addNamespace("opensearch", "http://a9.com/-/spec/opensearch/1.1/");
			xpath.addNamespace("atom", "http://www.w3.org/2005/Atom");
			return xpath.selectNodes(element);
		} catch (JaxenException e) {
			return null;
		}
	}

	private Collection<MetadatumDTO> readTransformed(OMElement recordType, String xpathExpression, String schema, String element, String qualifier) throws JaxenException {
		return readTransformed(recordType, xpathExpression, schema, element, qualifier, new IdentityTransformer());
	}

	private Collection<MetadatumDTO> readTransformed(OMElement recordType, String xpathExpression, String schema, String element, String qualifier, Function<String, String> transformer) throws JaxenException {
		AXIOMXPath xpath = new AXIOMXPath(xpathExpression);
		xpath.addNamespace("opensearch", "http://a9.com/-/spec/opensearch/1.1/");
		xpath.addNamespace("atom", "http://www.w3.org/2005/Atom");
		xpath.addNamespace("arxiv", "http://arxiv.org/schemas/atom");

		List<MetadatumDTO> result = new LinkedList<>();
		for (Object e : xpath.selectNodes(recordType)) {
			// Supports XML elements and attributes
			String text = e instanceof OMAttribute ? ((OMAttribute) e).getAttributeValue() : ((OMElement) e).getText();

			// arXiv uses a lot of line breaks in its feed. Remove them!
			// Also remove leading and trailing spaces
			String trimmedText = text.replaceAll("[\\s\\r\\n]+", " ").replaceAll("^\\s*|\\s*$", "");
			// Apply transformer
			trimmedText = transformer.apply(trimmedText);

			MetadatumDTO meta = new MetadatumDTO();
			meta.setSchema(schema);
			meta.setElement(element);
			meta.setQualifier(qualifier);
			meta.setValue(trimmedText);
			result.add(meta);
		}
		return result;
	}

	@Override
	public ImportRecord transformSourceRecords(OMElement recordType) {
		List<MetadatumDTO> result = new LinkedList<>();
		try {
			result.addAll(readTransformed(recordType, ".//atom:title", "dc", "title", null));
			result.addAll(readTransformed(recordType, ".//atom:id", "dc", "identifier", "other"));
			result.addAll(readTransformed(recordType, ".//arxiv:doi", "dc", "identifier", "doi"));
			result.addAll(readTransformed(recordType, ".//atom:summary", "dc", "description", "abstract"));
			result.addAll(readTransformed(recordType, ".//atom:author/atom:name", "dc", "contributor", "author", new AuthorTransformer()));
			result.addAll(readTransformed(recordType, ".//atom:published", "dc", "date", "issued", new IssuedTransformer()));
			result.addAll(readTransformed(recordType, ".//atom:link/@href", "dc", "relation", "uri"));
		} catch (JaxenException e) {
			return null;
		}
		return new ImportRecord(result);
	}

	private class GetRecord implements Callable<ImportRecord> {

		private String id;

		public GetRecord(String id) {
			this.id = id;
		}

		public GetRecord(Query query) {
			this.id = query.getParameterAsClass("id", String.class);
		}

		@Override
		public ImportRecord call() throws Exception {
			if (id == null) {
				return null;
			}

			URI uri = URI.create(id);
			if (!uri.getHost().equals("arxiv.org")) {
				return null;
			}

			// Split at / and take last part
			String[] path = uri.getPath().split(Pattern.quote("/"));
			String arxivId = path[path.length - 1];

			WebTarget target = arxivWebTarget.queryParam("id_list", arxivId);
			String responseString = target.request("application/atom+xml;charset=UTF-8").get(String.class);

			List<OMElement> omElements = splitToRecords(responseString);

			List<ImportRecord> result = new LinkedList<>();
			for (OMElement record : omElements) {
				result.add(transformSourceRecords(record));
			}
			return result.isEmpty() ? null : result.get(0);
		}
	}

	private class FindMatchingRecords implements Callable<Collection<ImportRecord>> {

		private String id;

		public FindMatchingRecords(Query query) {
			id = query.getParameterAsClass("id", String.class);
		}

		public FindMatchingRecords(Item item) {
			ItemService itemService = ContentServiceFactory.getInstance().getItemService();
			List<MetadataValue> mv = itemService.getMetadata(item, "dc", "identifier", "other", Item.ANY);

			if (!mv.isEmpty()) {
				id = mv.get(0).getValue();
			}
		}

		@Override
		public Collection<ImportRecord> call() throws Exception {
			if (id == null) {
				return Collections.emptyList();
			}

			URI uri = URI.create(id);
			if (!uri.getHost().equals("arxiv.org")) {
				return Collections.emptyList();
			}

			// Split at / and take last part
			String[] path = uri.getPath().split(Pattern.quote("/"));
			String arxivId = path[path.length - 1];

			WebTarget target = arxivWebTarget.queryParam("id_list", arxivId);
			String responseString = target.request("application/atom+xml;charset=UTF-8").get(String.class);

			List<OMElement> omElements = splitToRecords(responseString);

			Collection<ImportRecord> result = new LinkedList<>();
			for (OMElement record : omElements) {
				result.add(transformSourceRecords(record));
			}
			return result;
		}
	}

	private class GetNbRecords implements Callable<Integer> {

		private Query query;

		private GetNbRecords(Query query) {
			this.query = query;
		}

		private GetNbRecords(String queryString) {
			this.query = new Query();
			this.query.addParameter("query", queryString);
		}

		@Override
		public Integer call() throws Exception {
			String queryString = query.getParameterAsClass("query", String.class);

			// Should result in
			// http://export.arxiv.org/api/query?search_query=all:%22some+search+term%22
			WebTarget target = arxivWebTarget.queryParam("search_query", String.format("all:\"%s\"", queryString));
			String responseString = target.request("application/atom+xml;charset=UTF-8").get(String.class);

			String count = getSingleElementValue(responseString, "opensearch:totalResults");
			return Integer.parseInt(count);
		}
	}

	private class GetRecords implements Callable<Collection<ImportRecord>> {

		private Query query;

		private GetRecords(Query query) {
			this.query = query;
		}

		private GetRecords(String queryString, int start, int count) {
			this.query = new Query();
			this.query.addParameter("query", queryString);
			this.query.addParameter("start", start);
			this.query.addParameter("count", count);
		}

		@Override
		public Collection<ImportRecord> call() throws Exception {
			String queryString = query.getParameterAsClass("query", String.class);
			Integer start = query.getParameterAsClass("start", Integer.class);
			Integer count = query.getParameterAsClass("count", Integer.class);

			if (count == null || count < 0) {
				count = 10;
			}

			if (start == null || start < 0) {
				start = 0;
			}

			// Should result in
			// http://export.arxiv.org/api/query?search_query=all:%22some+search+term%22&start=0&max_results=10
			WebTarget target = arxivWebTarget.queryParam("search_query", String.format("all:\"%s\"", queryString)).queryParam("start", start).queryParam("max_results", count);
			String responseString = target.request("application/atom+xml;charset=UTF-8").get(String.class);

			List<OMElement> omElements = splitToRecords(responseString);

			Collection<ImportRecord> result = new LinkedList<>();
			for (OMElement record : omElements) {
				result.add(transformSourceRecords(record));
			}
			return result;
		}
	}
}
