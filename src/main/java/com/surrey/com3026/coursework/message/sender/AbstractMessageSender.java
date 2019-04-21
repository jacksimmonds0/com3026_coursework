package com.surrey.com3026.coursework.message.sender;

import com.surrey.com3026.coursework.member.Member;
import com.surrey.com3026.coursework.member.Members;
import com.surrey.com3026.coursework.message.Message;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.StringWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.Timestamp;

public abstract class AbstractMessageSender
{
    protected Members members;

    protected InetAddress address;

    protected int port;

    protected Member thisNode;

    public AbstractMessageSender(Members members, Member thisNode)
    {
        this.members = members;
        this.thisNode = thisNode;
    }

    public AbstractMessageSender(Members members, InetAddress address, int port, Member thisNode)
    {
        this.members = members;
        this.address = address;
        this.port = port;
        this.thisNode = thisNode;
    }


    protected void sendMessage(InetAddress address, int port, Message message)
    {
        String messageString = this.getMarshalledMessage(message);

        byte[] buffer = messageString.getBytes();

        DatagramPacket packet = new DatagramPacket(
                buffer, buffer.length, address, port
        );

        // creating socket for request/response
        try(DatagramSocket socket = new DatagramSocket();)
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
}
