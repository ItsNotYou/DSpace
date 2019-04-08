package org.dspace.submit.step;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dspace.app.util.SubmissionInfo;
import org.dspace.app.util.Util;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.submit.AbstractProcessingStep;

public class DataUploadQuestionStep extends AbstractProcessingStep {

	// is the publication based on text, data or code?
	public static final String BASED_ON_TEXT = "text_based";
	public static final String BASED_ON_DATA = "data_based";
	public static final String BASED_ON_CODE = "code_based";

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

	/***************************************************************************
	 * STATUS / ERROR FLAGS (returned by doProcessing() if an error occurs or
	 * additional user interaction may be required)
	 * 
	 * (Do NOT use status of 0, since it corresponds to STATUS_COMPLETE flag defined
	 * in the JSPStepManager class)
	 **************************************************************************/
	/**
	 * User didn't select an upload state
	 */
	public static final int STATUS_NO_STATE_SELECTED = 1;

	@Override
	public int doProcessing(Context context, HttpServletRequest request, HttpServletResponse response, SubmissionInfo subInfo) throws ServletException, IOException, SQLException, AuthorizeException {
		// get button user pressed
		String buttonPressed = Util.getSubmitButton(request, NEXT_BUTTON);

		// get reference to item
		Item item = subInfo.getSubmissionItem().getItem();

		// For Manakin:
		// Choosing an upload state means selecting an option and clicking Next
		String[] state = request.getParameterValues(PARAMETER_BASED_ON);

		if (state == null && buttonPressed.equals(NEXT_BUTTON)) {
			return STATUS_NO_STATE_SELECTED;
		}

		// TODO: check for correct states

		context.dispatchEvents();
		return STATUS_COMPLETE;
	}

	@Override
	public int getNumberOfPages(HttpServletRequest request, SubmissionInfo subInfo) throws ServletException {
		return 1;
	}
}
