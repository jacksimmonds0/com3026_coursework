package com.surrey.com3026.coursework;

import com.surrey.com3026.coursework.helper.GetInfoHelper;
import com.surrey.com3026.coursework.member.Member;
import com.surrey.com3026.coursework.member.Leader;
import com.surrey.com3026.coursework.message.Message;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class JoiningGroup_IT
{
    private List<Node> nodesInUse;

    private DatagramSocket socket;

    @Before
    public void setUp() throws Exception
    {
        this.nodesInUse = new ArrayList<>();
        this.socket = new DatagramSocket(8999);
    }

    @Test
    public void test_3_nodes_join_with_1_leader() throws Exception
    {
        Node n1 = createNode("1", "8001");
        Node n2 = createNode("2", "8002", "127.0.0.1:8001");
        Node n3 = createNode("3", "8003", "127.0.0.1:8002");
        startThreads(n1, n2, n3);

        Thread.sleep(5000);

        Member expectedLeader = new Leader(1, 8001);
        List<Member> expected = Arrays.asList(
                expectedLeader,
                new Member(2, 8002),
                new Member(3, 8003)
        );
        List<Member> currentMembers = getInfoFromNode(8003).getMembers();

        // check all expected members are part of the current members list
        assertEquals(expected.size(), currentMembers.size());

        Member actualLeader = null;
        for (Member m : currentMembers)
        {
            assertTrue(expected.contains(m));

            if (m instanceof Leader)
            {
                actualLeader = m;
            }
        }

        // ensure the leader from the current members is correct
        if (actualLeader != null)
        {
            assertEquals(expectedLeader, actualLeader);
        }
        else
        {
            fail();
        }
    }


    @Test
    public void test_node_2_shuts_down_node_4_joins() throws Exception
    {
        Node n1 = createNode("1", "8001");
        Node n2 = createNode("2", "8002", "127.0.0.1:8001");
        Node n3 = createNode("3", "8003", "127.0.0.1:8002");
        startThreads(n1, n2, n3);

        n2.closeSocket();
        Node n4 = createNode("4", "8004", "127.0.0.1:8003");

        startThreads(n4);
        Thread.sleep(5000);

        // node 2 has been stopped so should not be present
        List<Member> expected = Arrays.asList(
                new Member(1, 8001),
                new Member(3, 8003),
                new Member(4, 8004)
        );
        List<Member> currentMembers = getInfoFromNode(8003).getMembers();

        // check all expected members are part of the current members list
        assertEquals(expected.size(), currentMembers.size());
        for (Member m : expected)
        {
            assertTrue(currentMembers.contains(m));
        }
    }


    private Node createNode(String... args)
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

    private void startThreads(Node... nodes) throws InterruptedException
    {
        for(Node n : nodes)
        {
            this.nodesInUse.add(n);
            Thread t = createJoinGroupThread(n);

            t.start();
            Thread.sleep(1000);
        }
    }

    private Thread createJoinGroupThread(Node node)
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

    private Message getInfoFromNode(int port) throws IOException, JAXBException
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
