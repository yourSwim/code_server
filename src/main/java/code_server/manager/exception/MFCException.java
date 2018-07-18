package code_server.manager.exception;

public class MFCException extends RuntimeException {

	private static final long serialVersionUID = -3748143974774978381L;

	private int code;

	public MFCException() {
		this(500);

	}

	public MFCException(int code) {
		super();
		this.code = code;
	}

	public MFCException(final String msg) {
		this(500, msg);
	}

	public MFCException(int code, final String msg) {
		super(msg);
		this.code = code;
	}

	public MFCException(final String msg, final Throwable cause) {
		this(500, msg, cause);
	}

	public MFCException(int code, final String msg, final Throwable cause) {
		super(msg, cause);
		this.code = code;
	}

	public MFCException(final Throwable cause) {
		this(500, cause);
	}

	public MFCException(int code, final Throwable cause) {
		super(cause);
		this.code = code;
	}

	public int getCode() {
		return this.code;
	}
}