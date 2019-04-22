package com.surrey.com3026.coursework.message;

import com.surrey.com3026.coursework.election.LeaderElection;
import com.surrey.com3026.coursework.member.Member;
import com.surrey.com3026.coursework.member.Members;
import com.surrey.com3026.coursework.message.sender.AcceptJoiner;
import com.surrey.com3026.coursework.message.sender.NewJoiner;
import com.surrey.com3026.coursework.message.sender.SendAllCurrentMembers;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Class to listen for messages from other nodes that have joined or are trying to join the group
 */
public class MessageReceiver implements Runnable
{
    private static final int BUFFER_SIZE = 1024;

    private Members members;

    private DatagramSocket socket;

    private LeaderElection election;

    private Member thisNode;

    private List<Member> membersToCheckAccepted = Collections.synchronizedList(new ArrayList<>());

    public MessageReceiver(Members members, Member thisNode, DatagramSocket socket, LeaderElection election)
    {
        this.members = members;
        this.thisNode = thisNode;
        this.socket = socket;
        this.election = election;
    }

    @Override
    public void run()
    {
        try
        {
            // continuous loop, always need to listen for messages while the node is still running
            while(true)
            {

                byte[] buffer = new byte[BUFFER_SIZE];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                socket.receive(packet);
                String messageReceived = new String(packet.getData(), packet.getOffset(), packet.getLength());

                JAXBContext context = JAXBContext.newInstance(Message.class);
                Unmarshaller unmarshallerObj = context.createUnmarshaller();
                Message message = (Message) unmarshallerObj.unmarshal(new StringReader(messageReceived));

                if (message != null)
                {
                    this.handleMessageType(message);
                }
            }
        }
        catch (IOException | JAXBException  e)
        {
            e.printStackTrace();
        }
    }

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
                    newJoiner.getPortNumber(), thisNode, socket);
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
                    newJoiner.getPortNumber(), thisNode, socket);
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
                        this, election);
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
