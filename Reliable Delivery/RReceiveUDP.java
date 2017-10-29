import java.net.DatagramPacket;
import edu.utulsa.unet.*;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.io.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.nio.ByteBuffer;

public class RReceiveUDP implements RReceiveUDPI, Runnable
{
  private int recMode;
  private long modeParameter;
  private String filename;
  private int localPort;
  private UDPSocket socket;
  private int mtu;
  private InetSocketAddress sender;
  private ArrayList<byte[]> data = new ArrayList<byte[]>();
  private Long finishedTimeout;
  private boolean finished = false;

	public RReceiveUDP()
  {
    recMode = 0;
    localPort = 12987;
  }

  public static void main(String[] args)
  {
    RReceiveUDP receiver = new RReceiveUDP();
    receiver.setFilename("receive.txt");
    receiver.setLocalPort(23456);
    receiver.receiveFile();
    System.exit(0);
  }

  public boolean setMode(int mode)
  {
    recMode = mode;
    return true;
  }

  public int getMode()
  {
    return recMode;
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
    filename = new String(fname);
  }

  public String getFilename()
  {
    return filename;
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

  public boolean receiveFile()
  {
    try
    {
      socket = new UDPSocket(localPort);
      mtu = socket.getSendBufferSize();
      long startTime = System.currentTimeMillis();
      finishedTimeout = Long.MAX_VALUE;

      if(recMode == 0)
      {
        System.out.println("Receiving "+this.filename+" at "+socket.getLocalAddress().getCanonicalHostName()+":"+this.getLocalPort()+" using stop-and-wait");
      }
      else
      {
        System.out.println("Receiving "+this.filename+" at "+socket.getLocalAddress().getCanonicalHostName()+":"+this.getLocalPort()+" using sliding window");
      }
        Thread t = new Thread(this);
        t.start();
        while (finishedTimeout > System.currentTimeMillis())
        {
          Thread.sleep(10);
        }
        long stopTime = System.currentTimeMillis();
        int totalBytes = 0;
        for (byte[] b: data)
        {
          totalBytes += b.length;
        }

        byte[] file = new byte[totalBytes];
        ByteBuffer buffer = ByteBuffer.wrap(file);
        for(byte[] b: data)
        {
          buffer.put(b);
        }

      PrintWriter fileWriter = new PrintWriter(filename);
      fileWriter.print(new String(file));
      fileWriter.close();
      System.out.println("Successfully received " + filename + " (" + totalBytes + ") " + " in " + (stopTime-startTime)/1000 + " seconds");
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  public void run()
  {
    while (finishedTimeout > System.currentTimeMillis())
    {
      try
      {
        byte[] buffer = new byte[mtu];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        UDPPacket dataPacket = UDPPacket.getPacketFields(buffer, packet.getLength());

        if (sender == null)
        {
          sender = new InetSocketAddress(packet.getAddress(), packet.getPort());
          System.out.println("Connection established from " + sender.toString());
        }
        while((data.size() - 1) < dataPacket.seqNum)
        {
          data.add(null);
        }
        data.set(dataPacket.seqNum, dataPacket.data);
        System.out.println("Message "+ dataPacket.seqNum+" received with "+dataPacket.data.length+" bytes of actual data");

        if (dataPacket.endOfFile == 0x01)
        {
          System.out.println("lastAck");
          UDPPacket ack = new UDPPacket(new byte[0], dataPacket.seqNum, (byte)0x01, (byte)0x01, System.currentTimeMillis(), (byte)0x00);
          socket.send(new DatagramPacket(ack.getPacketBytes(), ack.getPacketBytes().length, sender));
          System.out.println("Message "+dataPacket.seqNum+" acknowledged");
          finishedTimeout = System.currentTimeMillis() + (2 * 2000);
        }
        else
        {
          UDPPacket ack = new UDPPacket(new byte[0], dataPacket.seqNum, (byte)0x01, (byte)0x00, System.currentTimeMillis(), (byte)0x00);
          socket.send(new DatagramPacket(ack.getPacketBytes(), ack.getPacketBytes().length, sender));
          System.out.println("Message "+dataPacket.seqNum+" acknowledged");
        }
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    }
  }
}
