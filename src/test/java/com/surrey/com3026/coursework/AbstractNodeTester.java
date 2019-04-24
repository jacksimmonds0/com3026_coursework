package com.surrey.com3026.coursework;

import com.surrey.com3026.coursework.helper.GetInfoHelper;
import com.surrey.com3026.coursework.member.Member;
import com.surrey.com3026.coursework.message.Message;
import org.junit.After;
import org.junit.Before;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class to hold the common methods and fields for integration testing of nodes joining the group
 */
public abstract class AbstractNodeTester
{
    protected static final int TIMEOUT = 5000;

    private List<Node> nodesInUse;

    private DatagramSocket socket;

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

    protected Message getInfoFromNode(int port) throws IOException, JAXBException
    {
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        Member tester = new Member(10001, 8999);
        GetInfoHelper sender = new GetInfoHelper(InetAddress.getByName("127.0.0.1"), port, tester, socket);
        new Thread(sender).start();

        socket.receive(packet);
        String messageReceived = new String(packet.getData(), packet.getOffset(), packet.getLength());

        JAXBContext context = JAXBContext.newInstance(Message.class);
        Unmarshaller unmarshallerObj = context.createUnmarshaller();
        return (Message) unmarshallerObj.unmarshal(new StringReader(messageReceived));
    }


    @After
    public void tearDown()
    {
        // after each tests, close all sockets in use
        for (Node n : nodesInUse)
        {
            n.closeSocket();
        }
        nodesInUse.clear();
        this.socket.close();
    }
}
