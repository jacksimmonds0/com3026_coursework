package com.surrey.com3026.coursework.message.sender;

import com.surrey.com3026.coursework.member.Member;
import com.surrey.com3026.coursework.member.Members;
import com.surrey.com3026.coursework.message.Message;
import com.surrey.com3026.coursework.message.MessageTypes;
import com.surrey.com3026.coursework.security.SignatureHandler;

import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Sends the list of all the current members, as a response to the member who has just sent a join request
 */
public class SendAllCurrentMembers extends AbstractMessageSender implements Runnable
{

    public SendAllCurrentMembers(Members members, InetAddress address, int port, Member thisNode,
                                 DatagramSocket socket, SignatureHandler signatureHandler)
    {
        super(members, address, port, thisNode, socket, signatureHandler);
    }

    @Override
    public void run()
    {
        Message message = new Message(MessageTypes.ALL_CURRENT_MEMBERS, super.members.getMembers(), thisNode);
        this.sendMessage(address, port, message);
    }
}