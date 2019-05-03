package com.surrey.com3026.coursework.message.sender;

import com.surrey.com3026.coursework.member.Member;
import com.surrey.com3026.coursework.member.Members;
import com.surrey.com3026.coursework.message.Message;
import com.surrey.com3026.coursework.security.SignatureHandler;

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
import java.util.List;

/**
 * Abstract class for common functionality to send messages to other members within the group
 */
public abstract class AbstractMessageSender
{
    protected Members members;

    protected InetAddress address;

    protected int port;

    protected Member thisNode;

    protected DatagramSocket socket;

    protected SignatureHandler signatureHandler;

    /**
     * For testing purposes
     */
    public AbstractMessageSender(InetAddress address, int port, Member thisNode, DatagramSocket socket)
    {
        this.address = address;
        this.port = port;
        this.thisNode = thisNode;
        this.socket = socket;
    }

    public AbstractMessageSender(Members members, Member thisNode, DatagramSocket socket,
                                 SignatureHandler signatureHandler)
    {
        this.members = members;
        this.thisNode = thisNode;
        this.socket = socket;
        this.signatureHandler = signatureHandler;
    }

    public AbstractMessageSender(Members members, InetAddress address, int port, Member thisNode,
                                 DatagramSocket socket, SignatureHandler signatureHandler)
    {
        this(members, thisNode, socket, signatureHandler);
        this.address = address;
        this.port = port;
    }


    protected void sendMessage(InetAddress address, int port, Message message)
    {
        // TODO change this to a class variable?
//
//        String messageToSign = message.getType() + " from node-" + thisNode.getId();
//        byte[] dataToSign = messageToSign.getBytes();
        message.setSignature(signatureHandler.sign(message.toString().getBytes()));

        String messageString = this.getMarshalledMessage(message);
        byte[] buffer = messageString.getBytes();

        DatagramPacket packet = new DatagramPacket(
                buffer, buffer.length, address, port
        );

        // creating socket for request/response
        try
        {
            socket.send(packet);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private String getMarshalledMessage(Message message)
    {
        String messageString = "";
        try
        {
            JAXBContext context = JAXBContext.newInstance(Message.class);

            Marshaller marshallObj = context.createMarshaller();
            marshallObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            final StringWriter dataWriter = new StringWriter();
            marshallObj.marshal(message, dataWriter);

            messageString = dataWriter.toString();
        }
        catch (JAXBException e)
        {
            e.printStackTrace();
        }

        return messageString;
    }

    protected void sendMessageToMultipleMembers(Message message, List<Member> membersToMessage)
    {
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

            sendMessage(mAddress, m.getPortNumber(), message);
        }
    }

    protected void broadcastMessage(Message message)
    {
        // broadcasting a message to all other members, so exclude this node
        List<Member> membersToMessage = new ArrayList<>(members.getMembers());
        membersToMessage.remove(thisNode);

        this.sendMessageToMultipleMembers(message, membersToMessage);
    }

}
