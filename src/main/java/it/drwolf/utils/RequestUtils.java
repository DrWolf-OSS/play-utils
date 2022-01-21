package it.drwolf.utils;

import java.time.LocalDate;
import java.util.Set;

import play.mvc.Http.Request;

public final class RequestUtils {

	private RequestUtils() {
	}

	static public Boolean getBooleanParameter(Request request, String parameterName) {
		return request.queryString().containsKey(parameterName) && request.queryString().get(parameterName).length > 0 ?
				Boolean.parseBoolean((request.queryString().get(parameterName)[0])) :
				null;
	}

	static public LocalDate getDateParameter(Request request, String parameterName, String datePattern) {
		String dateAsString = RequestUtils.getStringParameter(request, parameterName);
		return DateUtils.convertFromString(dateAsString, datePattern);
	}

	static public Integer getIntegerParameter(Request request, String parameterName) {
		String longAsString = RequestUtils.getStringParameter(request, parameterName);
		Integer id = null;
		if (longAsString != null && !longAsString.trim().isEmpty()) {
			id = Integer.parseInt(longAsString);
		}
		return id;
	}

	static public Long getLongParameter(Request request, String parameterName) {
		String longAsString = RequestUtils.getStringParameter(request, parameterName);
		Long id = null;
		if (longAsString != null && !longAsString.trim().isEmpty()) {
			id = Long.parseLong(longAsString);
		}
		return id;
	}

	/**
	 * Il parametro è una stringa contenente dei long separati da virgole. Es.:
	 * "1,34,42"
	 *
	 * @param request
	 * @param parameterName
	 * @return
	 */
	static public Set<Long> getSetOfLongParameter(Request request, String parameterName) {
		String idsAsString = RequestUtils.getStringParameter(request, parameterName);
		Set<Long> setOfLongs = null;
		if (idsAsString != null && !idsAsString.trim().isEmpty()) {
			setOfLongs = CommonUtils.convertToLongs(idsAsString);
		}
		return setOfLongs;
	}

	static public String getStringParameter(Request request, String parameterName) {
		return (request.queryString().containsKey(parameterName)
				&& request.queryString().get(parameterName).length > 0) ?
				request.queryString().get(parameterName)[0] :
				null;
	}

}
