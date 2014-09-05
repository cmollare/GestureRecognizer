package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import recognition.Recognizer;

public abstract class SerializationUtils
{
	public static void toFile(String filename, Object o)
	{
		FileOutputStream os;
		ObjectOutputStream oos;
		try
		{
			os = new FileOutputStream(filename);
			oos = new ObjectOutputStream(os);
			oos.writeObject(o);
			oos.flush();
			oos.close();
			os.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public static Object fromFile(String filename)
	{
		FileInputStream is;
		try
		{
			is = new FileInputStream(filename);
			ObjectInputStream ois = new ObjectInputStream(is);
			Object obj = ois.readObject();
			ois.close();
			is.close();
			return obj;
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static boolean fileExists(String filename)
	{
		return new File(filename).exists();
	}
}
