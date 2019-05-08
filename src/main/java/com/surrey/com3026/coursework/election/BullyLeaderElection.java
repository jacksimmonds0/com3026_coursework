package com.surrey.com3026.coursework.election;

import com.surrey.com3026.coursework.member.Leader;
import com.surrey.com3026.coursework.member.Member;
import com.surrey.com3026.coursework.member.Members;
import com.surrey.com3026.coursework.message.Message;
import com.surrey.com3026.coursework.message.sender.AbstractMessageSender;
import com.surrey.com3026.coursework.message.sender.UpdateMembers;
import com.surrey.com3026.coursework.security.SignatureHandler;
import org.apache.log4j.Logger;

import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

/**
 * Implementation of the bully leader election algorithm, for use in this distributed system
 */
public class BullyLeaderElection implements LeaderElection
{
    private static final Logger LOG = Logger.getLogger(BullyElectionSender.class);

    private static final int TIMEOUT = 5 * 1000;

    private static final String ELECTION_INITIATE = "election_initiate";

    private static final String ELECTION_ALIVE = "election_alive";

    private static final String ELECTION_VICTORY = "election_victory";

    private boolean waitingForResponses;

    private List<Message> receivedElectionMessages;

    private Members members;

    private Member thisNode;

    private DatagramSocket socket;

    private SignatureHandler signatureHandler;

    /**
     * Create a bully leader election object
     *
     * @param members
     *          the current list of {@link Member}s in the group system
     * @param thisNode
     *          the {@link Member} representing this node
     * @param socket
     *          the UDP {@link DatagramSocket} to send election messages across
     * @param signatureHandler
     *          the handler for signatures, to create a digital signature on messages sent
     */
    public BullyLeaderElection(Members members, Member thisNode, DatagramSocket socket,
                               SignatureHandler signatureHandler)
    {
        this.waitingForResponses = false;
        this.receivedElectionMessages = new ArrayList<>();
        this.members = members;
        this.thisNode = thisNode;
        this.socket = socket;
        this.signatureHandler = signatureHandler;
    }

    @Override
    public void initiate()
    {
        LOG.debug("Leader election initiated");
        List<Member> higherIdMembers = getMembersWithHigherId(thisNode);

        // check if thisNode has the highest process ID in the list
        if (higherIdMembers.isEmpty())
        {
            // update this node to coordinator in this nodes members list
            // send victory message to all
            LOG.debug("No higher ID members found in current members list");
            sendVictory();
        }
        else
        {
            waitingForResponses = true;

            // send an election initiate message to higher ID members
            BullyElectionSender sender = new BullyElectionSender(members, thisNode, socket, signatureHandler,
                    ELECTION_INITIATE, higherIdMembers);
            new Thread(sender).start();

            waitForResponses(higherIdMembers);
        }
    }

    @Override
    public void handleElectionMessage(Message message)
    {
        if (waitingForResponses)
        {
            this.receivedElectionMessages.add(message);
        }
        else
        {
            if (message.getType().equals(ELECTION_VICTORY))
            {
                complete((Leader) message.getResponder());
            }
            else if (message.getType().equals(ELECTION_INITIATE))
            {
                Member electionInitiator = message.getResponder();

                // return election_alive response
                BullyElectionSender sender = new BullyElectionSender(members, thisNode, socket, signatureHandler,
                        ELECTION_ALIVE, Collections.singletonList(electionInitiator));
                new Thread(sender).start();

                if (electionInitiator.getId() < thisNode.getId())
                {
                    LOG.debug("Election initiator has lower ID, so re-initiating election process.");

                    // lower ID = restart process
                    this.initiate();
                }
            }
        }
    }

    @Override
    public boolean isElectionMessage(String messageType)
    {
        return messageType.equals(ELECTION_ALIVE) || messageType.equals(ELECTION_INITIATE)
                || messageType.equals(ELECTION_VICTORY);
    }

    /**
     * Get members in the current members list with a higher ID than the member to check
     *
     * @param memberToCheck
     *          the member to check if there are any other members with higher IDs
     * @return the {@link List} of {@link Member} objects with higher process IDs
     */
    private List<Member> getMembersWithHigherId(Member memberToCheck)
    {
        List<Member> higherIds = new ArrayList<>(members.getMembers());

        return higherIds.stream()
                .filter(member -> member.getId() > memberToCheck.getId())
                .collect(Collectors.toList());
    }


