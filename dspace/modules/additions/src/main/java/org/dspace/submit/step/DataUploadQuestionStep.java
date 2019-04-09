package org.dspace.submit.step;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dspace.app.util.SubmissionInfo;
import org.dspace.app.util.Util;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Item;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.core.Context;
import org.dspace.submit.AbstractProcessingStep;

public class DataUploadQuestionStep extends AbstractProcessingStep {

	// is the publication based on text, data or code?
	public static final String BASED_ON_TEXT = "text";
	public static final String BASED_ON_DATA = "data";
	public static final String BASED_ON_CODE = "code";

	// is everything uploaded?
	public static final String UPLOAD_STATE_COMPLETE = "artifacts_complete";
	public static final String UPLOAD_STATE_INCOMPLETE = "artifacts_missing";

	// form parameter names
	public static final String PARAMETER_BASED_ON = "based_on";
	public static final String PARAMETER_UPLOAD_STATE = "upload_state";
	public static final String PARAMETER_COMMENT = "comment";

	/**
	 * Item base. Should be used to indicate which parts (text, data, code) this
	 * item consists of.
	 * 
	 * @see #BASED_ON_TEXT
	 * @see #BASED_ON_DATA
	 * @see #BASED_ON_CODE
	 */
	public static final String[] METADATA_BASED_ON = { "crc", "upload", "base" };

	/**
	 * Upload state. Should be used to say whether all item parts were uploaded or
	 * whether an item is incomplete.
	 * 
	 * @see #UPLOAD_STATE_COMPLETE
	 */
	public static final String[] METADATA_UPLOAD_STATE = { "crc", "upload", "state" };

	/**
	 * Comment on the upload state. Should be used to explain why an artifact is
	 * missing.
	 */
	public static final String[] METADATA_COMMENT = { "crc", "upload", "comment" };

	// the metadata language qualifier
	public static final String LANGUAGE_QUALIFIER = "en";

	/***************************************************************************
	 * STATUS / ERROR FLAGS (returned by doProcessing() if an error occurs or
	 * additional user interaction may be required)
	 * 
	 * (Do NOT use status of 0, since it corresponds to STATUS_COMPLETE flag defined
	 * in the JSPStepManager class)
	 **************************************************************************/
	/**
	 * there were required fields that were not filled out
	 */
	public static final int STATUS_MISSING_REQUIRED_FIELDS = 2;

	@Override
	public int doProcessing(Context context, HttpServletRequest request, HttpServletResponse response, SubmissionInfo subInfo) throws ServletException, IOException, SQLException, AuthorizeException {
		// get button user pressed and reference to item
		String buttonPressed = Util.getSubmitButton(request, NEXT_BUTTON);
		Item item = subInfo.getSubmissionItem().getItem();

		// Step 1:
		// clear out all item metadata defined on this page
		itemService.clearMetadata(context, item, METADATA_BASED_ON[0], METADATA_BASED_ON[1], METADATA_BASED_ON[2], Item.ANY);
		itemService.clearMetadata(context, item, METADATA_UPLOAD_STATE[0], METADATA_UPLOAD_STATE[1], METADATA_UPLOAD_STATE[2], Item.ANY);
		itemService.clearMetadata(context, item, METADATA_COMMENT[0], METADATA_COMMENT[1], METADATA_COMMENT[2], Item.ANY);

		// Clear required-field errors first
		clearErrorFields(request);

		// Step 2:
		// now update the item metadata

		// Choosing an upload base means selecting one or more options
		String[] base = request.getParameterValues(PARAMETER_BASED_ON);
		if (base != null) {
			itemService.addMetadata(context, item, METADATA_BASED_ON[0], METADATA_BASED_ON[1], METADATA_BASED_ON[2], LANGUAGE_QUALIFIER, Arrays.asList(base));
		}
		// Choosing an upload state means selecting one option
		String state = request.getParameter(PARAMETER_UPLOAD_STATE);
		if (state != null) {
			itemService.addMetadata(context, item, METADATA_UPLOAD_STATE[0], METADATA_UPLOAD_STATE[1], METADATA_UPLOAD_STATE[2], LANGUAGE_QUALIFIER, state);
		}
		// Giving a comment means entering some free text
		String comment = request.getParameter(PARAMETER_COMMENT);
		if (comment != null && !comment.isEmpty()) {
			itemService.addMetadata(context, item, METADATA_COMMENT[0], METADATA_COMMENT[1], METADATA_COMMENT[2], LANGUAGE_QUALIFIER, comment);
		}

		// Step 3:
		// Check to see if any fields are missing

		// since this field is missing add to list of error fields
		// TODO addErrorField(request, getFieldName(inputs[i]));

		// Step 4:
		// Save changes to database
		ContentServiceFactory.getInstance().getInProgressSubmissionService(subInfo.getSubmissionItem()).update(context, subInfo.getSubmissionItem());

		// commit changes
		context.dispatchEvents();

		// if one or more fields errored out, return
		if (getErrorFields(request) != null && getErrorFields(request).size() > 0) {
			return STATUS_MISSING_REQUIRED_FIELDS;
		}

		// completed without errors
		return STATUS_COMPLETE;
	}

	@Override
	public int getNumberOfPages(HttpServletRequest request, SubmissionInfo subInfo) throws ServletException {
		return 1;
	}
}
