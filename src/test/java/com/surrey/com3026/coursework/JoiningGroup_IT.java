package com.surrey.com3026.coursework;

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


/**
 * Testing plan
 *
 * Synchronisation_IT   - when a member joins, all members have the correct list get info from all nodes(?)
 * Replication_IT       -
 * FaultTolerance_IT    - testing leader election, create group, kill leader, join node, get info and assert new leader correct
 * Recovery_IT          - create group, kill node, spin same node back up, can get same members list correctly
 * Security_IT          - don't sign the message properly / change the message between signing and sending
 */
public class JoiningGroup_IT extends AbstractNodeTester
{

    @Test
    public void test_3_nodes_join_with_1_leader() throws Exception
    {
        Node n1 = createNode("1", "8001");
        Node n2 = createNode("2", "8002", "127.0.0.1:8001");
        Node n3 = createNode("3", "8003", "127.0.0.1:8002");
        startThreads(n1, n2, n3);

        Thread.sleep(TIMEOUT);

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
    public void test_3_join_node_2_shuts_down_node_4_joins() throws Exception
    {
        Node n1 = createNode("1", "8001");
        Node n2 = createNode("2", "8002", "127.0.0.1:8001");
        Node n3 = createNode("3", "8003", "127.0.0.1:8002");
        startThreads(n1, n2, n3);

        n2.closeSocket();
        Node n4 = createNode("4", "8004", "127.0.0.1:8003");

        startThreads(n4);
        Thread.sleep(TIMEOUT);

        // node 2 has been stopped so should not be present
        List<Member> expected = Arrays.asList(
                new Leader(1, 8001),
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


}
