package com.surrey.com3026.coursework.message.receiver;

import com.surrey.com3026.coursework.election.LeaderElection;
import com.surrey.com3026.coursework.member.Member;
import com.surrey.com3026.coursework.member.Members;
import com.surrey.com3026.coursework.message.Message;
import com.surrey.com3026.coursework.message.MessageTypes;
import com.surrey.com3026.coursework.message.sender.AcceptJoiner;
import com.surrey.com3026.coursework.message.sender.NewJoiner;
import com.surrey.com3026.coursework.message.sender.SendAllCurrentMembers;
import com.surrey.com3026.coursework.security.SignatureHandler;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Class that runs on a thread to consume messages placed on the blocking message queue by the {@link MessageReceiver}
 */
public class MessageConsumer implements Runnable
{
    private BlockingQueue messageQueue;

    private Members members;

    private DatagramSocket socket;

    private LeaderElection election;

    private Member thisNode;

    private SignatureHandler signatureHandler;

    private List<Member> membersToCheckAccepted = Collections.synchronizedList(new ArrayList<>());

    /**
     * Create a message consumer to take messages from the blocking queue of {@link Message} objects
     *
     * @param messageQueue
     *          the blocking queue of messages to take from
     * @param members
     *          the current members known within the joined group
     * @param socket
     *          the socket to send messages across on
     * @param election
     *          the implementation of {@link LeaderElection} to perform a leader election where necessary
     * @param thisNode
     *          the {@link Member} object representing this node that has been executed
     * @param signatureHandler
     *          the class to verify the sign of the received message and sign messages to send
     */
    public MessageConsumer(BlockingQueue messageQueue, Members members, DatagramSocket socket,
                           LeaderElection election, Member thisNode, SignatureHandler signatureHandler)
    {
        this.messageQueue = messageQueue;
        this.members = members;
        this.socket = socket;
        this.election = election;
        this.thisNode = thisNode;
        this.signatureHandler = signatureHandler;
    }

    @Override
    public void run()
    {
        try
        {
            // runs in parallel with MessageReceiver to take from the queue when possible
            while (true)
            {
                // take the message from the queue and then apply logic based on the type of message received
                Message message = (Message) messageQueue.take();

                // used to check digital signature is correct
                int responderId = message.getResponder().getId();
                byte[] signature = message.getSignature();

                if (signatureHandler.verify(responderId, signature))
                {
                    System.out.println("Signature verified from node " + responderId);
                    handleMessageType(message);
                }
                else
                {
                    // return message that message is not verified?
                }

            }
        }
        catch (InterruptedException | IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Taking the message and applying the logic to handle it appropriately based on the type
     * e.g. receiving a join request means update members with the joiner, and send back a list of all current members
     *
     * @param message
     *          the message to be handled
     * @throws UnknownHostException
     *          if the IP from a responder is unknown
     */
    private void handleMessageType(Message message) throws UnknownHostException
    {
        // send a message to the joiner with info on all current members
        if (message.getType().equals(MessageTypes.JOIN_REQUEST))
        {

            Member newJoiner = message.getResponder();
            // update this nodes list of members with the new joiner
            members.addMember(newJoiner);

            System.out.println("RECEIVED JOIN REQUEST");
            System.out.println("------------------------");
            System.out.println(members.toString());
            System.out.println("------------------------");


            SendAllCurrentMembers sender = new SendAllCurrentMembers(members, newJoiner.getIp(),
                    newJoiner.getPortNumber(), thisNode, socket, signatureHandler);
            new Thread(sender).start();
        }
        else if (message.getType().equals(MessageTypes.NEW_JOINER))
        {
            Member newJoiner = message.getResponder();
            // update this nodes list of members with the new joiner
            members.addMember(newJoiner);

            System.out.println("NEW MEMBER JOINED");
            System.out.println("------------------------");
            System.out.println(members.toString());
            System.out.println("------------------------");

            // send accept_joiner
            // need to check that all these accepted messages are received
            AcceptJoiner sender = new AcceptJoiner(members, newJoiner.getIp(),
                    newJoiner.getPortNumber(), thisNode, socket, signatureHandler);
            new Thread(sender).start();
        }
        else if (message.getType().equals(MessageTypes.ALL_CURRENT_MEMBERS))
        {
            List<Member> currentMembers = message.getMembers();
            members.setMembers(currentMembers);

            System.out.println("CURRENT MEMBERS LIST RECEIVED");
            System.out.println("-------------------------------");
            System.out.println(members.toString());
            System.out.println("-------------------------------");

            // send update members message to everyone
            // except this node and the node we just got the all_members message from
            if (message.getResponder() != null)
            {
                NewJoiner sender = new NewJoiner(members, thisNode, socket, message.getResponder(),
                        this, election, signatureHandler);
                new Thread(sender).start();
            }
        }
        else if (message.getType().equals(MessageTypes.ACCEPT_JOINER))
        {
            System.out.println("MEMBER HAS ACCEPTED ME");
            System.out.println("------------------------");
            System.out.println(message.getResponder().toString());
            this.removeMemberAccepted(message.getResponder());
            System.out.println("------------------------");
        }
        else if (message.getType().equals(MessageTypes.UPDATE_MEMBERS))
        {
            System.out.println("UPDATING MEMBERS");
            System.out.println("------------------------");
            System.out.println(message.getMembers().toString());
            System.out.println("------------------------");

            // updating the members list to the current members
            // due to some not responding
            members.setMembers(message.getMembers());
        }
        else if (message.getType().equals(MessageTypes.GET_INFO))
        {
            // helper method to get info (for testing purposes)
            // just sends this current list of members back
            Member responder = message.getResponder();

            System.out.println(members.toString());

            SendAllCurrentMembers sender = new SendAllCurrentMembers(members, responder.getIp(),
                    responder.getPortNumber(), thisNode, socket, signatureHandler);
            new Thread(sender).start();
        }
        else if (election.isElectionMessage(message.getType()))
        {
            election.handleElectionMessage(message);
        }
    }


    public void setThisNode(Member thisNode)
    {
        this.thisNode = thisNode;
    }

    public List<Member> getMembersToCheckAccepted()
    {
        return membersToCheckAccepted;
    }

    public void setMembersToCheckAccepted(List<Member> membersToCheckAccepted)
    {
        this.membersToCheckAccepted = membersToCheckAccepted;
    }

    private void removeMemberAccepted(Member member)
    {
        membersToCheckAccepted.remove(member);
    }
}
