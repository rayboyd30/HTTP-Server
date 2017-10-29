import java.net.*;
import edu.utulsa.unet.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Arrays;
import java.lang.Math;

public class RSendUDP implements RSendUDPI, Runnable
{
  private int sendMode;
  private int localPort;
  private long modeParameter;
  private String filename;
  private long timeout;
  private InetSocketAddress remoteReceiver;
  private UDPSocket socket;
  private ArrayList<UDPPacket> sentPackets = new ArrayList<UDPPacket>();
  private int mtu;
  private boolean endOfFile = false;
  private short seqNum = 0;
  private int lastAckReceived = -1;
  private int lastSent = -1;


  public RSendUDP()
  {
    sendMode = 0;
    modeParameter = 1;
    localPort = 12987;
    remoteReceiver = new InetSocketAddress("localhost", 12987);
  }

  public static void main(String[] args)
  {
    RSendUDP sender = new RSendUDP();
    sender.setFilename("send.txt");
    sender.setTimeout(1000);
    sender.setLocalPort(2000);
    sender.setReceiver(new InetSocketAddress("localhost", 23456));
    sender.sendFile();
  }

  public boolean setMode(int mode)
  {
     sendMode = mode;
     if (mode == 1)
     {
       modeParameter = 256;
     }
     return true;
  }

  public int getMode()
  {
    return sendMode;
  }

  public boolean setModeParameter(long n)
  {
    modeParameter = n;
    return true;
  }

  public long getModeParameter()
  {
    return modeParameter;
  }

  public void setFilename(String fname)
  {
    filename = fname;
  }

  public String getFilename()
  {
    return filename;
  }

  public boolean setTimeout(long timeout)
  {
    timeout = timeout;
    return true;
  }

  public long getTimeout()
  {
    return timeout;
  }

  public boolean setLocalPort(int port)
  {
    localPort = port;
    return true;
  }

  public int getLocalPort()
  {
    return localPort;
  }

  public boolean setReceiver(InetSocketAddress receiver)
  {
    remoteReceiver = new InetSocketAddress(receiver.getAddress(), receiver.getPort());
    return true;
  }

  public InetSocketAddress getReceiver()
  {
    return remoteReceiver;
  }

  public boolean sendFile()
  {
    try
    {
      socket = new UDPSocket(localPort);
      mtu = socket.getSendBufferSize();
      String fileText = new Scanner(new File(filename)).useDelimiter("\\Z").next();
      ArrayList<byte[]> packets = buildPackets(fileText, mtu);
      System.out.println(packets.size());

      System.out.println("Sending "+this.filename+" from "+socket.getLocalAddress().getHostAddress()+":"+this.getLocalPort()+" to "+this.remoteReceiver.toString()+" with "+fileText.length()+" bytes");
      if (sendMode == 0)
      {
			  System.out.println("Using stop-and-wait");
      }
      else
      {
        System.out.println("Using sliding window");
      }
      Long startTime = System.currentTimeMillis();

      new Thread(this).start();
      if (sendMode == 0)
      {
        while (!endOfFile)
        {
          if (lastSent + 1 == packets.size() - 1  && lastAckReceived == lastSent)
          {
            UDPPacket packet = new UDPPacket(packets.get(lastSent + 1), (short)(lastSent + 1), (byte)0x00, (byte)0x01, System.currentTimeMillis(), (byte)0x00);
            socket.send(new DatagramPacket(packet.getPacketBytes(), packet.getPacketBytes().length, remoteReceiver));
            sentPackets.add(packet);
            System.out.println("Message " + (lastSent + 1) + " sent with " + packet.data.length + " bytes of actual data");
            lastSent++;
          }
          else if (lastSent == lastAckReceived && lastSent + 1 < packets.size())
          {
            UDPPacket packet = new UDPPacket(packets.get(lastSent + 1), (short)(lastSent + 1), (byte)0x00, (byte)0x00, System.currentTimeMillis(), (byte)0x00);
            socket.send(new DatagramPacket(packet.getPacketBytes(), packet.getPacketBytes().length, remoteReceiver));
            sentPackets.add(packet);
            System.out.println("Message " + (lastSent + 1) + " sent with " + packet.data.length + " bytes of actual data");
            lastSent++;
          }
          else if (checkTimeout() != null)
          {
            int i = checkTimeout();
            System.out.println("Message " + i + " timed out");
            socket.send(new DatagramPacket(sentPackets.get(i).getPacketBytes(), sentPackets.get(i).getPacketBytes().length, remoteReceiver));
            sentPackets.get(i).timeSent = System.currentTimeMillis();
            System.out.println("Message " + i + " sent with " + sentPackets.get(i).data.length + " bytes of actual data");
          }
        }
      }
      Long endTime = System.currentTimeMillis();
      System.out.println("Successfully transferred " + filename + " (" + fileText.length() + ") " + "in " + (((double)endTime - (double)startTime)/1000) + " seconds");
    }
    catch (SocketException e)
    {
      e.printStackTrace();
    }
    catch (FileNotFoundException e)
    {
      e.printStackTrace();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    return true;
  }

  private synchronized Integer checkTimeout()
  {
    //System.out.println("Check timeout");
			if(sentPackets.get(lastSent).acked == 0x00 && ((System.currentTimeMillis() - sentPackets.get(lastSent).timeSent) > this.timeout))
				return lastSent;
    //System.out.println("null");
		return null;
	}

  private synchronized void updateLastAck(short seqNum)
  {
    lastAckReceived = seqNum;
  }

  @Override
  public void run()
  {
    while (!endOfFile)
    {
      timeout = 1000;
      byte[] buffer = new byte[mtu];
      DatagramPacket ackPacket = new DatagramPacket(buffer, buffer.length);
      try
      {
        socket.receive(ackPacket);
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
      UDPPacket ack = UDPPacket.getPacketFields(buffer, ackPacket.getLength());
      if (ack.isAck == 0x01)
      {
        sentPackets.get(ack.seqNum).acked = 0x01;
        updateLastAck(ack.seqNum);
        System.out.println("Message "+ ack.seqNum +" acknowledged");
      }
      if (ack.endOfFile == 0x01)
      {
        endOfFile = true;
      }
    }
  }

  private ArrayList<byte[]> buildPackets(String fileText, int mtu)
  {
    byte[] fileBytes = fileText.getBytes();
    int dataSize = mtu - 13;
    int numPackets = (int)Math.ceil(fileText.length()/(double)dataSize);
    ArrayList<byte[]> packets = new ArrayList<byte[]>();

    for(int i = 0; i < numPackets; i++)
    {
      byte[] data = Arrays.copyOfRange(fileBytes, i * dataSize, Math.min((i + 1) * dataSize, fileBytes.length));
      packets.add(data);
    }
    return packets;
  }
}
