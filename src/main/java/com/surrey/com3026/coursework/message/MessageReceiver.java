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
            DatagramSocket serverSocket = new DatagramSocket(port);
            byte[] receiveData = new byte[8];
            byte[] sendData = new byte[8];

            while(true)
            {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                String sentence = new String( receivePacket.getData());
                System.out.println("RECEIVED: " + sentence);

//                InetAddress IPAddress = receivePacket.getAddress();
//                String sendString = "polo";
//                sendData = sendString.getBytes();
//                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
//                serverSocket.send(sendPacket);
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
