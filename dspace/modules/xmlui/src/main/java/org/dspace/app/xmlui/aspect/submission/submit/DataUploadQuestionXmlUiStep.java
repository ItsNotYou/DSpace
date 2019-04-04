package org.dspace.app.xmlui.aspect.submission.submit;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.cocoon.ProcessingException;
import org.dspace.app.xmlui.aspect.submission.AbstractSubmissionStep;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.Body;
import org.dspace.app.xmlui.wing.element.Division;
import org.dspace.app.xmlui.wing.element.List;
import org.dspace.app.xmlui.wing.element.Radio;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.submit.step.DataUploadQuestionStep;
import org.xml.sax.SAXException;

public class DataUploadQuestionXmlUiStep extends AbstractSubmissionStep {

	private static final String DESCRIPTIVE_MESSAGE = "Bla bla bla describe this step";
	private static final String REVIEW_HEAD = "Check File Completeness";

	private static final String UPLOAD_NONEUSED = "The item is not based on code or data.";
	private static final String UPLOAD_CODEONLY = "The item is only based on code and the code is uploaded.";
	private static final String UPLOAD_DATAONLY = "The item is only based on data and the data is uploaded.";
	private static final String UPLOAD_CODEANDDATA = "The item is based on code and data. Both are uploaded.";
	private static final String UPLOAD_MISSINGARTIFACT = "The item is based on code or data but at least one of these is not uploaded.";

	@Override
	public void addBody(Body body) throws SAXException, WingException, UIException, SQLException, IOException, AuthorizeException, ProcessingException {
		Collection collection = submission.getCollection();
		String actionURL = contextPath + "/handle/" + collection.getHandle() + "/submit/" + knot.getId() + ".continue";

		Division div = body.addInteractiveDivision("submit-data-completeness", actionURL, Division.METHOD_POST, "primary submission");
		div.setHead(T_submission_head);
		addSubmissionProgressList(div);

		Division inner = div.addDivision("submit-data-completeness-inner");
		inner.setHead(REVIEW_HEAD);
		inner.addPara(DESCRIPTIVE_MESSAGE);

		// Show different options
		List controls = inner.addList("submit-data-completeness-list", List.TYPE_FORM);

		addUploadOption(controls, DataUploadQuestionStep.UPLOAD_NONEUSED, UPLOAD_NONEUSED);
		addUploadOption(controls, DataUploadQuestionStep.UPLOAD_CODEONLY, UPLOAD_CODEONLY);
		addUploadOption(controls, DataUploadQuestionStep.UPLOAD_DATAONLY, UPLOAD_DATAONLY);
		addUploadOption(controls, DataUploadQuestionStep.UPLOAD_CODEANDDATA, UPLOAD_CODEANDDATA);
		addUploadOption(controls, DataUploadQuestionStep.UPLOAD_MISSINGARTIFACT, UPLOAD_MISSINGARTIFACT);

		// add standard control/paging buttons
		addControlButtons(controls);
	}

	private void addUploadOption(List controls, String option, String message) throws WingException {
		Radio decision = controls.addItem().addRadio(DataUploadQuestionStep.PARAMETER_NAME);
		decision.addOption(option, message);
	}

	@Override
	public List addReviewSection(List reviewList) throws SAXException, WingException, UIException, SQLException, IOException, AuthorizeException {
		List uploadSection = reviewList.addList("submit-review-" + this.stepAndPage, List.TYPE_FORM);
		uploadSection.setHead(REVIEW_HEAD);

		uploadSection.addItem("The item doesn't contain code");

		// TODO Auto-generated method stub

		return uploadSection;
	}
}
