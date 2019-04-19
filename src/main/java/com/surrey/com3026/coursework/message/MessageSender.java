package com.surrey.com3026.coursework.message;

import com.surrey.com3026.coursework.member.Member;
import com.surrey.com3026.coursework.member.Members;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MessageSender
{
    private Members members;

    public MessageSender(Members members)
    {
        this.members = members;
    }

    public void sendJoinerMessage(Member member, InetAddress address, int port)
    {
        String message = "joiner = " + member.toString();
        this.sendMessage(address, port, message);
    }

    public void sendMessageAllMembers(InetAddress address, int port)
    {
        this.sendMessage(address, port, members.toString());
    }

    private void sendMessage(InetAddress address, int port, String message)
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
