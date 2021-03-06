package com.surrey.com3026.coursework.message.receiver;

import com.surrey.com3026.coursework.election.LeaderElection;
import com.surrey.com3026.coursework.member.Member;
import com.surrey.com3026.coursework.member.Members;
import com.surrey.com3026.coursework.message.Message;
import com.surrey.com3026.coursework.message.MessageTypes;
import com.surrey.com3026.coursework.message.sender.AcceptJoiner;
import com.surrey.com3026.coursework.message.sender.NewJoiner;
import com.surrey.com3026.coursework.message.sender.SendAllCurrentMembers;
import com.surrey.com3026.coursework.security.SignatureHandler;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.StringWriter;
import java.net.DatagramSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * Class that runs on a thread to consume messages placed on the blocking message queue by the {@link MessageReceiver}
 */
public class MessageConsumer implements Runnable
{
    private static final Logger LOG = Logger.getLogger(MessageConsumer.class);

    private BlockingQueue messageQueue;

    private Members members;

    private DatagramSocket socket;

    private LeaderElection election;

    private Member thisNode;

    private SignatureHandler signatureHandler;

    private List<Member> membersToCheckAccepted = Collections.synchronizedList(new ArrayList<>());

    /**
     * Create a message consumer to take messages from the blocking queue of {@link Message} objects
     *
     * @param messageQueue
     *          the blocking queue of messages to take from
     * @param members
     *          the current members known within the joined group
     * @param socket
     *          the socket to send messages across on
     * @param election
     *          the implementation of {@link LeaderElection} to perform a leader election where necessary
     * @param thisNode
     *          the {@link Member} object representing this node that has been executed
     * @param signatureHandler
     *          the class to verify the sign of the received message and sign messages to send
     */
    public MessageConsumer(BlockingQueue messageQueue, Members members, DatagramSocket socket,
                           LeaderElection election, Member thisNode, SignatureHandler signatureHandler)
    {
        this.messageQueue = messageQueue;
        this.members = members;
        this.socket = socket;
        this.election = election;
        this.thisNode = thisNode;
        this.signatureHandler = signatureHandler;
    }

    @Override
    public void run()
    {
        try
        {
            // runs in parallel with MessageReceiver to take from the queue when possible
            while (true)
            {
                // take the message from the queue and then apply logic based on the type of message received
                Message message = (Message) messageQueue.take();

                if (message.getTimestamps() != null)
                {
                    // synchronise the timestamps from the message based on the max value
                    this.synchroniseTimestamps(message.getTimestamps());
                }

                // increment the Lamport timestamp once a message has been received
                thisNode.incrementTimestamp();
                members.updateMemberTimestamp(thisNode);

                // used to check digital signature is correct
                int responderId = message.getResponder().getId();
                byte[] signature = message.getSignature();

                if (signatureHandler.verify(responderId, signature, message.toString().getBytes()))
                {
                    LOG.debug("Signature verified from node " + responderId);
                    handleMessageType(message);

                    // after every message received log the current members list
                    logCurrentMembers();
                }
                else
                {
                    // Ignore the message since it could not be verified
                    // Log this fact to the console to show why the message was ignored
                    LOG.error("Unable to verify signature from node " + responderId + ", message ignored");
                }
            }
        }
        catch (InterruptedException | IOException e)
        {
            LOG.error("Exception thrown when consuming messages: ", e);
        }
    }

