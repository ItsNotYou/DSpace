package org.dspace.app.xmlui.aspect.submission.submit;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.cocoon.ProcessingException;
import org.dspace.app.xmlui.aspect.submission.AbstractSubmissionStep;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.Body;
import org.dspace.app.xmlui.wing.element.Division;
import org.dspace.app.xmlui.wing.element.Item;
import org.dspace.app.xmlui.wing.element.List;
import org.dspace.app.xmlui.wing.element.Radio;
import org.dspace.app.xmlui.wing.element.TextArea;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.submit.step.DataUploadQuestionStep;
import org.xml.sax.SAXException;

public class DataUploadQuestionXmlUiStep extends AbstractSubmissionStep {

	private static final String REVIEW_HEAD = "Check File Completeness";

	protected static final Message T_required_field = message("xmlui.Submission.submit.DescribeStep.required_field");

	@Override
	public void addBody(Body body) throws SAXException, WingException, UIException, SQLException, IOException, AuthorizeException, ProcessingException {
		Collection collection = submission.getCollection();
		String actionURL = contextPath + "/handle/" + collection.getHandle() + "/submit/" + knot.getId() + ".continue";

		Division div = body.addInteractiveDivision("submit-data-completeness", actionURL, Division.METHOD_POST, "primary submission");
		div.setHead(T_submission_head);
		addSubmissionProgressList(div);

		Division inner = div.addDivision("submit-data-completeness-inner");
		inner.setHead(REVIEW_HEAD);
		List controls = inner.addList("submit-data-completeness-list", List.TYPE_FORM);

		// Input for "Based on"
		controls.addLabel("Item scope");
		Item base = controls.addItem();
		base.addContent("Check all content types this item is based on. If you only use simulation data that you create and use within a script, please leave the 'data' box unchecked.");
		base.addCheckBox(DataUploadQuestionStep.PARAMETER_BASED_ON).addOption(true, DataUploadQuestionStep.BASED_ON_TEXT, "text (e.g. article)");
		base.addCheckBox(DataUploadQuestionStep.PARAMETER_BASED_ON).addOption(DataUploadQuestionStep.BASED_ON_DATA, "data (e.g. experimental data)");
		base.addCheckBox(DataUploadQuestionStep.PARAMETER_BASED_ON).addOption(DataUploadQuestionStep.BASED_ON_CODE, "code (e.g. scripts, programs)");

		// Input for "Is complete" and "Comment if incomplete"
		controls.addLabel("Uploaded artifacts");
		Item complete = controls.addItem();
		complete.addContent("Specify whether all artifacts mentioned in 'Item scope' were uploaded. Please provide a reason if some artifacts were not uploaded.");
		// Radio buttons
		{
			complete.addRadio(DataUploadQuestionStep.PARAMETER_UPLOAD_STATE).addOption(DataUploadQuestionStep.UPLOAD_STATE_COMPLETE, "complete: all artifacts mentioned above were uploaded.");
			Radio radio = complete.addRadio(DataUploadQuestionStep.PARAMETER_UPLOAD_STATE);
			radio.addOption(DataUploadQuestionStep.UPLOAD_STATE_INCOMPLETE, "incomplete: some artifacts mentioned above were not uploaded, a reason is provided below.");
			// Add error message if necessary
			if (isFieldInError(DataUploadQuestionStep.PARAMETER_UPLOAD_STATE)) {
				radio.addError(T_required_field);
			}
		}
		// Comment box
		{
			TextArea text = complete.addTextArea(DataUploadQuestionStep.PARAMETER_COMMENT);
			// Add error message if necessary
			if (isFieldInError(DataUploadQuestionStep.PARAMETER_COMMENT)) {
				text.addError(T_required_field);
			}
		}

		// add standard control/paging buttons
		addControlButtons(controls);
	}

	/**
	 * Check if the given fieldname is listed as being in error.
	 *
	 * @param fieldName
	 * @return
	 */
	private boolean isFieldInError(String fieldName) {
		return (this.errorFields.contains(fieldName));
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
