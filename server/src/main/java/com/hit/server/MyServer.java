package com.hit.server;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import sun.nio.cs.UTF_8;


public class MyServer {
    public static final int PORT = 8899;
    private List<Socket> mList = new ArrayList<>();
    private ServerSocket server = null;


    public static void main(String[]args){
        new MyServer();
    }

    public MyServer() {
        try{
            InetAddress addr = InetAddress.getLocalHost();
            System.out.println("Local host:"+addr);

            //1.创建SeverSocket
            server = new ServerSocket(PORT);
            //创建线程池
            System.out.println("--服务器开启中--");
            while(true){
                //2.等待请求结束 这里接受客户端的请求
                Socket client = server.accept();
                System.out.println("客户端连接：" + client);
                mList.add(client);
                //初始化完成
                new Thread(new Service(client)).start();;
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class Service implements Runnable{
        private Socket socket;
        private BufferedReader in = null;
        private String content = "";

        public Service(Socket clientsocket) {
            this.socket = clientsocket;
            try{
                //3.接受请求后创建链接socket
                //4.通过InputStream 和 outputStream进行通信
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));

                content = "用户" + this.socket.getInetAddress() + "加入了聊天室" + "当前人数：" + mList.size();
                this.sendmsg();

            } catch (IOException e){
                e.printStackTrace();
            }
        }

        public void sendmsg() {
            System.out.println(content);
            int num = mList.size();
            for (int index = 0; index < num; index++){
                Socket mSocket = mList.get(index);
                PrintWriter pout = null;
                try{
                    pout = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(mSocket.getOutputStream(),"UTF-8")),true);
                    pout.println(content);
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void run() {
            try{
                while((content = in.readLine())!=null){
                    for (Socket s:mList){
                        System.out.println("从客户端收到的消息为：" + content);
                        if (content.equals("bye")){
                            System.out.println("~~~~~~~~~~");
                            mList.remove(socket);
                            in.close();
                            content = "用户" + this.socket.getInetAddress() + "退出" + "当前在线人数：" + mList.size();
                            socket.close();
                            this.sendmsg();
                            break;
                        } else {
                            content = socket.getInetAddress() + "说：" + content;
                            this.sendmsg();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

}