    /**
     * Wait for election responses until the TIMEOUT
     *
     * @param membersMessaged
     *          the list of {@link Member}s that have been messaged with election_initiate
     */
    private void waitForResponses(List<Member> membersMessaged)
    {
        new Timer().schedule(
                new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        List<Message> victoriesReceived = receivedElectionMessages.stream()
                                .filter(message -> message.getType().equals(ELECTION_VICTORY))
                                .collect(Collectors.toList());

                        if (!victoriesReceived.isEmpty())
                        {
                            complete((Leader) victoriesReceived.get(0).getResponder());
                        }
                        else
                        {
                            List<Member> membersResponded = receivedElectionMessages.stream()
                                    .filter(message -> message.getType().equals(ELECTION_ALIVE))
                                    .map(Message::getResponder)
                                    .collect(Collectors.toList());

                            // if no higher ID processes respond, then we can just make this node the leader
                            // to complete the algorithm
                            if (membersResponded.isEmpty())
                            {
                                LOG.debug("Waited for responses and no member has");
                                sendVictory();
                            }

                            // update all others lists to remove those who are not responding
                            List<Member> membersNotResponding = membersMessaged.stream()
                                    .filter(membersResponded::contains)
                                    .collect(Collectors.toList());

                            if (!membersNotResponding.isEmpty())
                            {
                                members.removeMembers(membersNotResponding);
                                UpdateMembers sender = new UpdateMembers(members, thisNode, socket, signatureHandler);
                                new Thread(sender).start();
                            }
                        }

                        // reset after timeout for next potential leader election
                        waitingForResponses = false;
                    }
                }, TIMEOUT
        );
    }

    /**
     * Sending a victory message to all other nodes, to update their list of members for this node
     * as the new leader, completing the algorithm
     *
     */
    private void sendVictory()
    {
        Leader newLeader = new Leader(thisNode.getId(), thisNode.getPortNumber());

        BullyElectionSender sender = new BullyElectionSender(members, newLeader, socket, signatureHandler, ELECTION_VICTORY);
        new Thread(sender).start();

        // complete the algorithm as this node is now the leader
        complete(newLeader);
    }

    @Override
    public void complete(Leader leader)
    {
        LOG.debug("Leader election complete");
        LOG.debug("New leader has been elected: " + leader.toString());

        // clear the list of received messages to allow for the next potential election in the future
        receivedElectionMessages.clear();

        // change members list and thisNode to a leader object (if appropriate)
        if (thisNode.equals(leader))
        {
            thisNode = leader;
        }

        for (int i = 0; i < members.getMembers().size(); i++)
        {
            Member member = members.getMembers().get(i);

            if (leader.equals(member))
            {
                members.replaceMember(i, leader);
            }
        }

        LOG.debug("Current list of members is now: " + members);
    }


    /**
     * Message sender specifically for the bully leader election algorithm
     */
    private class BullyElectionSender extends AbstractMessageSender implements Runnable
    {
        private String messageType;

        private List<Member> membersToMessage;

        /**
         * Creating a message sender thread to allow for election messages to be sent on the UDP socket
         *
         * @param members
         *          the list of current members
         * @param thisNode
         *          the {@link Member} representing this node
         * @param socket
         *          the {@link DatagramSocket} to sent messages on
         * @param signatureHandler
         *          the handler for signatures, to create a digital signature on messages sent
         * @param messageType
         *          the type of election message being sent
         */
        BullyElectionSender(Members members, Member thisNode, DatagramSocket socket, SignatureHandler signatureHandler,
                            String messageType)
        {
            super(members, thisNode, socket, signatureHandler);
            this.messageType = messageType;
        }

        /**
         * Creating a message sender thread to allow for election messages to be sent on the UDP socket
         *
         * @param members
         *          the list of current members
         * @param thisNode
         *          the {@link Member} representing this node
         * @param socket
         *          the {@link DatagramSocket} to sent messages on
         * @param signatureHandler
         *          the handler for signatures, to create a digital signature on messages sent
         * @param messageType
         *          the type of election message being sent
         * @param memberToMessage
         *          the list of members to send the message to
         */
        BullyElectionSender(Members members, Member thisNode, DatagramSocket socket, SignatureHandler signatureHandler,
                            String messageType, List<Member> memberToMessage)
        {
            super(members, thisNode, socket, signatureHandler);
            this.messageType = messageType;
            this.membersToMessage = memberToMessage;
        }

        @Override
        public void run()
        {
            Message message = new Message(messageType, thisNode);

            if (messageType.equals(ELECTION_VICTORY))
            {
                broadcastMessage(message);
            }
            else
            {
                sendMessageToMultipleMembers(message, membersToMessage);
            }
        }
    }

}
