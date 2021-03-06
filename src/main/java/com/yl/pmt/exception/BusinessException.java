package com.yl.pmt.exception;

import lombok.Data;

/**
 * BusinessException:自定义异常
 *
 * @author pch
 * @date 2021/5/13
 */
@Data
public class BusinessException extends RuntimeException {

	/**
	 * 自定义返回状态码
	 */
	private Integer code;

	/**
	 * 返回自定义的状态码和异常描述
	 *
	 * @param code
	 * @param msg
	 */
	public BusinessException(Integer code, String msg) {
		super(msg);
		this.code = code;
	}

	/**
	 * 只返回异常信息(默认返回500)
	 *
	 * @param msg
	 */
	public BusinessException(String msg) {
		super(msg);
	}

}
