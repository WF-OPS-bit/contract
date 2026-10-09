package com.ar.contractreview.exception;

/**
 * 调用 Python AI 服务失败时抛出。
 * <p>
 * 与 {@link BusinessException} 的区别：这个异常专门表示“Python 侧”的问题，
 * 携带 HTTP 状态码，便于上层区分「Python 没启动(连接失败)」
 * 和「Python 正常返回但业务报错(4xx/5xx + code/message)」两种情况。
 * </p>
 *
 * @author wyh
 */
public class PythonAiException extends RuntimeException {

    /**
     * Python 返回的 HTTP 状态码；连接失败/超时时为 null
     */
    private final Integer httpStatus;

    /**
     * Python 返回的业务码（响应体里的 code 字段）
     */
    private final Integer bizCode;

    public PythonAiException(String message) {
        this(message, null, null, null);
    }

    public PythonAiException(String message, Throwable cause) {
        this(message, null, null, cause);
    }

    public PythonAiException(String message, Integer httpStatus, Integer bizCode, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.bizCode = bizCode;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }

    public Integer getBizCode() {
        return bizCode;
    }
}
