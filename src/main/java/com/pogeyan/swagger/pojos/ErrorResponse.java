package com.pogeyan.swagger.pojos;


@SuppressWarnings("serial")
public class ErrorResponse extends Exception {
	private String error;
	private int errorCode;

	public ErrorResponse(ErrorResponse resp) {
		this.error = resp.error;
		this.errorCode = resp.errorCode;
	}

	public ErrorResponse(String error, int errorCode) {
		super();
		this.error = error;
		this.errorCode = errorCode;
	}

	/**
	 * Gets the error.
	 *
	 * @return the error
	 */
	public final String getError() {
		return error;
	}

	/**
	 * Sets the error.
	 *
	 * @param error
	 *            the new error
	 */
	public final void setError(final String error) {
		this.error = error;
	}

	/**
	 * Gets the error code.
	 *
	 * @return the error code
	 */
	public final int getErrorCode() {
		return errorCode;
	}

	/**
	 * Sets the error code.
	 *
	 * @param errorCode
	 *            the new error code
	 */
	public final void setErrorCode(final int errorCode) {
		this.errorCode = errorCode;
	}

	@Override
	public String toString() {
		return "ErrorCode [" + this.errorCode + "];ErrorMsg [" + this.error != null ? this.error : "" + "]";
	}
}
