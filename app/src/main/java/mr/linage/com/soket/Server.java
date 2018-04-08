package mr.linage.com.soket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by YDH on 2018-04-07.
 */

public class Server implements Runnable {

    public static final int ServerPort = 9999;

    @Override
    public void run() {
        try {
            System.out.println("S: Connecting...");
            ServerSocket serverSocket = new ServerSocket(ServerPort);
            while (true) {
                Socket client = serverSocket.accept();
                System.out.println("S: Receiving...");
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    String str = in.readLine();
                    System.out.println("S: Received: '" + str + "'");
                    Thread.sleep(10000);
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);
                    str = "리턴:" + str;
                    out.println("Server Received " + str);
                    System.out.println("Server Received " + str);
                } catch (Exception e) {
                    System.out.println("S: Error");
                    e.printStackTrace();
                } finally {

                    client.close();
                    System.out.println("S: Done.");
                }
            }
        } catch (Exception e) {
            System.out.println("S: Error");
            e.printStackTrace();
        }
    }
}
