package com.surrey.com3026.coursework.message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MessageReceiver implements Runnable
{
    private static final int BUFFER_SIZE = 1024;

    private MessageSender sender;

    private int port;

    public MessageReceiver(MessageSender sender, int port)
    {
        this.sender = sender;
        this.port = port;
    }

    @Override
    public void run()
    {
        try
        {
            DatagramSocket socket = new DatagramSocket(port);

            while(true)
            {
                byte[] buffer = new byte[BUFFER_SIZE];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String message = new String(packet.getData(), packet.getOffset(), packet.getLength());
                System.out.println(message);

                // handle based on message
                // send a message to the joiner with info on all current members
                if(message.startsWith("joiner"))
                {
                    InetAddress address = InetAddress.getByName("127.0.0.1");
                    int port = 8002;
                    sender.sendMessageAllMembers(address, port);
                }
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
