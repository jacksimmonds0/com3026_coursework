package com.surrey.com3026.coursework.message.sender;

import com.surrey.com3026.coursework.member.Member;
import com.surrey.com3026.coursework.member.Members;
import com.surrey.com3026.coursework.message.Message;
import com.surrey.com3026.coursework.message.MessageTypes;
import com.surrey.com3026.coursework.security.SignatureHandler;

import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * First message sent to the previous member (info entered at runtime) to get the current list of members
 */
public class JoinRequest extends AbstractMessageSender implements Runnable
{

    public JoinRequest(Members members, InetAddress address, int port, Member thisNode, DatagramSocket socket,
                       SignatureHandler signatureHandler)
    {
        super(members, address, port, thisNode, socket, signatureHandler);
    }

    @Override
    public void run()
    {
        Message message = new Message(MessageTypes.JOIN_REQUEST, thisNode);
        super.sendMessage(address, port, message);
    }
}

