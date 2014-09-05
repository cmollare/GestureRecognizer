package ui;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

import core.GestureLabel;

public class UDPWriter implements OutputWriter
{
	private DatagramSocket socket;
	private int port;
	private InetAddress address;
	
	public UDPWriter(String ip, int port)
	{
		try
		{
			this.port = port;
			address = InetAddress.getByName(ip);
			socket = new DatagramSocket();
		}
		catch (SocketException e)
		{
			e.printStackTrace();
		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void write(GestureLabel label)
	{
		// TODO
	}

	@Override
	public void write(String n)
	{
		String s = n + " " + 1f + "\n";
		byte[] b = s.getBytes();
		try
		{
			DatagramPacket packet = new DatagramPacket(b, s.length(), address, port);
			socket.send(packet);
			System.out.println("wrote " + Arrays.toString(b) + " at " + packet.getAddress() + " " + packet.getPort());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
