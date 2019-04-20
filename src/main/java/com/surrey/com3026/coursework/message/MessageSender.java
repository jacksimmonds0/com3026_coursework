package com.surrey.com3026.coursework.message;

import com.surrey.com3026.coursework.member.Member;
import com.surrey.com3026.coursework.member.Members;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.StringWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MessageSender
{
    private Members members;

    private Member thisNode;

    public MessageSender(Members members)
    {
        this.members = members;
    }

    public void sendJoinerRequest(Member member, InetAddress address, int port)
    {
        Message message = new Message(MessageTypes.JOIN_REQUEST, Collections.singletonList(member));
        this.sendMarshalledMessage(address, port, message);
    }

    public void returnMessageToJoinRequest(InetAddress address, int port)
    {
        Message message = new Message(MessageTypes.ALL_CURRENT_MEMBERS, members.getMembers(), thisNode);
        this.sendMarshalledMessage(address, port, message);
    }

    public void sendNewJoinerToCurrentMembers(Member responder) throws UnknownHostException
    {
        // exclude this node and the initial join_request responder node
        List<Member> membersToMessage = new ArrayList<>(members.getMembers());
        membersToMessage.remove(thisNode);
        membersToMessage.remove(responder);

        if(membersToMessage.isEmpty())
        {
            return;
        }

        Message message = new Message(MessageTypes.NEW_JOINER, Collections.singletonList(thisNode));
        for(Member m : membersToMessage)
        {
            this.sendMarshalledMessage(m.getIp(), m.getPortNumber(), message);
        }

        // create listener to ensure all X responses received for accepting the new joiner
    }


    private void sendMessage(InetAddress address, int port, String message)
    {
        byte[] buffer = message.getBytes();

        DatagramPacket packet = new DatagramPacket(
                buffer, buffer.length, address, port
        );

        try (DatagramSocket datagramSocket = new DatagramSocket()) {
            datagramSocket.send(packet);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void sendMarshalledMessage(InetAddress address, int port, Message message)
    {
        try
        {
            JAXBContext context = JAXBContext.newInstance(Message.class);

            Marshaller marshallObj = context.createMarshaller();
            marshallObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            final StringWriter dataWriter = new StringWriter();
            marshallObj.marshal(message, dataWriter);

            this.sendMessage(address, port, dataWriter.toString());
        }
        catch (JAXBException e)
        {
            e.printStackTrace();
        }
    }

    public void setThisNode(Member thisNode)
    {
        this.thisNode = thisNode;
    }
}
