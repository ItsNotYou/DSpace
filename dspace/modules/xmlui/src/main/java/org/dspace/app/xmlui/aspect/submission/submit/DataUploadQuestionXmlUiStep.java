package org.dspace.app.xmlui.aspect.submission.submit;

import static org.dspace.submit.step.DataUploadQuestionStep.BASED_ON_CODE;
import static org.dspace.submit.step.DataUploadQuestionStep.BASED_ON_DATA;
import static org.dspace.submit.step.DataUploadQuestionStep.BASED_ON_TEXT;
import static org.dspace.submit.step.DataUploadQuestionStep.METADATA_BASED_ON;
import static org.dspace.submit.step.DataUploadQuestionStep.METADATA_COMMENT;
import static org.dspace.submit.step.DataUploadQuestionStep.METADATA_UPLOAD_STATE;
import static org.dspace.submit.step.DataUploadQuestionStep.PARAMETER_BASED_ON;
import static org.dspace.submit.step.DataUploadQuestionStep.PARAMETER_COMMENT;
import static org.dspace.submit.step.DataUploadQuestionStep.PARAMETER_UPLOAD_STATE;
import static org.dspace.submit.step.DataUploadQuestionStep.UPLOAD_STATE_COMPLETE;
import static org.dspace.submit.step.DataUploadQuestionStep.UPLOAD_STATE_INCOMPLETE;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.cocoon.ProcessingException;
import org.dspace.app.xmlui.aspect.submission.AbstractSubmissionStep;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.Body;
import org.dspace.app.xmlui.wing.element.CheckBox;
import org.dspace.app.xmlui.wing.element.Division;
import org.dspace.app.xmlui.wing.element.Item;
import org.dspace.app.xmlui.wing.element.List;
import org.dspace.app.xmlui.wing.element.Radio;
import org.dspace.app.xmlui.wing.element.TextArea;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.MetadataValue;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ItemService;
import org.xml.sax.SAXException;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

public class DataUploadQuestionXmlUiStep extends AbstractSubmissionStep {

	private static final String REVIEW_HEAD = "Check File Completeness";

	private static final String LABEL_BASED_ON = "Item scope";
	private static final String LABEL_UPLOAD_STATE = "Uploaded artifacts";

	private static final String DESCRIPTION_COMPLETE = "complete: all artifacts mentioned above were uploaded.";
	private static final String DESCRIPTION_INCOMPLETE = "incomplete: some artifacts mentioned above were not uploaded, a reason is provided below.";

	protected static final Message T_required_field = message("xmlui.Submission.submit.DescribeStep.required_field");

	protected ItemService itemService = ContentServiceFactory.getInstance().getItemService();

	@Override
	public void addBody(Body body) throws SAXException, WingException, UIException, SQLException, IOException, AuthorizeException, ProcessingException {
		// Obtain the inputs (i.e. metadata fields we are going to display)
		Collection collection = submission.getCollection();
		String actionURL = contextPath + "/handle/" + collection.getHandle() + "/submit/" + knot.getId() + ".continue";

		Division div = body.addInteractiveDivision("submit-data-completeness", actionURL, Division.METHOD_POST, "primary submission");
		div.setHead(T_submission_head);
		addSubmissionProgressList(div);

		Division inner = div.addDivision("submit-data-completeness-inner");
		inner.setHead(REVIEW_HEAD);
		List controls = inner.addList("submit-data-completeness-list", List.TYPE_FORM);

		// Input for "Based on"
		controls.addLabel(LABEL_BASED_ON);
		Item base = controls.addItem();
		base.addContent("Check all content types this item is based on. If you only use simulation data that you create and use within a script, please leave the 'data' box unchecked.");
		renderCheckBox(base, METADATA_BASED_ON, PARAMETER_BASED_ON, BASED_ON_TEXT, "text (e.g. article)");
		renderCheckBox(base, METADATA_BASED_ON, PARAMETER_BASED_ON, BASED_ON_DATA, "data (e.g. experimental data)");
		renderCheckBox(base, METADATA_BASED_ON, PARAMETER_BASED_ON, BASED_ON_CODE, "code (e.g. scripts, programs)");

		// Input for "Is complete" and "Comment if incomplete"
		controls.addLabel(LABEL_UPLOAD_STATE);
		Item complete = controls.addItem();
		complete.addContent("Specify whether all artifacts mentioned in 'Item scope' were uploaded. Please provide a reason if some artifacts were not uploaded.");
		renderRadio(complete, METADATA_UPLOAD_STATE, PARAMETER_UPLOAD_STATE, UPLOAD_STATE_COMPLETE, DESCRIPTION_COMPLETE);
		Radio radio = renderRadio(complete, METADATA_UPLOAD_STATE, PARAMETER_UPLOAD_STATE, UPLOAD_STATE_INCOMPLETE, DESCRIPTION_INCOMPLETE);
		TextArea text = renderTextArea(complete, METADATA_COMMENT, PARAMETER_COMMENT);

		// Add error messages if necessary
		if (isFieldInError(PARAMETER_UPLOAD_STATE)) {
			radio.addError(T_required_field);
		}
		if (isFieldInError(PARAMETER_COMMENT)) {
			text.addError(T_required_field);
		}

		// add standard control/paging buttons
		addControlButtons(controls);
	}