    /**
     * Taking the message and applying the logic to handle it appropriately based on the type
     * e.g. receiving a join request means update members with the joiner, and send back a list of all current members
     *
     * @param message
     *          the message to be handled
     * @throws UnknownHostException
     *          if the IP from a responder is unknown
     */
    private void handleMessageType(Message message) throws UnknownHostException
    {
        LOG.debug(message.getType() + " message received from: " + message.getResponder());

        // send a message to the joiner with info on all current members
        if (message.getType().equals(MessageTypes.JOIN_REQUEST))
        {

            Member newJoiner = message.getResponder();
            // update this nodes list of members with the new joiner
            members.addMember(newJoiner);

            LOG.debug("The current members are now: " + members);

            SendAllCurrentMembers sender = new SendAllCurrentMembers(members, newJoiner.getIp(),
                    newJoiner.getPortNumber(), thisNode, socket, signatureHandler);
            new Thread(sender).start();
        }
        else if (message.getType().equals(MessageTypes.NEW_JOINER))
        {
            Member newJoiner = message.getResponder();
            // update this nodes list of members with the new joiner
            members.addMember(newJoiner);

            LOG.debug("The current members are now: " + members);

            // send accept_joiner
            // need to check that all these accepted messages are received
            AcceptJoiner sender = new AcceptJoiner(members, newJoiner.getIp(),
                    newJoiner.getPortNumber(), thisNode, socket, signatureHandler);
            new Thread(sender).start();
        }
        else if (message.getType().equals(MessageTypes.ALL_CURRENT_MEMBERS))
        {
            List<Member> currentMembers = message.getMembers();
            members.setMembers(currentMembers);

            LOG.debug("The current members received are: " + members);

            // send update members message to everyone
            // except this node and the node we just got the all_members message from
            if (message.getResponder() != null)
            {
                NewJoiner sender = new NewJoiner(members, thisNode, socket, message.getResponder(),
                        this, election, signatureHandler);
                new Thread(sender).start();
            }
        }
        else if (message.getType().equals(MessageTypes.ACCEPT_JOINER))
        {
            LOG.debug("Member has accepted me in joining the group: " + message.getResponder());

            // remove from the list for checking all members are alive
            membersToCheckAccepted.remove(message.getResponder());
        }
        else if (message.getType().equals(MessageTypes.UPDATE_MEMBERS))
        {
            LOG.debug("Updating the current members list to: " + message.getMembers());

            // updating the members list to the current members
            // due to some not responding
            members.setMembers(message.getMembers());
        }
        else if (message.getType().equals(MessageTypes.GET_INFO))
        {
            // helper method to get info (for testing purposes)
            // just sends this current list of members back
            Member responder = message.getResponder();

            LOG.debug("Sending current members list to: " + responder);

            SendAllCurrentMembers sender = new SendAllCurrentMembers(members, responder.getIp(),
                    responder.getPortNumber(), thisNode, socket, signatureHandler);
            new Thread(sender).start();
        }
        else if (election.isElectionMessage(message.getType()))
        {
            election.handleElectionMessage(message);
        }
    }

    /**
     * @return the {@link List} of {@link Member}s who this node needs to check accepted it as a joiner
     */
    public List<Member> getMembersToCheckAccepted()
    {
        return membersToCheckAccepted;
    }

    /**
     * Set the members to check accepted the new joiner request
     *
     * @param membersToCheckAccepted
     *          the {@link List} of {@link Member}s to check
     */
    public void setMembersToCheckAccepted(List<Member> membersToCheckAccepted)
    {
        this.membersToCheckAccepted = membersToCheckAccepted;
    }

    /**
     * Log the JAXB members object to the node log, acting as a checkpoint for recovery
     */
    private void logCurrentMembers()
    {
        try
        {
            JAXBContext context = JAXBContext.newInstance(Members.class);

            Marshaller marshallObj = context.createMarshaller();
            StringWriter dataWriter = new StringWriter();
            marshallObj.marshal(members, dataWriter);

            LOG.debug(dataWriter.toString());
        }
        catch (JAXBException e)
        {
            LOG.error("Error while marshalling the current members to be log", e);
        }
    }

    /**
     * After receiving a message, need to synchronise the timestamps for all members
     *
     * @param timestamps
     *          the map of timestamps received
     */
    private void synchroniseTimestamps(Map<Integer, Integer> timestamps)
    {
        for (Integer memberId : timestamps.keySet())
        {
            // find the matching member in the current members list from the message list from the ID
            Member currentMember = members.getMemberFromId(memberId);

            if (currentMember != null)
            {
                // set the timestamp to the highest timestamp between the current stored and the message
                // TS[k] = max(TS[k], MTS[k]) for k = 1 to N
                int timestamp = Math.max(currentMember.getLamportTimestamp(), timestamps.get(memberId));
                currentMember.setLamportTimestamp(timestamp);
            }
        }
    }
}
