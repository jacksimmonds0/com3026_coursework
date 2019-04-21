package com.surrey.com3026.coursework.message.sender;

import com.surrey.com3026.coursework.member.Member;
import com.surrey.com3026.coursework.member.Members;
import com.surrey.com3026.coursework.message.Message;
import com.surrey.com3026.coursework.message.MessageReceiver;
import com.surrey.com3026.coursework.message.MessageTypes;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class NewJoiner extends AbstractMessageSender implements Runnable
{
    private Member responder;

    private MessageReceiver receiver;

    public NewJoiner(Members members, Member thisNode, Member responder, MessageReceiver receiver)
    {
        super(members, thisNode);
        this.responder = responder;
        this.receiver = receiver;
    }

    @Override
    public void run()
    {
        // exclude this node and the initial join_request responder node
        List<Member> membersToMessage = new ArrayList<>(members.getMembers());
        membersToMessage.remove(thisNode);
        membersToMessage.remove(responder);

        if(membersToMessage.isEmpty())
        {
            // when the only other member is the initial responder, just return
            return;
        }

        this.receiver.setTimeMembersCheckStart(System.currentTimeMillis());
        this.receiver.setMembersToCheckAccepted(membersToMessage);

        Message message = new Message(MessageTypes.NEW_JOINER, thisNode);
        for(Member m : membersToMessage)
        {
            InetAddress mAddress = null;
            try
            {
                mAddress = m.getIp();
            }
            catch (UnknownHostException e)
            {
                e.printStackTrace();
            }

            super.sendMessage(mAddress, m.getPortNumber(), message);
        }
    }
}
