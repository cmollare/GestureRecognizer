package utils;

public abstract class StringUtils
{
	public static boolean equal(String a, String b)
	{
		return a == b || (a != null && a.equals(b));
	}
}
