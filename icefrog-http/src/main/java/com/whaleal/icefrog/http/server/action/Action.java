package com.whaleal.icefrog.http.server.action;

import com.whaleal.icefrog.http.server.HttpServerRequest;
import com.whaleal.icefrog.http.server.HttpServerResponse;

import java.io.IOException;

/**
 * 请求处理接口<br>
 * 当用户请求某个Path，则调用相应Action的doAction方法
 *
 * @author Looly
 * @author wh
 * @since 1.0.0
 */
@FunctionalInterface
public interface Action {

	/**
	 * 处理请求
	 *
	 * @param request  请求对象
	 * @param response 响应对象
	 * @throws IOException IO异常
	 */
	void doAction(HttpServerRequest request, HttpServerResponse response) throws IOException;
}