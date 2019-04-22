package com.surrey.com3026.coursework.message.checker;

import com.surrey.com3026.coursework.election.LeaderElection;
import com.surrey.com3026.coursework.member.Leader;
import com.surrey.com3026.coursework.member.Member;
import com.surrey.com3026.coursework.member.Members;
import com.surrey.com3026.coursework.message.MessageReceiver;
import com.surrey.com3026.coursework.message.sender.UpdateMembers;

import java.net.DatagramSocket;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

/**
 * Class to check that all expected members have responded, after new joiner message sent
 * to all members (other than the initial responder)
 *
 */
public class MembersResponseChecker implements Runnable
{
    private static final int TIMEOUT = 5 * 1000;

    private MessageReceiver receiver;

    private Members members;

    private Member thisNode;

    private DatagramSocket socket;

    private LeaderElection election;

    public MembersResponseChecker(MessageReceiver receiver, Members members, Member thisNode,
                                  DatagramSocket socket, LeaderElection election)
    {
        this.receiver = receiver;
        this.members = members;
        this.thisNode = thisNode;
        this.socket = socket;
        this.election = election;
    }

    @Override
    public void run()
    {
        // check for all the members who have not responded after a defined delay timeout
        new Timer().schedule(
                new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        List<Member> remaining = receiver.getMembersToCheckAccepted();
                        if (!remaining.isEmpty())
                        {
                            System.out.println("MEMBERS NOT RESPONDING");
                            System.out.println("-------------------------------");
                            remaining.forEach(System.out::println);
                            System.out.println("-------------------------------");

                            // send updated list to all other members
                            members.removeMembers(remaining);
                            UpdateMembers sender = new UpdateMembers(members, thisNode, socket);
                            new Thread(sender).start();

                            // see if any of the non-responsive previous members was the leader/coordinator
                            List<Member> anyLeader = remaining.stream()
                                    .filter(member -> member instanceof Leader)
                                    .collect(Collectors.toList());

                            if (!anyLeader.isEmpty())
                            {
                                // trigger leader election here
                                election.initiate();
                            }

                        }
                    }
                }, TIMEOUT
        );

    }
}
