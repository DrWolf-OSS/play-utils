package it.drwolf.utils;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FilenameUtils;

public final class CommonUtils {

	private CommonUtils() {
	}

	public static String appendTimeStamp(String fileName, String dateFormat) {
		String baseName = FilenameUtils.getBaseName(fileName);
		String extension = FilenameUtils.getExtension(fileName);
		String timestamp = new SimpleDateFormat(
				dateFormat != null && !dateFormat.isEmpty() ? dateFormat : "yyyy_MM_dd").format(new Date());
		return String.format("%s-%s.%s", baseName, timestamp, extension);
	}

	public static List<String> convertCommaSeparatedToListOfString(String stringsSeparatedByComma) {
		List<String> list = new ArrayList<>();
		String[] splitted = stringsSeparatedByComma.split(",");
		for (String s : splitted) {
			list.add(s.trim());
		}
		return list;
	}

	public static Set<Long> convertToLongs(Set<Integer> ints) {
		return ints.stream().map(i -> Long.valueOf(i)).collect(Collectors.toSet());
	}

	public static Set<Long> convertToLongs(String longsSeparatedByComma) {
		Set<Long> set = new HashSet<>();
		String[] inputInternalCategoriesId = longsSeparatedByComma.split(",");
		for (String icid : inputInternalCategoriesId) {
			set.add(Long.parseLong(icid));
		}
		return set;
	}

	public static byte[] zip(byte[] source, String filename) {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ZipOutputStream zipOut = new ZipOutputStream(baos)) {
			ZipEntry zipEntry = new ZipEntry(filename);
			zipOut.putNextEntry(zipEntry);
			zipOut.write(source);
			zipOut.closeEntry();
			zipOut.close();
			return baos.toByteArray();
		} catch (Exception e) {
			throw new RuntimeException("Error writing a zip file", e);
		}
	}

}
