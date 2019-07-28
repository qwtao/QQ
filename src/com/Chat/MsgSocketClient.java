package com.Chat;

import com.Interface.FriendView;
import com.Interface.friend;

import java.awt.*;
import java.io.*;
import java.math.RoundingMode;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class MsgSocketClient {
    Socket socket = null;
    InputStreamReader input = null;
    InputStream in = null;
    OutputStream out = null;
    int admin_id;
    ArrayList<String> receive = new ArrayList<>();
    public ArrayList<Integer> chat_open = new ArrayList<>();    // 记录私聊的窗口，元素是私聊的朋友的id值
    int i;
    FileInputStream fis;
    FileOutputStream fos;
    DataOutputStream dos;
    DataInputStream dis;
    private static DecimalFormat df = null;
    static {
        // 设置数字格式，保留一位有效小数
        df = new DecimalFormat("#0.0");
        df.setRoundingMode(RoundingMode.HALF_UP);
        df.setMinimumFractionDigits(1);
        df.setMaximumFractionDigits(1);
    }

    public void socketStart(int admin_id){
        this.admin_id = admin_id;
        try{
            //this.sessionMap = sessionMap;
            this.socket = new Socket("127.0.0.1", 8888);
            System.out.println("客户端启动.......");
            chatsend(admin_id, admin_id+":login");
            //System.out.println(admin_id+":login");
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private String getFormatFileSize(long length) {
        double size = ((double) length) / (1 << 30);
        if(size >= 1) {
            return df.format(size) + "GB";
        }
        size = ((double) length) / (1 << 20);
        if(size >= 1) {
            return df.format(size) + "MB";
        }
        size = ((double) length) / (1 << 10);
        if(size >= 1) {
            return df.format(size) + "KB";
        }
        return length + "B";
    }

    public Thread chat(int friend_id, PrivateChat pc){
        PrivateChat privateChat = pc;
        Socket socket = this.socket;
        // 用户每开一个私聊的窗口，就会新建一个线程，处理该窗口对方的消息（实质上是对方向服务器发送消息，由服务器转发到该用户）
        // 问题：当关闭窗口时，该线程应该被中止掉。
        // 解决方法：将线程对象返回，在PrivateChat中增加关闭窗口时的处理代码——将线程终止。

        try{
            // 接受返回数据
            Thread thread = new Thread() {
                public void run() {
                    try {
                        while (true) {
                            receive = privateChat.client.receive;
                            for(int i = 0; i < receive.size(); i++){
                                if(receive.get(i)!=null && receive.get(i).split(":")[0].equals(Integer.toString(friend_id))){
                                    String friend_name = new friend().get_id(Integer.parseInt(receive.get(i).split(":")[0]));
                                    privateChat.dialog.append(friend_name + ":\n" + receive.get(i).split(":")[1]+"\n");
                                    receive.remove(i);
                                }
                                else if(receive.get(i)!=null && receive.get(i).split(":")[0].equals("file")){
                                    if(receive.get(i).split(":")[1].equals(Integer.toString(friend_id))){
                                        String friend_name = new friend().get_id(Integer.parseInt(receive.get(i).split(":")[1]));
                                        String file_name = receive.get(i).split(":")[2];
                                        privateChat.dialog.append("Already received file : " + file_name + " from " + new friend().get_id(friend_id));
                                        receive.remove(i);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();
            return thread;
        }catch (Exception e){
            e.getStackTrace();
        }
        return null;
    }

    public Thread pchat(int group_id, PublicChat publicchat){
        PublicChat publicChat = publicchat;
        Socket socket = this.socket;
        // 类似于chat方法，用户每开一个群聊的窗口，也会新建一个线程，处理该群聊中其他用户的消息
        // （实质上是其他用户向服务器发送消息，由服务器转发到该用户）
        // 问题：当关闭群聊窗口时，该线程应该被中止掉。
        // 解决方法：将线程对象返回，在PublicChat中增加关闭窗口时的处理代码——将线程终止。
        try{
            // 接受返回数据
            Thread thread = new Thread() {
                public void run() {
                    try {
                        while (true) {
                            for(int i = 0; i < receive.size(); i++){
                                //System.out.println(Integer.toString(friend_id));
                                if(receive.get(i)!=null && receive.get(i).split(":")[0].equals("g"+Integer.toString(group_id))){
                                    String friend_name = new friend().get_id(Integer.parseInt(receive.get(i).split(":")[1]));
                                    publicChat.dialog.append(friend_name + ":\n" + receive.get(i).split(":")[2]+"\n");
                                    //System.out.println("receive "+receive.get(i));
                                    receive.remove(i);
                                }
                                else if(receive.get(i)!=null && receive.get(i).split(":")[0].equals("online")){
                                    // 将在线的人的背景置为绿色
                                    for(int j = 0; j < receive.get(i).split(":")[1].split(",").length; j++){

                                        if(publicChat.str.contains(Integer.parseInt((receive.get(i).split(":")[1].split(","))[j]))){

                                            int index = Integer.parseInt(receive.get(i).split(":")[1].split(",")[j]);
                                            //index = online_id
                                            for(int k = 0; k < publicChat.jbls.length; k++){
                                                if(publicChat.jbls[k].getText().equals(Integer.toString(index))){
                                                    publicChat.jbls[k].setBackground(Color.green);
                                                    //System.out.println("set green");
                                                }
                                            }
                                        }
                                    }
                                    receive.remove(i);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();
            return thread;
        }catch (Exception e){
            e.getStackTrace();
        }
        return null;
    }

    public void chatreceive(){
        /*
        用来接收服务器发来的消息，将它们存在this.receive中
         */
        Socket socket = this.socket;
        try{
            // 接受返回数据
            new Thread() {
                public void run() {
                    try {
                        while (true) {
                            in = socket.getInputStream();
                            ObjectInputStream ois = new ObjectInputStream(in);
                            Message msg = (Message) ois.readObject();
                            System.out.println("received message: "+msg.getMsg());

                            if(msg.getMsg().split(":")[0].equals("file")){
                                //in = socket.getInputStream();
                                dis = new DataInputStream(in);
                                String fileName = dis.readUTF();
                                receive.add("file:" + msg.getMsg().split(":")[2] + ":" + fileName);
                                long fileLength = dis.readLong();
                                File directory = new File("D:\\"+admin_id);
                                if(!directory.exists()) {
                                    directory.mkdir();
                                }
                                File file = new File(directory.getAbsolutePath() + File.separatorChar + fileName);
                                fos = new FileOutputStream(file);

                                // 开始接收文件
                                byte[] bytes = new byte[1024];
                                int length = 0;
                                Long temp = fileLength;
                                while(temp > 0) {
                                    length = dis.read(bytes, 0, bytes.length);
                                    fos.write(bytes, 0, length);
                                    fos.flush();
                                    temp -= length;
                                }
                                System.out.println("======== 文件接收成功 [File Name：" + fileName + "] [Size：" + getFormatFileSize(fileLength) + "] ========");
                            }
                            else{
                                receive.add(msg.getMsg());
                                if(!msg.getMsg().equals("") && !msg.getMsg().split(":")[0].equals("online")){
                                    // 如果收到了A的消息，却没有打开和A单聊的窗口，则弹出一个单聊窗口
                                    if(chat_open==null || !chat_open.contains(Integer.parseInt(msg.getMsg().split(":")[0]))){
                                        int friend_id = Integer.parseInt(msg.getMsg().split(":")[0]);
                                        new PrivateChat(admin_id,new friend().get_id(friend_id),MsgSocketClient.this);
                                        chat_open.add(friend_id);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }catch (Exception e){
            e.getStackTrace();
        }
    }


    public void chatsend(int friend_id, String messsage) {
        /*
            向朋友发送消息
         */
        try{
            socket = this.socket;
            out = socket.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(out);
            input = new InputStreamReader(System.in);
            Message msg = new Message();
            msg.setIP(Integer.toString(friend_id));
            msg.setMsg(messsage);
            oos.writeObject(msg);
            System.out.println("已发送："+msg.getMsg());
        }catch (Exception e){
            e.getStackTrace();
        }
    }

    public void groupsend(String messsage, ArrayList<Integer> list) {
        /*
            向群中的每个人发送消息
         */
        try {
            socket = this.socket;
            out = socket.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(out);
            input = new InputStreamReader(System.in);
            Message msg = new Message();
            for (i = 0; i < list.size(); i++) {
                msg.setIP(Integer.toString(list.get(i)));
                msg.setMsg(messsage);
                System.out.println("已发送" + msg.getMsg());
                oos.writeObject(msg);
            }
        }catch (Exception e) {
            e.getStackTrace();
        }
    }

    public void check_online(int admin_id, ArrayList<Integer> online){
        try{
            socket = this.socket;
            out = socket.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(out);
            input = new InputStreamReader(System.in);
            Message msg = new Message();
            String message = "checkonline:";
            message += online.get(0);
            for(int i = 1 ; i < online.size(); i++){
                message += "," + online.get(i);
            }
            msg.setMsg(message);
            msg.setIP(Integer.toString(admin_id));
            oos.writeObject(msg);
        }catch (Exception e){
            e.getStackTrace();
        }
    }

    public void filesend(File file, int friend_id, int admin_id) throws Exception{
        /*
                向好友传输文件。传输流程：向服务器传输
                    1. file: + friend_id + admin_id
                    2. 文件名和长度
                    3. 文件内容
                之后，由服务器向friend传输文件
         */
        try {
            socket = this.socket;
            out = socket.getOutputStream();
            fis = new FileInputStream(file);
            dos = new DataOutputStream(out);

            ObjectOutputStream oos = new ObjectOutputStream(out);
            //input = new InputStreamReader(System.in);
            Message msg = new Message();
            String message = "file:"+friend_id+":"+admin_id;
            msg.setMsg(message);
            msg.setIP(Integer.toString(friend_id));
            oos.writeObject(msg);

            // 文件名和长度
            dos.writeUTF(file.getName());
            dos.flush();
            dos.writeLong(file.length());
            dos.flush();

            // 开始传输文件
            System.out.println("======== 开始传输文件 ========");
            byte[] bytes = new byte[1024];
            int length = 0;
            long progress = 0;
            while((length = fis.read(bytes, 0, bytes.length)) != -1) {
                dos.write(bytes, 0, length);
                dos.flush();
                progress += length;
                System.out.print("| " + (100*progress/file.length()) + "% |");
            }

            System.out.println();
            System.out.println("======== 文件传输成功 ========");


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
