package it.drwolf.utils;

import java.time.LocalDate;
import java.util.List;

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
	static public List<Long> getListOfLongsParameter(Request request, String parameterName) {
		String longsAsString = RequestUtils.getStringParameter(request, parameterName);
		List<Long> listOfLongs = null;
		if (longsAsString != null && !longsAsString.trim().isEmpty()) {
			listOfLongs = CommonUtils.convertToListOfLongs(longsAsString);
		}
		return listOfLongs;
	}

	static public List<String> getListOfStringsParameter(Request request, String parameterName) {
		String commaSeparatedValues = RequestUtils.getStringParameter(request, parameterName);
		if (commaSeparatedValues != null) {
			return CommonUtils.convertToListOfStrings(commaSeparatedValues);
		}
		return null;
	}

	static public String getStringParameter(Request request, String parameterName) {
		return (request.queryString().containsKey(parameterName)
				&& request.queryString().get(parameterName).length > 0) ?
				request.queryString().get(parameterName)[0] :
				null;
	}

}
