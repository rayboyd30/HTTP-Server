import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer
{
  public static void main(String args[])
  {
    ServerSocket serverSocket;
    int port = Integer.parseInt(args[0]);
    
    try
    {
      if (port < 1 || port > 65535)
      {
        System.out.println("Invalid port number, must be between 1 and 65,535");
      }
    }
    catch (Exception e)
    {
      System.out.println("Invalid argument, must be a valid port number (1-65,535)");
    }
    try
    {
      serverSocket = new ServerSocket(port);
      System.out.println("HttpServer running on port " + serverSocket.getLocalPort());

      while (true)
      {
        Socket socket = serverSocket.accept();
        System.out.println("New connection accepted " + socket.getInetAddress() + ":" + socket.getPort());

        try
        {
          HttpRequestHandler request = new HttpRequestHandler(socket);
          Thread thread = new Thread(request);
          thread.start();
        }
        catch (Exception e)
        {
          System.out.println(e);
        }
      }
    }
    catch (IOException e)
    {
      System.out.println(e);
    }
  }
}
