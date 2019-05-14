package com.surrey.com3026.coursework;

import com.surrey.com3026.coursework.member.Member;
import com.surrey.com3026.coursework.message.Message;
import com.surrey.com3026.coursework.message.MessageTypes;
import com.surrey.com3026.coursework.message.sender.AbstractMessageSender;
import com.surrey.com3026.coursework.security.SecurityConstants;
import com.surrey.com3026.coursework.security.SignatureHandler;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;

/**
 * Abstract class to hold the common methods and fields for integration testing of nodes joining the group
 */
public abstract class AbstractNodeTester
{
    /** Ensuring the messages have been sent/received by nodes, to simulate it like a real environment */
    protected static final int TIMEOUT = 5000;

    /** Longer timeout required for elections as need to wait for messages from election_initiate */
    protected static final int ELECTION_TIMEOUT = 15000;

    protected static final int TESTER_ID = 20;

    protected static final String LOG_FILENAME = "node.log";

    private List<Node> nodesInUse;

    protected DatagramSocket socket;

    @Before
    public void setUp() throws SocketException
    {
        this.nodesInUse = new ArrayList<>();
        this.socket = new DatagramSocket(8999);

        // only keep enabled on non Recovery_IT classes
        if (this.getClass() != Recovery_IT.class)
        {
            // disable logging (unless overridden)
            Logger.getLogger("com.surrey.com3026.coursework").setLevel(Level.OFF);
        }
    }

    /**
     * Create a node based on the arguments
     *
     * @param args
     *          the varargs for the node, 2 or 3 otherwise IllegalArgumentException is thrown (test fails)
     * @return the created {@link Node}
     */
    protected Node createNode(String... args)
    {
        Node node;
        if(args.length == 2)
        {
            node = new Node(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        }
        else if (args.length == 3)
        {
            node = new Node(Integer.parseInt(args[0]), Integer.parseInt(args[1]), args[2]);
        }
        else
        {
            throw new IllegalArgumentException("Starting parameters incorrect");
        }

        return node;
    }

    /**
     * Start all threads from the nodes parameter
     *
     * @param nodes
     *          all the nodes to start thread on
     * @throws InterruptedException when starting the node or on sleeping the thread
     */
    protected void startThreads(Node... nodes) throws InterruptedException
    {
        for(Node n : nodes)
        {
            this.nodesInUse.add(n);
            Thread t = createJoinGroupThread(n);

            t.start();
            Thread.sleep(500);
        }
    }

    /**
     * Create a join group thread
     *
     * @param node
     *          the node to create a thread from
     * @return the {@link Thread} for the node to be started
     */
    protected Thread createJoinGroupThread(Node node)
    {
        return new Thread(node::startNode);
    }

    /**
     * Using helper method to get the nodes current list of members for testing
     *
     * @param port
     *          the port to send to
     * @return the {@link Message} received from the node
     */
    protected Message getInfoFromNode(int port)
    {
        Message message = null;
        try
        {
            byte[] buffer = new byte[4096];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            Member tester = new Member(TESTER_ID, 8999);
            SignatureHandler signatureHandler = new SignatureHandler(getTesterKeyStore(TESTER_ID));

            GetInfoHelper sender = new GetInfoHelper(InetAddress.getByName("127.0.0.1"), port, tester, socket,
                    signatureHandler);
            new Thread(sender).start();

            socket.receive(packet);
            String messageReceived = new String(packet.getData(), packet.getOffset(), packet.getLength());

            message = unmarshallMessage(messageReceived);

        }
        catch (IOException | JAXBException e)
        {
            System.out.println(e);
            fail();
        }

        return message;
    }

    protected Message unmarshallMessage(String messageReceived) throws JAXBException
    {
        JAXBContext context = JAXBContext.newInstance(Message.class);
        Unmarshaller unmarshallerObj = context.createUnmarshaller();
        return (Message) unmarshallerObj.unmarshal(new StringReader(messageReceived));
    }

    protected KeyStore getTesterKeyStore(int id)
    {
        String keyStoreFilename = "src/test/resources/keystores/node-" + id + ".jks";
        KeyStore keyStore = null;

        try (InputStream is = new FileInputStream(keyStoreFilename))
        {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(is, SecurityConstants.PASSWORD);
        }
        catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e)
        {
            fail();
        }

        return keyStore;
    }

    /**
     * Helper method for assert two lists of members (regardless of order)
     *
     * @param expectedMembers
     *          the expected list of members to be returned
     * @param actualMembers
     *          the actual list of members received from the node
     */
    protected void assertMembersListSame(List<Member> expectedMembers, List<Member> actualMembers)
    {
        assertNotNull(actualMembers);
        assertTrue(expectedMembers.size() == actualMembers.size() &&
                expectedMembers.containsAll(actualMembers) && actualMembers.containsAll(expectedMembers));
    }


    @After
    public void tearDown()
    {
        Logger.getLogger("com.surrey.com3026.coursework").setLevel(Level.OFF);

        // after each test, close all sockets in use
        for (Node n : nodesInUse)
        {
            n.closeSocket();
        }
        nodesInUse.clear();
        this.socket.close();

        // delete log file so checkpoints not used
        File nodeLog = new File(LOG_FILENAME);
        boolean deleted = nodeLog.delete();
    }


    /**
     * Sends a get_info message to help with testing, used to retrieve the list of current members so when spinning up
     * and shutting down nodes we can be sure they are holding the correct information
     */
    private class GetInfoHelper extends AbstractMessageSender implements Runnable

    {

        public GetInfoHelper(InetAddress address, int port, Member thisNode, DatagramSocket socket,
                             SignatureHandler signatureHandler)
        {
            super(address, port, thisNode, socket, signatureHandler);
        }

        @Override
        public void run()
        {
            Message message = new Message(MessageTypes.GET_INFO, thisNode);
            this.sendMessage(address, port, message);
        }
    }

}

