package com.surrey.com3026.coursework;

import com.surrey.com3026.coursework.member.Leader;
import com.surrey.com3026.coursework.member.Member;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Test cases ensuring all member nodes are synchronised with the same list of members for the group
 * under a few different circumstances
 */
public class Synchronisation_IT extends AbstractNodeTester
{
    @Test
    public void test_all_nodes_have_same_members_list_after_new_joiner() throws InterruptedException
    {
        // establish initial group
        Node n1 = createNode("1", "8001");
        Node n2 = createNode("2", "8002", "127.0.0.1:8001");
        Node n3 = createNode("3", "8003", "127.0.0.1:8002");
        startThreads(n1, n2, n3);

        // then add the new joiner
        Thread.sleep(TIMEOUT);
        Node n4 = createNode("4", "8004", "127.0.0.1:8003");
        startThreads(n4);

        Thread.sleep(TIMEOUT);

        List<Member> expectedMembers = Arrays.asList(
                new Leader(1, 8001),
                new Member(2, 8002),
                new Member(3, 8003),
                new Member(4, 8004)
        );

        assertAllMembersListCorrect(expectedMembers);
    }

    @Test
    public void test_same_members_list_after_node_fails_and_new_joiner() throws InterruptedException
    {
        // establish initial group
        Node n1 = createNode("1", "8001");
        Node n2 = createNode("2", "8002", "127.0.0.1:8001");
        Node n3 = createNode("3", "8003", "127.0.0.1:8002");
        startThreads(n1, n2, n3);

        // then fail n3 and add the new joiner
        Thread.sleep(TIMEOUT);
        n3.closeSocket();
        Node n4 = createNode("4", "8004", "127.0.0.1:8002");
        startThreads(n4);

        Thread.sleep(TIMEOUT);

        List<Member> expectedMembers = Arrays.asList(
                new Leader(1, 8001),
                new Member(2, 8002),
                new Member(4, 8004)
        );

        assertAllMembersListCorrect(expectedMembers);
    }


    @Test
    public void test_nodes_synchronised_after_multiple_members_failed() throws InterruptedException
    {
        // establish initial group
        Node n1 = createNode("1", "8001");
        Node n2 = createNode("2", "8002", "127.0.0.1:8001");
        Node n3 = createNode("3", "8003", "127.0.0.1:8002");
        Node n4 = createNode("4", "8004", "127.0.0.1:8003");
        Node n5 = createNode("5", "8005", "127.0.0.1:8004");
        startThreads(n1, n2, n3, n4, n5);

        // then fail multiple noted
        Thread.sleep(TIMEOUT);
        n2.closeSocket();
        n3.closeSocket();
        n4.closeSocket();

        Node n6 = createNode("6", "8006", "127.0.0.1:8001");
        Node n7 = createNode("7", "8007", "127.0.0.1:8001");
        startThreads(n6, n7);
        Thread.sleep(TIMEOUT);

        List<Member> expectedMembers = Arrays.asList(
                new Leader(1, 8001),
                new Member(5, 8005),
                new Member(6, 8006),
                new Member(7, 8007)
        );

        assertAllMembersListCorrect(expectedMembers);
    }

    /**
     * Assert that all expected members have the exact same list
     */
    private void assertAllMembersListCorrect(List<Member> expectedMembers)
    {
        for (Member member : expectedMembers)
        {
            List<Member> actualMembers = getInfoFromNode(member.getPortNumber()).getMembers();

            if (!(expectedMembers.size() == actualMembers.size() &&
                    expectedMembers.containsAll(actualMembers) && actualMembers.containsAll(expectedMembers)))
            {
                System.out.println(expectedMembers);
                System.out.println(actualMembers);

            }
            assertMembersListSame(expectedMembers, actualMembers);

        }
    }

}
