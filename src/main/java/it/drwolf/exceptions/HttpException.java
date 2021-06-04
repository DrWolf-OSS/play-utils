package it.drwolf.exceptions;

public class HttpException extends RuntimeException {

	public enum Status {

		BAD_REQUEST(400), UNAUTHORIZED(401), FORBIDDEN(403), CONFLICT(409), UNPROCESSABLE_ENTITY(422), LOCKED(423),

		INTERNAL_SERVER_ERROR(500), NOT_IMPLEMENTED(501);

		public final Integer code;

		Status(Integer code) {
			this.code = code;
		}

		public Integer getCode() {
			return this.code;
		}

	}

	private static final long serialVersionUID = -6350195518227039078L;
	private Status status = Status.INTERNAL_SERVER_ERROR;

	public HttpException(String message) {
		super(message, null);
	}

	public HttpException(String message, Status status) {
		super(message, null);
		this.status = status;
	}

	public HttpException(String message, Throwable throwable) {
		super(message, throwable);
	}


	public HttpException(String message, Throwable throwable, Status status) {
		super(message, throwable);
		this.status = status;
	}

	public Status getStatus() {
		return this.status;
	}

}
