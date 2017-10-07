import java.net.*;
import edu.utulsa.unet.*;

public class STest
{
  public static void main(String[] args)
  {
    RSendUDP sender = new RSendUDP();
    sender.setFilename("send.txt");
    sender.setTimeout(1000);
    sender.setLocalPort(2000);
    sender.setReceiver(new InetSocketAddress("localhost", 23456));
    sender.sendFile();
  }
}
