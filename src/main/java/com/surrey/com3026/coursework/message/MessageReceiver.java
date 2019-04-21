package com.surrey.com3026.coursework.message;

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
 * Class to listen for messages for new joiner requests, and updating members with a new joiner
 *
 */
public class MessageReceiver implements Runnable
{
    private static final int BUFFER_SIZE = 1024;

    private Members members;

    private DatagramSocket socket;

    private Member thisNode;

    private long timeMembersCheckStart = 0;

    private List<Member> membersToCheckAccepted = Collections.synchronizedList(new ArrayList<>());

    public MessageReceiver(Members members, DatagramSocket socket)
    {
        this.members = members;
        this.socket = socket;
    }

    @Override
    public void run()
    {
        try
        {
            // continuous loop, always need to listen for messages while the node is still running
            while(true)
            {

                if (timeMembersCheckStart != 0)
                {
                    long elapsed = System.currentTimeMillis() - timeMembersCheckStart;
                    if (elapsed > 10000)
                    {
                        // members not responding
                        System.out.println("MEMBERS NOT RESPONDING");
                        System.out.println(membersToCheckAccepted.toString());
                    }
                }

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
                    newJoiner.getPortNumber(), thisNode);
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
                    newJoiner.getPortNumber(), thisNode);
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
                NewJoiner sender = new NewJoiner(members, thisNode, message.getResponder(), this);
                new Thread(sender).start();
            }
        }
        else if (message.getType().equals(MessageTypes.ACCEPT_JOINER))
        {
            System.out.println("MEMBER HAS ACCEPTED ME");
            System.out.println("------------------------");
            System.out.println(message.getResponder().toString());

            System.out.println(membersToCheckAccepted.toString());
            this.removeMemberAccepted(message.getResponder());
            System.out.println(membersToCheckAccepted.toString());

            System.out.println("------------------------");
        }
    }



    public void setThisNode(Member thisNode)
    {
        this.thisNode = thisNode;
    }

    public void setTimeMembersCheckStart(long timeMembersCheckStart)
    {
        this.timeMembersCheckStart = timeMembersCheckStart;
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
