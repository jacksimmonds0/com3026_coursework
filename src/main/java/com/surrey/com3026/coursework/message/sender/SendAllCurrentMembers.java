package com.surrey.com3026.coursework.message.sender;

import com.surrey.com3026.coursework.member.Member;
import com.surrey.com3026.coursework.member.Members;
import com.surrey.com3026.coursework.message.Message;
import com.surrey.com3026.coursework.message.MessageTypes;

import java.net.DatagramSocket;
import java.net.InetAddress;

public class SendAllCurrentMembers extends AbstractMessageSender implements Runnable
{

    public SendAllCurrentMembers(Members members, InetAddress address, int port, Member thisNode, DatagramSocket socket)
    {
        super(members, address, port, thisNode, socket);
    }

    @Override
    public void run()
    {
        Message message = new Message(MessageTypes.ALL_CURRENT_MEMBERS, super.members.getMembers(), thisNode);
        this.sendMessage(address, port, message);
    }
}