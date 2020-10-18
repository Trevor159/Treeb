package gg.trevor.treeb;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Util
{
	public static String DOWNLOAD_PATH;

	public static String[] parseArgs(String args)
	{
		List<String> tokens = new ArrayList<>();
		StringBuilder sb = new StringBuilder();

		boolean insideQuote = false;

		for (char c : args.toCharArray())
		{

			if (c == '"')
			{
				insideQuote = !insideQuote;
				continue;
			}

			//when space is not inside quote split..
			if (c == ' ' && !insideQuote)
			{
				tokens.add(sb.toString()); //token is ready, lets add it to list
				sb.delete(0, sb.length()); //and reset StringBuilder`s content
			}
			else
			{
				sb.append(c); //else add character to token
			}
		}

		//lets not forget about last token that doesn't have space after it
		tokens.add(sb.toString());

		return tokens.toArray(new String[0]);
	}

	public static Double parseNumber(String number)
	{
		try
		{
			return Double.valueOf(number);
		}
		catch (NumberFormatException e)
		{
			return null;
		}
	}

	public static String getFullFilePath(String fileName)
	{
		return DOWNLOAD_PATH + "/" + fileName;
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
		list.sort(Map.Entry.comparingByValue());

		Map<K, V> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}

		return result;
	}

	public static boolean compareStrings(String str1, String str2)
	{
		str1 = normalizeString(str1);
		str2 = normalizeString(str2);

		return str1.equalsIgnoreCase(str2);
	}

	private static String normalizeString(String str)
	{
		str = Normalizer.normalize(str, Normalizer.Form.NFD);
		str = str.replaceAll("[^a-zA-Z0-9]", "");
		return str;
	}
}