	@Override
	public List addReviewSection(List reviewList) throws SAXException, WingException, UIException, SQLException, IOException, AuthorizeException {
		// Create a new list section for this step (and set its heading)
		List describeSection = reviewList.addList("submit-review-" + this.stepAndPage, List.TYPE_FORM);
		describeSection.setHead(REVIEW_HEAD);

		// Show base
		describeSection.addLabel(LABEL_BASED_ON);
		java.util.List<String> bases = new ArrayList<String>();
		for (String key : new String[] { BASED_ON_TEXT, BASED_ON_DATA, BASED_ON_CODE }) {
			if (isInMetadata(METADATA_BASED_ON, key)) {
				bases.add(key);
			}
		}
		describeSection.addItem(Joiner.on(", ").join(bases));

		// Show upload state
		describeSection.addLabel(LABEL_UPLOAD_STATE);
		if (isInMetadata(METADATA_UPLOAD_STATE, UPLOAD_STATE_COMPLETE)) {
			describeSection.addItem(DESCRIPTION_COMPLETE);
		} else if (isInMetadata(METADATA_UPLOAD_STATE, UPLOAD_STATE_INCOMPLETE)) {
			describeSection.addItem(DESCRIPTION_INCOMPLETE);
		}

		// Show comment
		String comment = getFirstMetadata(METADATA_COMMENT, null);
		if (!Strings.isNullOrEmpty(comment)) {
			describeSection.addItem(comment);
		}

		return describeSection;
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

	private java.util.List<MetadataValue> getMetadata(String[] metaField) {
		org.dspace.content.Item item = submission.getItem();
		java.util.List<MetadataValue> values = itemService.getMetadata(item, metaField[0], metaField[1], metaField[2], org.dspace.content.Item.ANY);
		return values;
	}

	private boolean isInMetadata(String[] metaField, String value) {
		java.util.List<MetadataValue> values = getMetadata(metaField);
		for (MetadataValue mv : values) {
			if (value.equals(mv.getValue())) {
				return true;
			}
		}
		return false;
	}

	private String getFirstMetadata(String[] metaField, String defaultValue) {
		java.util.List<MetadataValue> values = getMetadata(metaField);
		if (values.size() >= 1) {
			return values.get(0).getValue();
		} else {
			return defaultValue;
		}
	}

	private TextArea renderTextArea(Item ui, String[] metaField, String parameterName) throws WingException {
		TextArea text = ui.addTextArea(parameterName);
		text.setValue(getFirstMetadata(metaField, ""));
		return text;
	}

	private Radio renderRadio(Item ui, String[] metaField, String parameterName, String parameterValue, String description) throws WingException {
		Radio radio = ui.addRadio(parameterName);
		radio.addOption(isInMetadata(metaField, parameterValue), parameterValue, description);
		return radio;
	}

	private CheckBox renderCheckBox(Item ui, String[] metaField, String parameterName, String parameterValue, String description) throws WingException {
		CheckBox checkbox = ui.addCheckBox(parameterName);
		checkbox.addOption(isInMetadata(metaField, parameterValue), parameterValue, description);
		return checkbox;
	}
}
