package com.surrey.com3026.coursework.election;

import com.surrey.com3026.coursework.member.Leader;
import com.surrey.com3026.coursework.message.Message;

/**
 * Interface to allow for different implementations of leader election algorithms (bully or ring)
 */
public interface LeaderElection
{
    /**
     * Method to initiate a leader election, called when the current leader is unresponsive
     */
    public void initiate();

    /**
     * Handle any type of election message, to send responses where necessary, eng the algorithm
     * if the leader is selected etc.
     *
     * @param message
     *          the {@link Message} to handle
     */
    public void handleElectionMessage(Message message);

    /**
     * Determine if a message is an election message based on the type
     *
     * @param messageType
     *          the type received from the message
     * @return true if the message is an election message, false otherwise
     */
    public boolean isElectionMessage(String messageType);

    /**
     * Completing the algorithm, by setting the input node as the new leader
     *
     * @param leader
     *          the new leader of the group
     */
    public void complete(Leader leader);

}
