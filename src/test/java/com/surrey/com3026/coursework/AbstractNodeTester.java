package com.surrey.com3026.coursework;

import com.surrey.com3026.coursework.member.Member;
import com.surrey.com3026.coursework.message.Message;
import com.surrey.com3026.coursework.message.MessageTypes;
import com.surrey.com3026.coursework.message.sender.AbstractMessageSender;
import com.surrey.com3026.coursework.security.SignatureHandler;
import com.surrey.com3026.coursework.security.generator.KeyGenerator;
import org.junit.After;
import org.junit.Before;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.fail;

/**
 * Abstract class to hold the common methods and fields for integration testing of nodes joining the group
 */
public abstract class AbstractNodeTester
{
    protected static final int TIMEOUT = 5000;

    private List<Node> nodesInUse;

    protected DatagramSocket socket;

    @Before
    public void setUp() throws Exception
    {
        this.nodesInUse = new ArrayList<>();
        this.socket = new DatagramSocket(8999);
    }

    protected Node createNode(String... args)
    {
        Node node = null;
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

    protected Thread createJoinGroupThread(Node node)
    {
        return new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                node.startNode();
            }
        });
    }

    protected Message getInfoFromNode(int port)
    {
        Message message = null;
        try
        {
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            int id = 20;
            Member tester = new Member(id, 8999);
            SignatureHandler signatureHandler = new SignatureHandler(getTesterKeyStore(id));

            GetInfoHelper sender = new GetInfoHelper(InetAddress.getByName("127.0.0.1"), port, tester, socket,
                    signatureHandler);
            new Thread(sender).start();

            socket.receive(packet);
            String messageReceived = new String(packet.getData(), packet.getOffset(), packet.getLength());
            message = unmarshallMessage(messageReceived);

        }
        catch (IOException | JAXBException e)
        {
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
        String keyStoreFilename = "node-" + id + ".jks";
        KeyStore keyStore = null;

        try (InputStream is = new FileInputStream(keyStoreFilename))
        {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(is, KeyGenerator.PASSWORD);
        }
        catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e)
        {
            fail();
        }

        return keyStore;
    }


    @After
    public void tearDown()
    {
        // after each test, close all sockets in use
        for (Node n : nodesInUse)
        {
            n.closeSocket();
        }
        nodesInUse.clear();
        this.socket.close();
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

