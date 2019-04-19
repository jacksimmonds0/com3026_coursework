package com.surrey.com3026.coursework.message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MessageReceiver implements Runnable
{
    private int port;

    public MessageReceiver(int port)
    {
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
                byte[] buffer = new byte[1024];
                byte[] sendData = new byte[8];

                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String message = new String(packet.getData(), packet.getOffset(), packet.getLength());
                System.out.println("RECEIVED: " + message);

//                InetAddress IPAddress = packet.getAddress();
//                String sendString = "polo";
//                sendData = sendString.getBytes();
//                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
//                socket.send(sendPacket);
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
