package com.yl.pmt.security.handler;

import com.yl.pmt.security.util.ResultUtil;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Description 暂无权限处理类
 * @Author pch
 * @CreateTime 2020/10/3 8:39
 */
@Component
public class UserAuthAccessDeniedHandler implements AccessDeniedHandler {
	/**
	 * 暂无权限返回结果
	 *
	 * @Author pch
	 * @CreateTime 2020/10/3 8:41
	 */
	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception) {
		ResultUtil.responseJson(response, ResultUtil.resultCode(403, "未授权"));
	}
}
