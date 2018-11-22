package mr.linage.com.soket;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class TCPClient extends Thread {

    private String TAG = getClass().getSimpleName().trim();

    /**
     * 소켓 변수
     */
    private Socket socket;
    private BufferedWriter networkWriter;
    public boolean RESULT_OK = false;

    SocketAddress socketAddress;
    private final int connection_timeout = 2000;

    String msg = "app";

    public TCPClient(String ip, int port) throws RuntimeException {
        socketAddress = new InetSocketAddress(ip, port);
    }
    @Override
    public void run() {
        try {
            socket = new Socket();
            socket.setSoTimeout(connection_timeout);
            socket.setSoLinger(true, 0);
            socket.connect(socketAddress, connection_timeout);
            networkWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            Log.d(TAG,"setThread"+" "+"정상적으로 서버에 접속하였습니다.");
            RESULT_OK = true;
        } catch (Exception e) {
            Log.d(TAG,"setThread"+" "+"소켓을 생성하지 못했습니다.");
            quit();
        }
    }

    public void quit() {
        try {
            if (networkWriter != null) {
                networkWriter.close();
                networkWriter = null;
            }
            if (socket != null) {
                socket.close();
                socket = null;
                Log.d(TAG,"setThread"+" "+"접속을 중단합니다.");
            }
        } catch (IOException e) {
            Log.d(TAG, "에러 발생", e);
        }
    }

    public void sendDing(String msg) {
        Log.d(TAG,"sendDing"+" "+"msg:"+msg);
//        Log.d(TAG,"sendDing"+" "+"networkWriter:"+networkWriter);
        if("".equals(msg)) {
            msg = this.msg;
        }
        try {
            if(networkWriter!=null) {
                networkWriter.write(msg);
                networkWriter.newLine();
                networkWriter.flush();
                Log.d(TAG,"sendDing 완료");
            }
        } catch (Exception e) {
            Log.d(TAG, "에러 발생", e);
        }
    }
}