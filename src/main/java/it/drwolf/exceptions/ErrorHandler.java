package it.drwolf.exceptions;

import com.fasterxml.jackson.databind.JsonNode;
import it.drwolf.base.interfaces.Loggable;
import play.http.HttpErrorHandler;
import play.libs.Json;
import play.mvc.Http.RequestHeader;
import play.mvc.Result;
import play.mvc.Results;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class ErrorHandler implements HttpErrorHandler, Loggable {

	private JsonNode createErrorData(RequestHeader request, int statusCode, String message, Throwable exception) {
		Map<String, Object> data = new HashMap<>();
		data.put("path", request.path());
		data.put("code", statusCode);
		if (exception != null) {
			data.put("exception", exception.getClass().getSimpleName());
		}
		if (statusCode!=401){
			// do not give information to login attempts
			data.put("message", message);
		}
		return Json.toJson(data);
	}

	@Override
	public CompletionStage<Result> onClientError(RequestHeader request, int statusCode, String message) {
		String out = String.format("%s %d %s", request.path(), statusCode, message).trim();
		this.logger().error(out);
		return CompletableFuture
				.completedFuture(Results.status(statusCode, this.createErrorData(request, statusCode, message, null)));
	}

	@Override
	public CompletionStage<Result> onServerError(RequestHeader request, Throwable exception) {
		String out = String
				.format("%s %s: %s", request.path(), exception.getClass().getSimpleName(), exception.getMessage())
				.trim();

		this.logger().error(out, exception);
		if (exception instanceof HttpException) {
			HttpException mpException = (HttpException) exception;
			return CompletableFuture
					.completedFuture(Results.status(mpException.getStatus().getCode(), this.createErrorData(request,
							mpException.getStatus().getCode(), mpException.getMessage(), mpException)));

		}
		return CompletableFuture.completedFuture(
				Results.internalServerError(this.createErrorData(request, 500, exception.getMessage(), exception)));
	}
}
