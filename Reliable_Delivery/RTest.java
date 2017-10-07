import edu.utulsa.unet.*;

public class RTest
{
  public static void main(String[] args)
  {
    RReceiveUDP receiver = new RReceiveUDP();
    receiver.setFilename("receive.txt");
    receiver.setLocalPort(23456);
    receiver.receiveFile();
  }
}
