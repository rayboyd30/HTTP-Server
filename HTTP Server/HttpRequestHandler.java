import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.StringTokenizer;

public class HttpRequestHandler implements Runnable
{
  private Socket socket;
  private InputStream input;
  private OutputStream output;
  private BufferedReader reader;
  private static String CRLF = "\r\n";

  public HttpRequestHandler(Socket socket)
  {
    try
    {
      this.socket = socket;
      this.output = socket.getOutputStream();
      this.input = socket.getInputStream();
      this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }
    catch (IOException e)
    {
      System.out.println(e);
    }
  }

  public void run()
  {
    String serverLine = "Server: CS4333_Http_Server/1.0";
    String statusLine = null;
    String contentTypeLine = null;
    String entityBody = null;
    String contentLengthLine = "Content-Length: 0";
    FileInputStream input = null;
    boolean fileExists = true;
    String fileName = null;
    boolean validRequest = false;
    try
    {
      while (true)
      {
        try
        {
          String headerLine = reader.readLine();

          if (headerLine.equals(CRLF) || headerLine.equals(""))
          {
            break;
          }
          StringTokenizer tokens = new StringTokenizer(headerLine);
          String temp = tokens.nextToken();

          if (temp.equals("GET"))
          {
            validRequest = true;
            fileName = tokens.nextToken();
            fileName = "public_html/" + fileName;
            try
            {
              input = new FileInputStream(fileName);
            }
            catch (FileNotFoundException e)
            {
              fileExists = false;
            }

            if (fileExists)
            {
              statusLine = "HTTP/1.1 200 OK" + CRLF;
              contentTypeLine = "Content-type: " + contentType(fileName) + CRLF;
              contentLengthLine = "Content-Length: " + (new Integer(input.available())).toString() + CRLF;
            }
            else
            {
              statusLine = "HTTP/1.1 404 Not Found" + CRLF;
              contentTypeLine = "Content-type: text/html";
              entityBody = "<html>"
                + "<head><title>404 Not Found</title></head>"
                + "<body>404 Not Found<body></html>";
            }

            output.write(statusLine.getBytes());
            System.out.println(statusLine);

            output.write(serverLine.getBytes());
            System.out.println(serverLine);

            output.write(contentTypeLine.getBytes());
            System.out.println(contentTypeLine);

            output.write(contentLengthLine.getBytes());
            System.out.println(contentLengthLine);

            output.write(CRLF.getBytes());
            System.out.print(CRLF);

            if (fileExists)
            {
              sendBytes(input, output);
              input.close();
            }
            else
            {
              output.write(entityBody.getBytes());
            }

          }
          else if (temp.equals("HEAD"))
          {
            validRequest = true;
            if (fileExists)
            {
              statusLine = "HTTP/1.1 200 OK" + CRLF;
              contentTypeLine = "Content-type: " + contentType(fileName) + CRLF;
              contentLengthLine = "Content-Length: " + (new Integer(input.available())).toString() + CRLF;
            }
            else
            {
              statusLine = "HTTP/1.1 404 Not Found" + CRLF;
              contentTypeLine = "Content-type: text/html";
              entityBody = "<html>"
                + "<head><title>404 Not Found</title></head>"
                + "<body>404 Not Found<body></html>";
            }
            output.write(statusLine.getBytes());
            System.out.println(statusLine);

            output.write(serverLine.getBytes());
            System.out.println(serverLine);

            output.write(contentTypeLine.getBytes());
            System.out.println(contentTypeLine);

            output.write(contentLengthLine.getBytes());
            System.out.println(contentLengthLine);

            output.write(CRLF.getBytes());
            System.out.println(CRLF);
          }
          else if (temp.equals("OPTIONS") || temp.equals("PUT") || temp.equals("POST")
                  || temp.equals("DELETE") || temp.equals("TRACE") || temp.equals("CONNECT"))
          {
            validRequest = true;
            statusLine = "HTTP/1.0 501 Not Implemented" + CRLF;
            contentTypeLine = "text/html";
            entityBody = "<html>"
              + "<head><title>501 Not Implemented</title></head>"
              + "<body>501 Not Implemented<body></html>";

            output.write(statusLine.getBytes());
            System.out.println(statusLine);

            output.write(serverLine.getBytes());
            System.out.println(serverLine);

            output.write(contentTypeLine.getBytes());
            System.out.println(contentTypeLine);

            output.write(contentLengthLine.getBytes());
            System.out.println(contentLengthLine);

            output.write(CRLF.getBytes());
            System.out.println(CRLF);
          }
          else if (!validRequest)
          {
            statusLine = "HTTP/1.0 400 Bad Request" + CRLF;
            contentTypeLine = "text/html";
            entityBody = "<html>"
              + "<head><title>400 Bad Request</title></head>"
              + "<body>400 Bad Request<body></html>";

            output.write(statusLine.getBytes());
            System.out.println(statusLine);

            output.write(serverLine.getBytes());
            System.out.println(serverLine);

            output.write(contentTypeLine.getBytes());
            System.out.println(contentTypeLine);

            output.write(contentLengthLine.getBytes());
            System.out.println(contentLengthLine);

            output.write(CRLF.getBytes());
            System.out.println(CRLF);
          }
          System.out.println(headerLine);
        }
        catch (Exception e)
        {
          System.out.println(e);
        }
      }
      try
      {
        output.close();
        reader.close();
        socket.close();
      }
      catch (Exception e)
      {
        System.out.println(e);
      }
    }
    catch (Exception e)
    {
      System.out.println(e);
    }
  }

  private static void sendBytes(FileInputStream input, OutputStream output)
    throws Exception
  {

    byte[] buffer = new byte[1024];
    int bytes = 0;

    while ((bytes = input.read(buffer)) != -1)
    {
        output.write(buffer, 0, bytes);
    }
  }

  private static String contentType(String fileName)
  {
      if (fileName.endsWith(".htm") || fileName.endsWith(".html") || fileName.endsWith(".txt"))
      {
          return "text/html";
      }
      else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg"))
      {
        return "image/jpeg";
      }
      else if (fileName.endsWith(".gif"))
      {
        return "image/gif";
      }
      else
      {
        return "error";
      }
  }

}
