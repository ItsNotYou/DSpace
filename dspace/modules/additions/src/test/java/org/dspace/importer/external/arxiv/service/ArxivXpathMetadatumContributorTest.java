package org.dspace.importer.external.arxiv.service;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.util.Collection;

import javax.annotation.Resource;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.dspace.importer.external.metadatamapping.MetadatumDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/spring/spring-dspace-addon-import-services.xml", "file:../../config/spring/api/arxiv-integration.xml", "file:../../config/spring/api/pubmed-integration.xml" })
public class ArxivXpathMetadatumContributorTest {

	@Resource(name = "arxivSummaryContrib")
	private ArxivXpathMetadatumContributor sutForAbstract;

	@Resource(name = "arxivDoiContrib")
	private ArxivXpathMetadatumContributor sutForDoi;

	private OMElement content;

	@Before
	public void before() {
		String rawContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<entry xmlns=\"http://www.w3.org/2005/Atom\">\r\n  <summary>A $10^5$ size</summary>\r\n  <arxiv:doi xmlns:arxiv=\"http://arxiv.org/schemas/atom\">doi$10^5$doi</arxiv:doi>\r\n</entry>";

		OMXMLParserWrapper records = OMXMLBuilderFactory.createOMBuilder(new StringReader(rawContent));
		this.content = records.getDocumentElement();
	}

	@Test
	public void shouldMapMathJaxInAbstract() {
		Collection<MetadatumDTO> result = sutForAbstract.contributeMetadata(content);

		assertEquals(1, result.size());
		for (MetadatumDTO meta : result) {
			assertEquals("A \\(10^5\\) size", meta.getValue());
		}
	}

	@Test
	public void shouldNotMapMathJaxInDoi() {
		Collection<MetadatumDTO> result = sutForDoi.contributeMetadata(content);

		assertEquals(1, result.size());
		for (MetadatumDTO meta : result) {
			assertEquals("doi$10^5$doi", meta.getValue());
		}
	}
}
