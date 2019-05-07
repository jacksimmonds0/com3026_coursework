package com.surrey.com3026.coursework;

import com.surrey.com3026.coursework.member.Leader;
import com.surrey.com3026.coursework.member.Member;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.surrey.com3026.coursework.AbstractNodeTester.TIMEOUT;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;

public class FaultTolerance_IT extends AbstractNodeTester
{

    @Test
    public void test_new_member_becomes_leader_after_election() throws InterruptedException
    {
        Node n1 = createNode("1", "8001");
        Node n2 = createNode("2", "8002", "127.0.0.1:8001");
        Node n3 = createNode("3", "8003", "127.0.0.1:8002");
        startThreads(n1, n2, n3);

        // kill the leader node
        n1.closeSocket();
        Node n4 = createNode("4", "8004", "127.0.0.1:8003");

        // node 4 should become the new leader
        startThreads(n4);
        Thread.sleep(ELECTION_TIMEOUT);

        // node 1 has been stopped so should not be present, node 4 should become the new leader as it
        // has the highest ID and bully leader election is being used
        Leader expectedLeader = new Leader(4, 8004);

        List<Member> currentMembers = getInfoFromNode(8004).getMembers();
        Leader actualLeader = getLeaderFromList(currentMembers);

        assertNotNull(actualLeader);
        assertEquals(expectedLeader, actualLeader);
    }

    @Test
    public void test_previous_member_id_10_becomes_leader_after_election() throws InterruptedException
    {
        // create a group of members with the node IDs 1, 2 and 10 (where 10 will become the leader)
        Node n1 = createNode("1", "8001");
        Node n2 = createNode("2", "8002", "127.0.0.1:8001");
        Node n10 = createNode("10", "8010", "127.0.0.1:8002");
        startThreads(n1, n2, n10);

        // kill the leader node
        n1.closeSocket();
        Node n4 = createNode("4", "8004", "127.0.0.1:8002");

        // node 10 should become the new leader
        startThreads(n4);
        Thread.sleep(ELECTION_TIMEOUT);

        // node 1 has been stopped so should not be present, node 10 should become the new leader as it
        // has the highest ID and bully leader election is being used
        Leader expectedLeader = new Leader(10, 8010);

        List<Member> currentMembers = getInfoFromNode(8004).getMembers();
        Leader actualLeader = getLeaderFromList(currentMembers);

        assertNotNull(actualLeader);
        assertEquals(expectedLeader, actualLeader);
    }

    @Test
    public void test_all_members_have_correct_leader_after_election() throws InterruptedException
    {
        Node n1 = createNode("1", "8001");
        Node n2 = createNode("2", "8002", "127.0.0.1:8001");
        Node n10 = createNode("10", "8010", "127.0.0.1:8002");
        startThreads(n1, n2, n10);

        // kill the leader node
        n1.closeSocket();
        Node n4 = createNode("4", "8004", "127.0.0.1:8002");

        startThreads(n4);
        Thread.sleep(ELECTION_TIMEOUT);

        Leader expectedLeader = new Leader(10, 8010);
        List<Member> members = Arrays.asList(
                expectedLeader,
                new Member(2, 8002),
                new Member(4, 8004)
        );

        // ensuring all members left in the group have the same leader
        for (Member member : members)
        {
            List<Member> currentMembersGetInfo = getInfoFromNode(member.getPortNumber()).getMembers();
            Leader actualLeader = getLeaderFromList(currentMembersGetInfo);

            assertNotNull(actualLeader);
            assertEquals(expectedLeader, actualLeader);
        }
    }



    /**
     * Getting the leader from the list of members
     *
     * @param members
     *          list of members to retrieve the leader from
     * @return the Leader if one exists, null otherwise
     */
    private Leader getLeaderFromList(List<Member> members)
    {
        List<Member> leaderFromCurrentMembers = members.stream()
                .filter(member -> member instanceof Leader)
                .collect(Collectors.toList());

        if (leaderFromCurrentMembers.size() == 1)
        {
            return (Leader) leaderFromCurrentMembers.get(0);
        }
        else
        {
            return null;
        }
    }

}
