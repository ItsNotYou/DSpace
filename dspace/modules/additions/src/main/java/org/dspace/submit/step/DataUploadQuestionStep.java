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

	public static final String UPLOAD_NONEUSED = "none_used";
	public static final String UPLOAD_DATAONLY = "data_only";
	public static final String UPLOAD_CODEONLY = "code_only";
	public static final String UPLOAD_CODEANDDATA = "code_and_data";
	public static final String UPLOAD_MISSINGARTIFACT = "missing_artifact";

	public static final String PARAMETER_NAME = "upload_state";

	/**
	 * Upload state.
	 * 
	 * @see #UPLOAD_NONEUSED
	 * @see #UPLOAD_DATAONLY
	 * @see #UPLOAD_CODEONLY
	 * @see #UPLOAD_CODEANDDATA
	 * @see #UPLOAD_MISSINGARTIFACT
	 */
	public static final String[] METADATA_STATE = { "local", "upload", "state" };

	/**
	 * Comment on the upload state. Should be used to explain why an artifact is
	 * missing.
	 */
	public static final String[] METADATA_COMMENT = { "local", "upload", "comment" };

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
		String state = request.getParameter(PARAMETER_NAME);

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
