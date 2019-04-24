package com.surrey.com3026.coursework;

import com.surrey.com3026.coursework.election.BullyLeaderElection;
import com.surrey.com3026.coursework.election.LeaderElection;
import com.surrey.com3026.coursework.member.Leader;
import com.surrey.com3026.coursework.member.Member;
import com.surrey.com3026.coursework.member.Members;
import com.surrey.com3026.coursework.message.receiver.MessageConsumer;
import com.surrey.com3026.coursework.message.receiver.MessageReceiver;
import com.surrey.com3026.coursework.message.sender.JoinRequest;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Class to instantiate all objects to run on this node, after taking initial command line parameters
 */
public class Node
{
    private static final int QUEUE_CAPACITY = 20;

    private Member thisNode;

    private String prevMember;

    private DatagramSocket socket;

    /**
     * Constructor for creating this node as a {@link Leader}
     *
     * @param id
     *          the id for this node
     * @param port
     *          the port for this node
     */
    public Node(int id, int port)
    {
        this.thisNode = new Leader(id, port);
    }

    /**
     * Constructor for creating this node as a {@link Member}
     *
     * @param id
     *          the id for this node
     * @param port
     *          the port for this node
     * @param prevMember
     *          the IP and port for a previous member, split by a :
     */
    public Node(int id, int port, String prevMember)
    {
        this.thisNode = new Member(id, port);
        this.prevMember = prevMember;
    }

    /**
     * Method to start the node, creating threads needed and attempt to join based on the command line params
     */
    public void startNode()
    {
        Members members = new Members();
        members.addMember(thisNode);

        try
        {
            this.initialiseThreads(members);

            if (prevMember != null)
            {
                // use previous member to retrieve all current members list to update
                String[] prevMemberSplit = prevMember.split(":");
                InetAddress prevAddress = InetAddress.getByName(prevMemberSplit[0]);
                int prevPort = Integer.parseInt(prevMemberSplit[1]);

                JoinRequest sender = new JoinRequest(members, prevAddress, prevPort, thisNode, socket);
                new Thread(sender).start();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Initialising the threads to run on the node
     *
     * @param members
     *          the list of current members to pass to threads where needed
     * @throws SocketException
     *          if there is an issue opening the socket on the port specified by the command line parameters
     */
    private void initialiseThreads(Members members) throws SocketException
    {

        // create the message queue to for the message receiver and consumer to handle
        BlockingQueue messageQueue = new LinkedBlockingQueue(QUEUE_CAPACITY);

        // create the message sender to send messages (e.g. a new joiner request)
        socket = new DatagramSocket(thisNode.getPortNumber());

        // decide on leader election implementation
        LeaderElection election = new BullyLeaderElection(members, thisNode, socket);

        // initialise to receive UDP messages on this node and add them to the message queue
        MessageReceiver receiver = new MessageReceiver(messageQueue, socket);
        new Thread(receiver).start();

        // initialise to consume messages from the message queue
        MessageConsumer consumer = new MessageConsumer(messageQueue, members, socket, election, thisNode);
        new Thread(consumer).start();
    }

    /**
     * Method for testing purposes, to close the socket when closing the thread down to simulate the node going down
     */
    void closeSocket()
    {
        socket.close();
    }

}
