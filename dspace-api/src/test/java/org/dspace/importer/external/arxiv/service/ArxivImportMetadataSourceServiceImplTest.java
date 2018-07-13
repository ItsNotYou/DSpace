package org.dspace.importer.external.arxiv.service;

import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.dspace.importer.external.datamodel.ImportRecord;
import org.dspace.importer.external.datamodel.Query;
import org.dspace.importer.external.exception.MetadataSourceException;
import org.dspace.importer.external.metadatamapping.MetadatumDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/spring/spring-dspace-addon-import-services.xml", "file:../dspace/config/spring/api/pubmed-integration.xml", "file:../dspace/config/spring/api/arxiv-integration.xml" })
public class ArxivImportMetadataSourceServiceImplTest {

	@Autowired
	private ArxivImportMetadataSourceServiceImpl sut;

	@Before
	public void before() throws Exception {
		this.sut.init();
	}

	@Test
	public void shouldFindDavidWiljesReichCount() throws MetadataSourceException {
		int result = sut.getNbRecords("Interacting particle filters for simultaneous state and parameter estimation");
		assertEquals(1, result);
	}

	@Test
	public void shouldCreateRecords() throws MetadataSourceException {
		Collection<ImportRecord> results = sut.getRecords("Interacting particle filters for simultaneous state and parameter estimation", 0, 10);
		ImportRecord first = results.toArray(new ImportRecord[1])[0];

		Collection<MetadatumDTO> dcTitle = first.getValue("dc", "title", null);
		assertEquals(1, dcTitle.size());
		assertEquals("Interacting particle filters for simultaneous state and parameter estimation", get(dcTitle, 0).getValue());

		Collection<MetadatumDTO> dcIdUri = first.getValue("dc", "identifier", "other");
		assertEquals(1, dcIdUri.size());
		assertEquals("http://arxiv.org/abs/1709.09199v1", get(dcIdUri, 0).getValue());

		Collection<MetadatumDTO> dcAbstract = first.getValue("dc", "description", "abstract");
		assertEquals(1, dcAbstract.size());
		assertEquals("Simultaneous state and parameter estimation arises from various applicational areas but presents a major computational challenge. Most available Markov chain or sequential Monte Carlo techniques are applicable to relatively low dimensional problems only. Alternative methods, such as the ensemble Kalman filter or other ensemble transform filters have, on the other hand, been successfully applied to high dimensional state estimation problems. In this paper, we propose an extension of these techniques to high dimensional state space models which depend on a few unknown parameters. More specifically, we combine the ensemble Kalman-Bucy filter for the continuous-time filtering problem with a generalized ensemble transform particle filter for intermittent parameter updates. We demonstrate the performance of this two stage update filter for a wave equation with unknown wave velocity parameter.", get(dcAbstract, 0).getValue());

		Collection<MetadatumDTO> dcAuthors = first.getValue("dc", "contributor", "author");
		assertEquals(3, dcAuthors.size());
		assertEquals("David, Angwenyi", get(dcAuthors, 0).getValue());
		assertEquals("de Wiljes, Jana", get(dcAuthors, 1).getValue());
		assertEquals("Reich, Sebastian", get(dcAuthors, 2).getValue());

		assertEquals(3, dcAuthors.size());
		assertEquals("David, Angwenyi", get(dcAuthors, 0).getValue());
		assertEquals("de Wiljes, Jana", get(dcAuthors, 1).getValue());
		assertEquals("Reich, Sebastian", get(dcAuthors, 2).getValue());

		Collection<MetadatumDTO> dcIssued = first.getValue("dc", "date", "issued");
		assertEquals(1, dcIssued.size());
		assertEquals("2017-09-26", get(dcIssued, 0).getValue());

		Collection<MetadatumDTO> dcRelation = first.getValue("dc", "relation", "uri");
		assertEquals(2, dcRelation.size());
		assertEquals("http://arxiv.org/abs/1709.09199v1", get(dcRelation, 0).getValue());
		assertEquals("http://arxiv.org/pdf/1709.09199v1", get(dcRelation, 1).getValue());
	}

	@Test
	public void shouldCreateRecordsWithDoi() throws MetadataSourceException {
		Collection<ImportRecord> results = sut.getRecords("A Microscopic Model for Packet Transport in the Internet", 0, 10);
		ImportRecord first = results.toArray(new ImportRecord[1])[0];

		Collection<MetadatumDTO> dcIdDoi = first.getValue("dc", "identifier", "doi");
		assertEquals(1, dcIdDoi.size());
		assertEquals("10.1016/S0378-4371(01)00107-8", get(dcIdDoi, 0).getValue());
	}

	@Test
	public void shouldFindMatchingDavidWiljesReich() throws MetadataSourceException {
		Query query = new Query();
		query.addParameter("id", "http://arxiv.org/abs/1709.09199v1");

		Collection<ImportRecord> result = sut.findMatchingRecords(query);

		assertEquals(1, result.size());
		ImportRecord first = get(result, 0);
		assertEquals("Interacting particle filters for simultaneous state and parameter estimation", get(first.getValue("dc", "title", null), 0).getValue());
	}

	@Test
	public void shouldGetRecordDavidWiljesReich() throws MetadataSourceException {
		Query query = new Query();
		query.addParameter("id", "http://arxiv.org/abs/1709.09199v1");

		ImportRecord result = sut.getRecord(query);

		assertEquals("Interacting particle filters for simultaneous state and parameter estimation", get(result.getValue("dc", "title", null), 0).getValue());
	}

	private static <T> T get(Collection<T> col, int index) {
		return (T) col.toArray()[index];
	}
}
