package ui;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import core.GestureLabel;
import com.illposed.osc.*;

public class OSCWriter implements OutputWriter
{
	private OSCPortOut sender;
	private String receiver;
	
	public OSCWriter(String ip, int port, String receiver)
	{
		try
		{
			InetAddress ipAddress = InetAddress.getByName(ip);
			sender = new OSCPortOut(ipAddress, port);
			this.receiver = receiver;
		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}
		catch (SocketException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void write(String n)
	{
		Object[] values = new Object[] {n};
		OSCMessage msg = new OSCMessage(receiver, values);
		
		try
		{
			sender.send(msg);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void write(GestureLabel label)
	{
		throw new UnsupportedOperationException(); // TODO
	}
}
