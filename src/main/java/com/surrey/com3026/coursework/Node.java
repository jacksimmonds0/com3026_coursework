package com.surrey.com3026.coursework;

import com.surrey.com3026.coursework.election.BullyLeaderElection;
import com.surrey.com3026.coursework.election.LeaderElection;
import com.surrey.com3026.coursework.member.Leader;
import com.surrey.com3026.coursework.member.Member;
import com.surrey.com3026.coursework.member.Members;
import com.surrey.com3026.coursework.message.receiver.MessageConsumer;
import com.surrey.com3026.coursework.message.receiver.MessageReceiver;
import com.surrey.com3026.coursework.message.sender.JoinRequest;
import com.surrey.com3026.coursework.message.sender.NewJoiner;
import com.surrey.com3026.coursework.message.sender.UpdateMembers;
import com.surrey.com3026.coursework.recovery.LogCheckpoint;
import com.surrey.com3026.coursework.security.SecurityConstants;
import com.surrey.com3026.coursework.security.SignatureHandler;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Class to instantiate all objects to run on this node, after taking initial command line parameters
 */
public class Node
{
    private static final Logger LOG = Logger.getLogger(Node.class);

    private static final int QUEUE_CAPACITY = 20;

    private Member thisNode;

    private String prevMember;

    private DatagramSocket socket;

    private MessageConsumer consumer;

    private LeaderElection election;

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

        LOG.debug("Node " + thisNode.getId() + " has been started");

        try
        {
            KeyStore keyStore = getThisNodesKeyStore();
            SignatureHandler signatureHandler = new SignatureHandler(keyStore);
            this.initialiseThreads(members, signatureHandler);

            // attempt to retrieve from the checkpoint within the log file
            LogCheckpoint checkpoint = new LogCheckpoint(members);
            boolean checkpointFound = checkpoint.getLastMembersFromLogFile();

            if (checkpointFound && !members.getMembers().isEmpty())
            {
                for (Member member : members.getMembers())
                {
                    // send to other member in the file, if none
                    if (!member.equals(thisNode))
                    {
                        // send join request to other node recovered from members list
                        // to receive the current members list
                        JoinRequest sender = new JoinRequest(members, member.getIp(), member.getPortNumber(), thisNode,
                                socket, signatureHandler);
                        new Thread(sender).start();

                        // only return if message sent, otherwise need to perform normal join request
                        return;
                    }

                }
            }

            if (prevMember != null)
            {
                // use previous member to retrieve all current members list to update
                String[] prevMemberSplit = prevMember.split(":");
                InetAddress prevAddress = InetAddress.getByName(prevMemberSplit[0]);
                int prevPort = Integer.parseInt(prevMemberSplit[1]);

                LOG.debug("Attempting to contact previous member " + prevMember + " with a join request");
                JoinRequest sender = new JoinRequest(members, prevAddress, prevPort, thisNode, socket, signatureHandler);
                new Thread(sender).start();
            }
        }
        catch (SocketException e)
        {
            LOG.error("Error attempting to initialise node threads: ", e);
        }
        catch (UnknownHostException e)
        {
            LOG.error("Error attempting to get IP from previous member information entered on startup: ", e);
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
    private void initialiseThreads(Members members, SignatureHandler signatureHandler) throws SocketException
    {

        // create the message queue to for the message receiver and consumer to handle
        BlockingQueue messageQueue = new LinkedBlockingQueue(QUEUE_CAPACITY);

        // create the message sender to send messages (e.g. a new joiner request)
        socket = new DatagramSocket(thisNode.getPortNumber());

        // decide on leader election implementation
        LeaderElection election = new BullyLeaderElection(members, thisNode, socket, signatureHandler);

        // initialise to receive UDP messages on this node and add them to the message queue
        MessageReceiver receiver = new MessageReceiver(messageQueue, socket);
        new Thread(receiver).start();

        // initialise to consume messages from the message queue
        MessageConsumer consumer = new MessageConsumer(messageQueue, members, socket, election,
                thisNode, signatureHandler);
        new Thread(consumer).start();

        this.consumer = consumer;
        this.election = election;
    }

    /**
     * Getting the keystore required for digital signatures
     *
     * @return the {@link KeyStore} for this node, based on its ID
     */
    private KeyStore getThisNodesKeyStore()
    {
        String keyStoreFilename = "node-" + thisNode.getId() + ".jks";
        KeyStore keyStore = null;

        try (InputStream is = new FileInputStream(keyStoreFilename))
        {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(is, SecurityConstants.PASSWORD);
        }
        catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e)
        {
            LOG.error("Error attempting to read the keystore from file based on ID (" +
                    thisNode.getId() + "): ", e);
        }

        return keyStore;
    }

    /**
     * Method for testing purposes, to close the socket when closing the thread down to simulate the node going down
     */
    void closeSocket()
    {
        socket.close();
    }

}
