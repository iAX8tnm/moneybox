package com.example.moneybox;


import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;

import static android.content.Context.MODE_PRIVATE;
import static com.example.moneybox.parseData.getCurrentDate;
import static com.example.moneybox.parseData.getTime;
import static com.example.moneybox.parseData.getTodayDate;
import static java.security.AccessController.getContext;


public class SocketClient {


    private static final String TAG = "MainActivity";
    private String serverIP="192.168.4.1";
    private int serverPort=8080;
    private boolean isConnected = false;
    private int hasNewData = 0;
    /**
     * 主 变量
     */


    // Socket变量
    private Socket socket = null;

    // 线程池
    // 为了方便展示,此处直接采用线程池进行线程管理,而没有一个个开线程
    private ExecutorService mThreadPool;

    /**
     * 接收服务器消息 变量
     */
    // 输入流对象
    private InputStream inputStream;

    // 输入流读取器对象
    private InputStreamReader isr ;
    private BufferedReader br ;

    // 接收服务器发送过来的消息
    private String response;
    private String message;


    /**
     * 发送消息到服务器 变量
     */
    // 输出流对象
    private OutputStream outputStream;


    private SocketClient() {

        mThreadPool = Executors.newCachedThreadPool();

        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // 创建Socket对象 & 指定服务端的IP 及 端口号
                    socket = new Socket("192.168.4.1", 333);

                    socket.setKeepAlive(true);

                    // 判断客户端和服务器是否连接成功
                    Log.d(TAG, "run: Server is already connected " + socket.isConnected());

                    if (socket.isConnected()) {
                        // 创建输入流对象InputStream
                        inputStream = socket.getInputStream();
                        // 创建输入流读取器对象 并传入输入流对象
                        // 该对象作用：获取服务器返回的数据
                        isr = new InputStreamReader(inputStream);
                        br = new BufferedReader(isr);

                        // 从Socket 获得输出流对象OutputStream
                        // 该对象作用：发送数据
                        outputStream = socket.getOutputStream();

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }




    public void sendMessage(final String message) {
        this.message = message;
        if (socket.isConnected()) {
            mThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    try {

                        // 写入需要发送的数据到输出流对象中
                        outputStream.write((message +"\n").getBytes("utf-8"));
                        // 特别注意：数据的结尾加上换行符才可让服务器端的readline()停止阻塞
                        // 发送数据到服务端
                        outputStream.flush();
                        Log.d(TAG, "run: already send message " + message + " ");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }



    public String receiveMessage() {
        if (socket.isConnected()) {
            try {
                // 通过输入流读取器对象 接收服务器发送过来的数据
                response = br.readLine();
                if (!response.equals(null))
                    Log.d(TAG, "receiveMessage: " + response);
                    return response;
                //Log.d(TAG, "run: test "+response.substring(response.indexOf("\"value\":")+8, response.indexOf("}]")));
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return null;
    }


    public void disconnectSocketServer() {
        try {


            socket.shutdownInput();
            socket.shutdownOutput();
            // 断开 客户端发送到服务器 的连接，即关闭输出流对象OutputStream
            outputStream.close();
            // 断开 服务器发送到客户端 的连接，即关闭输入流读取器对象BufferedReader
            br.close();
            // 最终关闭整个Socket连接
            inputStream.close();
            isr.close();
            socket.close();

            // 判断客户端和服务器是否已经断开连接
            Log.d(TAG, "disconnectSocketServer: " + socket.isConnected());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean getIsConnected() {
        if (socket != null) {
            return socket.isConnected();
        } else return false;

    }


    public static SocketClient getInstance() {
        return SocketClientHolder.socket;
    }

    //静态内部类
    public static class SocketClientHolder {
        private static final SocketClient socket = new SocketClient();
    }


}
