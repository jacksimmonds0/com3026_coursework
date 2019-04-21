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

public class UpdateMembers extends AbstractMessageSender implements Runnable
{

    public UpdateMembers(Members members, Member thisNode)
    {
        super(members, thisNode);
    }

    @Override
    public void run()
    {
        // exclude this node and the initial join_request responder node
        List<Member> membersToMessage = new ArrayList<>(members.getMembers());
        membersToMessage.remove(thisNode);

        Message message = new Message(MessageTypes.UPDATE_MEMBERS, members.getMembers());
        sendMessageToMultipleMembers(message, membersToMessage);
    }
}
