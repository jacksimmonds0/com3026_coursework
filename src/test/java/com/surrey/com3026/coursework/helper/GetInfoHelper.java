package com.surrey.com3026.coursework.helper;

import com.surrey.com3026.coursework.member.Member;
import com.surrey.com3026.coursework.member.Members;
import com.surrey.com3026.coursework.message.Message;
import com.surrey.com3026.coursework.message.MessageTypes;
import com.surrey.com3026.coursework.message.sender.AbstractMessageSender;

import java.net.DatagramSocket;
import java.net.InetAddress;

public class GetInfoHelper extends AbstractMessageSender implements Runnable

{
    public GetInfoHelper(InetAddress address, int port, Member thisNode, DatagramSocket socket)
    {
        super(address, port, thisNode, socket);
    }

    @Override
    public void run()
    {
        Message message = new Message(MessageTypes.GET_INFO, thisNode);
        this.sendMessage(address, port, message);
    }
}
