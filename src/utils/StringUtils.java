package utils;

/**
 * Class containing utility functions on Strings
 */

public abstract class StringUtils
{
    /**
     * compare two strings
     * @param a string 1
     * @param b string 2
     * @return true if equal
     */
	public static boolean equal(String a, String b)
	{
		return a == b || (a != null && a.equals(b));
	}
}
