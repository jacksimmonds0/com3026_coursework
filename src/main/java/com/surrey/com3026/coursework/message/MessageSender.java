package com.surrey.com3026.coursework.message;

import com.surrey.com3026.coursework.member.Member;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MessageSender
{

    public void sendMessage(InetAddress address, int port, String message)
    {
        byte[] buffer = message.getBytes();

        DatagramPacket packet = new DatagramPacket(
                buffer, buffer.length, address, port
        );

        try (DatagramSocket datagramSocket = new DatagramSocket()) {
            datagramSocket.send(packet);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
