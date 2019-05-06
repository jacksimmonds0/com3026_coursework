package com.surrey.com3026.coursework.message.sender;

import com.surrey.com3026.coursework.member.Member;
import com.surrey.com3026.coursework.member.Members;
import com.surrey.com3026.coursework.message.Message;
import com.surrey.com3026.coursework.security.SignatureHandler;
import org.apache.log4j.Logger;

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
    private static final Logger LOG = Logger.getLogger(AbstractMessageSender.class);

    protected Members members;

    protected InetAddress address;

    protected int port;

    protected Member thisNode;

    protected DatagramSocket socket;

    protected SignatureHandler signatureHandler;

    /**
     * For testing purposes
     */
    public AbstractMessageSender(InetAddress address, int port, Member thisNode, DatagramSocket socket,
                                 SignatureHandler signatureHandler)
    {
        this.address = address;
        this.port = port;
        this.thisNode = thisNode;
        this.socket = socket;
        this.signatureHandler = signatureHandler;
    }

    /**
     * Constructor for fields required to send messages to other nodes
     *
     * @param members
     *          the current list of members
     * @param thisNode
     *          information on the node this is running on
     * @param socket
     *          the {@link DatagramSocket} to send messages on
     * @param signatureHandler
     *          the handler for signing digital signatures
     */
    public AbstractMessageSender(Members members, Member thisNode, DatagramSocket socket,
                                 SignatureHandler signatureHandler)
    {
        this.members = members;
        this.thisNode = thisNode;
        this.socket = socket;
        this.signatureHandler = signatureHandler;
    }

    /**
     * Constructor for fields required to send messages to other nodes
     *
     * @param members
     *          the current list of members
     * @param thisNode
     *          information on the node this is running on
     * @param address
     *          the {@link InetAddress} to send the message to
     * @param port
     *          the port number of the node the message is being sent to
     * @param thisNode
     *          information on the node this is running on
     * @param socket
     *          the {@link DatagramSocket} to send messages on
     * @param signatureHandler
     *          the handler for signing digital signatures
     */
    public AbstractMessageSender(Members members, InetAddress address, int port, Member thisNode,
                                 DatagramSocket socket, SignatureHandler signatureHandler)
    {
        this(members, thisNode, socket, signatureHandler);
        this.address = address;
        this.port = port;
    }

    /**
     * Sends a message to another node with a known IP address and port number
     *
     * @param address
     *          the IP address to send the message to
     * @param port
     *          the port number to send the message to
     * @param message
     *          the {@link Message} object to be sent (marshalled)
     */
    protected void sendMessage(InetAddress address, int port, Message message)
    {
        LOG.debug("Sending message to: " + address.getHostAddress() + ":" + port);
        LOG.debug("Message being sent: " + message);
        LOG.debug("Signing the hashed message with private key");

        // set signature using message toString method
        // within this class the message is hashed using SHA-256 before signing with this nodes private key
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
            LOG.error("Error while attempting to send the message on socket", e);
        }
    }

    /**
     * Marshall the message to XML before sending across within a packet
     *
     * @param message
     *          the object to serialize to XML
     * @return the String of the serialized message object
     */
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
            LOG.error("Error while marshalling the message to be sent", e);
        }

        return messageString;
    }

    /**
     * Helper method to send a message to multiple members
     *
     * @param message
     *          the message to send
     * @param membersToMessage
     *          the {@link List} of members to send the message to
     */
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
                LOG.error("Unknown IP host address from member: " + m, e);
            }

            sendMessage(mAddress, m.getPortNumber(), message);
        }
    }

    /**
     * Broadcast the message to all other members within the current {@link Members} list
     *
     * @param message
     *          the {@link Message} to broadcast
     */
    protected void broadcastMessage(Message message)
    {
        // broadcasting a message to all other members, so exclude this node
        List<Member> membersToMessage = new ArrayList<>(members.getMembers());
        membersToMessage.remove(thisNode);

        this.sendMessageToMultipleMembers(message, membersToMessage);
    }

}
