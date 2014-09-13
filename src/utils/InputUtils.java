package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public abstract class InputUtils
{
	public static boolean enterIsPressed()
	{
		try
		{
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			if (!br.ready())
				return false;
			br.readLine();
			return true;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return false;
	}
}
