package com.surrey.com3026.coursework;

import com.surrey.com3026.coursework.message.Message;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

/**
 * Test cases to ensure logical vector clocks are being synchronised across the distributed group nodes
 */
public class Synchronization_IT extends AbstractNodeTester
{

    @Test
    public void test_group_of_2_vector_clock_correct_for_node_1() throws InterruptedException
    {
        Node n1 = createNode("1", "8001");
        Node n2 = createNode("2", "8002", "127.0.0.1:8001");
        startThreads(n1, n2);

        Thread.sleep(TIMEOUT);

        // from node 1's POV:
        // node 1 will be 4 due to receiving/sending from node 2 and then the same from the test node (20)
        // node 2 will be 1 as it has only sent to node 1 (then no further messages after node 1 -> node 2)
        Map<Integer, Integer> actualTimestamps = getInfoFromNode(8001).getTimestamps();
        Map<Integer, Integer> expectedTimestamps = new HashMap<Integer, Integer> ()
        {
            {
                put(1, 4);
                put(2, 1);
            }
        };

        assertNotNull(actualTimestamps);
        for (Integer id : expectedTimestamps.keySet())
        {
            assertEquals(expectedTimestamps.get(id), actualTimestamps.get(id));
        }
    }

    @Test
    public void test_group_of_3_vector_clock_correct_for_node_3() throws InterruptedException
    {
        Node n1 = createNode("1", "8001");
        Node n2 = createNode("2", "8002", "127.0.0.1:8001");
        startThreads(n1, n2);

        Thread.sleep(TIMEOUT);
        Node n3 = createNode("3", "8003", "127.0.0.1:8002");
        startThreads(n3);
        Thread.sleep(TIMEOUT);

        // testing logical clock from node 3's POV
        Map<Integer, Integer> actualTimestamps = getInfoFromNode(8003).getTimestamps();
        Map<Integer, Integer> expectedTimestamps = new HashMap<Integer, Integer> ()
        {
            {
                put(1, 4);
                put(2, 4);
                put(3, 6);
            }
        };

        assertNotNull(actualTimestamps);
        for (Integer id : expectedTimestamps.keySet())
        {
            assertEquals(expectedTimestamps.get(id), actualTimestamps.get(id));
        }
    }

    @Test
    public void test_group_of_4_synchronised_on_other_nodes() throws InterruptedException
    {
        Node n1 = createNode("1", "8001");
        Node n2 = createNode("2", "8002", "127.0.0.1:8001");
        Node n3 = createNode("3", "8003", "127.0.0.1:8002");
        startThreads(n1, n2, n3);

        Thread.sleep(TIMEOUT);
        Node n4 = createNode("4", "8004", "127.0.0.1:8003");
        startThreads(n4);

        // ensuring all 4 members have the same timestamp value for 3 (except 3 itself which will have +2 due to get_info)
        for (Integer port : Arrays.asList(8001, 8002, 8003, 8004))
        {
            Map<Integer, Integer> actualTimestamps = getInfoFromNode(port).getTimestamps();

            int expectedTimestamp = 6;
            if (port == 8003)
            {
                // adding +2 to the timestamp expected value due to the get_info message received/sent response on node 3
                expectedTimestamp += 2;
            }

            assertNotNull(actualTimestamps);
            assertEquals(expectedTimestamp, (int) actualTimestamps.get(3));
        }
    }
}
