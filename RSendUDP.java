import java.net.*;
import edu.utulsa.unet.*;
import java.io.*;

public class RSendUDP implements RSendUDPI
{
  private int sendMode;
  private int localPort;
  private long modeParameter;
  private String filename;
  private long timeout;
  private InetSocketAddress remoteReceiver;

  public RSendUDP()
  {
    sendMode = 0;
    timeout = 1000;
  }
  public boolean setMode(int mode)
  {
     sendMode = mode;
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

      byte[] sendBuffer = new byte[1024];
      byte[] receiveBuffer = new byte[1024];
      byte[] header;
      boolean timedOut = true;
      while(timedOut)
      {
        try
        {
          UDPSocket socket = new UDPSocket(localPort);
          socket.setSoTimeout((int)timeout);
          BufferedReader reader = new BufferedReader(new FileReader(filename));
          String line;
          while ((line = reader.readLine()) != null)
          {
            sendBuffer = line.getBytes();
            socket.send(new DatagramPacket(sendBuffer, sendBuffer.length, remoteReceiver.getAddress(), remoteReceiver.getPort()));
            socket.receive(new DatagramPacket(receiveBuffer, receiveBuffer.length));
            String ack = new String(receiveBuffer);
            
          }
        }
        catch(IOException e)
        {
          if (e instanceof SocketTimeoutException)
          {
            System.out.println("Timed Out!");
            //alternating bit
          }
          else
          {
              e.printStackTrace();
          }
        }
        timedOut = false;
      }
      return true;
    }
}
