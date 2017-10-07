import java.net.DatagramPacket;
import edu.utulsa.unet.*;
import java.net.InetAddress;
import java.io.*;

public class RReceiveUDP implements RReceiveUDPI
{
  private int recMode;
  private long modeParameter;
  private String filename;
  private int localPort;


	public RReceiveUDP()
  {
    recMode = 0;
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
    String ack = "ACK";
    if(recMode == 0)
    {
      try
      {
        UDPSocket socket = new UDPSocket(localPort);
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        while (true)
        {
          byte[] buffer = new byte[1024];
          DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
          socket.receive(packet);
          socket.send(new DatagramPacket(ack.getBytes(), ack.length(), packet.getAddress(), packet.getPort()));
          try
          {
            String line = new String(buffer);
            writer.write(line + "\n");
            writer.flush();
          }
          catch (IOException e)
          {
            e.printStackTrace();
          }
        }
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    }
    else
    {
      //sliding window
    }
    return true;
  }
}
