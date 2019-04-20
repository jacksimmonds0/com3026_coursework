package com.surrey.com3026.coursework.message;

import com.surrey.com3026.coursework.member.Member;
import com.surrey.com3026.coursework.member.Members;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.UnknownHostException;
import java.util.List;

public class MessageReceiver implements Runnable
{
    private static final int BUFFER_SIZE = 1024;

    private MessageSender sender;

    private Members members;

    private int port;

    public MessageReceiver(MessageSender sender, Members members, int port)
    {
        this.sender = sender;
        this.members = members;
        this.port = port;
    }

    @Override
    public void run()
    {
        try
        {
            DatagramSocket socket = new DatagramSocket(port);

            while(true)
            {
                byte[] buffer = new byte[BUFFER_SIZE];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String messageReceived = new String(packet.getData(), packet.getOffset(), packet.getLength());

                JAXBContext context = JAXBContext.newInstance(Message.class);
                Unmarshaller unmarshallerObj = context.createUnmarshaller();
                Message message = (Message) unmarshallerObj.unmarshal(new StringReader(messageReceived));

                // handle based on message
                // send a message to the joiner with info on all current members
                if (message.getType().equals(MessageTypes.JOIN_REQUEST))
                {
                    Member newJoiner = this.addNewJoiner(message);

                    sender.returnMessageToJoinRequest(newJoiner.getIp(), newJoiner.getPortNumber());
                }
                else if (message.getType().equals(MessageTypes.ALL_CURRENT_MEMBERS))
                {
                    List<Member> currentMembers = message.getMembers();
                    members.setMembers(currentMembers);

                    // send update members message to everyone
                    // except this node and the node we just got the message from
                    if (message.getResponder() != null)
                    {
                        sender.sendNewJoinerToCurrentMembers(message.getResponder());
                    }
                }
                else if (message.getType().equals(MessageTypes.NEW_JOINER))
                {
                    System.out.println(messageReceived);
                    this.addNewJoiner(message);

                    // send accepted_joiner
                    // need to check that all these accepted messages are received
                }
            }
        }
        catch (IOException | JAXBException e)
        {
            e.printStackTrace();
        }
    }

    private Member addNewJoiner(Message message) throws UnknownHostException
    {
        Member newJoiner = message.getMembers().get(0);

        // update this nodes list of members with the new joiner
        members.addMember(newJoiner);

        return newJoiner;
    }
}
