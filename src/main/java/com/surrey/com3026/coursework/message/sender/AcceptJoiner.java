package com.surrey.com3026.coursework.message.sender;

import com.surrey.com3026.coursework.member.Member;
import com.surrey.com3026.coursework.member.Members;
import com.surrey.com3026.coursework.message.Message;
import com.surrey.com3026.coursework.message.MessageTypes;

import java.net.InetAddress;

public class AcceptJoiner extends AbstractMessageSender implements Runnable
{

    public AcceptJoiner(Members members, InetAddress address, int port, Member thisNode)
    {
        super(members, address, port, thisNode);
    }

    @Override
    public void run()
    {
        Message message = new Message(MessageTypes.ACCEPT_JOINER, thisNode);
        this.sendMessage(address, port, message);
    }
}

