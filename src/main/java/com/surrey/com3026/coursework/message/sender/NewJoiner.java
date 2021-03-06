package com.surrey.com3026.coursework.message.sender;

import com.surrey.com3026.coursework.election.LeaderElection;
import com.surrey.com3026.coursework.member.Member;
import com.surrey.com3026.coursework.member.Members;
import com.surrey.com3026.coursework.message.Message;
import com.surrey.com3026.coursework.message.MessageTypes;
import com.surrey.com3026.coursework.message.checker.MembersResponseChecker;
import com.surrey.com3026.coursework.message.receiver.MessageConsumer;
import com.surrey.com3026.coursework.security.SignatureHandler;

import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;

/**
 * Thread to send a new joiner request to all other current members (other than the initial responder)
 */
public class NewJoiner extends AbstractMessageSender implements Runnable
{
    private Member responder;

    private MessageConsumer consumer;

    private LeaderElection election;

    public NewJoiner(Members members, Member thisNode, DatagramSocket socket, Member responder,
                     MessageConsumer consumer, LeaderElection election, SignatureHandler signatureHandler)
    {
        super(members, thisNode, socket, signatureHandler);
        this.responder = responder;
        this.consumer = consumer;
        this.election = election;
    }

    @Override
    public void run()
    {
        // exclude this node and the initial join_request responder node
        List<Member> membersToMessage = new ArrayList<>(members.getMembers());
        membersToMessage.remove(thisNode);

        if (responder != null)
        {
            membersToMessage.remove(responder);
        }

        if(membersToMessage.isEmpty())
        {
            // when the only other member is the initial responder, just return
            return;
        }

        consumer.setMembersToCheckAccepted(membersToMessage);

        Message message = new Message(MessageTypes.NEW_JOINER, thisNode);
        sendMessageToMultipleMembers(message, membersToMessage);

        // add listener to ensure all members are responding
        MembersResponseChecker checker = new MembersResponseChecker(consumer, members, thisNode, socket,
                election, signatureHandler);
        new Thread(checker).start();
    }


}
