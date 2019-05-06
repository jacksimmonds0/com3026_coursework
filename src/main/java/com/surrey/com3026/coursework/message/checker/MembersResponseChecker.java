package com.surrey.com3026.coursework.message.checker;

import com.surrey.com3026.coursework.election.LeaderElection;
import com.surrey.com3026.coursework.member.Leader;
import com.surrey.com3026.coursework.member.Member;
import com.surrey.com3026.coursework.member.Members;
import com.surrey.com3026.coursework.message.receiver.MessageConsumer;
import com.surrey.com3026.coursework.message.sender.UpdateMembers;
import com.surrey.com3026.coursework.security.SignatureHandler;
import org.apache.log4j.Logger;

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
    private static final Logger LOG = Logger.getLogger(MembersResponseChecker.class);

    private static final int TIMEOUT = 5 * 1000;

    private MessageConsumer consumer;

    private Members members;

    private Member thisNode;

    private DatagramSocket socket;

    private LeaderElection election;

    private SignatureHandler signatureHandler;

    /**
     * Initialise a checker to ensure all current memebrs respond, if not update everyone and trigger a leader election
     * (if the leader is not responding)
     *
     * @param consumer
     *          the {@link MessageConsumer} to retrieve the members to check accepted
     * @param members
     *          the current list of members
     * @param thisNode
     *          the information for this node being run
     * @param socket
     *          the socket to send messages across on
     * @param election
     *          the implementation of {@link LeaderElection} to initiate elections when the leader is un-responsive
     * @param signatureHandler
     *          the handler for ensuring digital signatures are verified
     */
    public MembersResponseChecker(MessageConsumer consumer, Members members, Member thisNode, DatagramSocket socket,
                                  LeaderElection election, SignatureHandler signatureHandler)
    {
        this.consumer = consumer;
        this.members = members;
        this.thisNode = thisNode;
        this.socket = socket;
        this.election = election;
        this.signatureHandler = signatureHandler;
    }

    @Override
    public void run()
    {
        LOG.debug("Waiting for responses from other members ensuring they are all alive.");

        // check for all the members who have not responded after a defined delay timeout
        new Timer().schedule(
                new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        List<Member> remaining = consumer.getMembersToCheckAccepted();
                        if (!remaining.isEmpty())
                        {
                            LOG.debug("Previous members are not responding: " + remaining);

                            // send updated list to all other members
                            members.removeMembers(remaining);
                            UpdateMembers sender = new UpdateMembers(members, thisNode, socket, signatureHandler);
                            new Thread(sender).start();

                            // see if any of the non-responsive previous members was the leader/coordinator
                            List<Member> anyLeader = remaining.stream()
                                    .filter(member -> member instanceof Leader)
                                    .collect(Collectors.toList());

                            if (!anyLeader.isEmpty())
                            {
                                LOG.debug("Leader not responding: " + anyLeader);

                                // trigger leader election here
                                election.initiate();
                            }

                        }
                    }
                }, TIMEOUT
        );

    }
}
