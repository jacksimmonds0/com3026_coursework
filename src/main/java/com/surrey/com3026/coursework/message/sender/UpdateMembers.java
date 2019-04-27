package com.surrey.com3026.coursework.message.sender;

import com.surrey.com3026.coursework.member.Member;
import com.surrey.com3026.coursework.member.Members;
import com.surrey.com3026.coursework.message.Message;
import com.surrey.com3026.coursework.message.MessageTypes;
import com.surrey.com3026.coursework.security.SignatureHandler;

import java.net.DatagramSocket;

public class UpdateMembers extends AbstractMessageSender implements Runnable
{

    public UpdateMembers(Members members, Member thisNode, DatagramSocket socket, SignatureHandler signatureHandler)
    {
        super(members, thisNode, socket, signatureHandler);
    }

    @Override
    public void run()
    {
        Message message = new Message(MessageTypes.UPDATE_MEMBERS, members.getMembers());
        broadcastMessage(message);
    }
}
