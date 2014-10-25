package httpproxy;
import java.net.*;
import java.io.*;
import java.util.regex.*;

// This is a simple single-threaded HTTP proxy that reads the request path, makes another http request, reads data, 
// and passes data back to the client as an attachment 
// The purpose of this project is learning sockets and networking in Java
public class HTTPProxy {

    public static void main(String[] args) throws Exception {
        OutputStream out = null;
        Reader in = null;
        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        Pattern pattern = Pattern.compile(" /(.*?) "); // pattern to extract the url from "GET /<url> HTTP/1.1"
        
        try {
            serverSocket = new ServerSocket(3333);
            System.out.println("Ready to accept connections.\n\n");
            
            while(true) {
                try {  
                    ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
                    
                    // open connection to a client; .accept() will block until a client connects
                    clientSocket = serverSocket.accept();
                    
                    out = new BufferedOutputStream(clientSocket.getOutputStream());
                    in = new InputStreamReader(new BufferedInputStream(clientSocket.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    
                    int c;
                    // just read the first line of HTTP request
                    while(true) {
                        c = in.read();
                        if (c == '\r' || c == '\n' || c == -1) break;                    
                        stringBuilder.append((char)c);
                    }
                    
                    String firstLine = stringBuilder.toString();
                    System.out.println(firstLine);
                    String theFile;
                            
                    Matcher matcher = pattern.matcher(firstLine);
                    if (matcher.find()) 
                    {
                        theFile = matcher.group(1);
                        URL url = new URL(theFile.contains("http://") ? theFile : "http://" + theFile);
                        URLConnection conn = url.openConnection();
                        InputStream input = conn.getInputStream();
                        
                        out.write("HTTP/1.1 200 OK\n".getBytes("ASCII")); 
                        out.write("Content-Type: text/html; charset=utf-8\n".getBytes("ASCII"));

                        int inputByte;
                        try 
                        {
                            //Read byte by byte from the input and write into the buffer
                            //When the end of the stream is reached, a value of -1 is returned. 
                            while ((inputByte = input.read()) != -1) 
                            { 
                                byteArrayStream.write(inputByte);
                            }    

                            String contenLength = "Content-Length: " + byteArrayStream.size() + "\n";
                            out.write(contenLength.getBytes("ASCII"));
                            // this header signals the web broswer to save the file instead of opening it
                            out.write("Content-Disposition: attachment; filename=File.zip \r\n\r\n".getBytes("ASCII"));
                            // write the file content to output stream    
                            byteArrayStream.writeTo(out);
                            out.flush(); // always flush the stream in case the buffer is not full and waiting for more data.
                        }
                        catch(Exception ex) {
                            System.out.println(ex.getMessage());
                        }
                        finally 
                        {
                            out.close();
                        }
                        
                        try {
                            input.close();
                        }
                        catch(Exception ex) { 
                            System.out.println(ex.getMessage()); 
                        }
                    }
                }
                catch(Exception ex) {
                    System.out.println(ex.getMessage());
                }
                finally {
                    if (clientSocket != null) { 
                        clientSocket.close();
                    }
                    if (in != null) {
                        in.close();
                    }
                    if (out != null) {
                        out.close();
                    }
                }
            }
        }
        finally {
            if(serverSocket != null) serverSocket.close();
        }
    }

}
