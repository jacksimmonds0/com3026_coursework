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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class JoinGroup
{

    /**
     * TODO refactor this into...
     * TODO     -> basic getting args[0] etc.
     * TODO     -> pass to another object to initialise all threads, socket etc.
     */
    public static void main(String[] args)
    {
        try
        {
            int id = Integer.parseInt(args[0]);
            int portNumber = Integer.parseInt(args[1]);

            // instantiate the object holding the current list of members in the group
            Members members = new Members();
            Member thisNode = null;

            if(args.length == 2)
            {
                thisNode = new Leader(id, portNumber);
                System.out.println("Leader created");
            }
            else
            {
                thisNode = new Member(id, portNumber);

            }
            members.addMember(thisNode);

            // create the message queue to for the message receiver and consumer to handle
            BlockingQueue messageQueue = new LinkedBlockingQueue(20);

            // create the message sender to send messages (e.g. a new joiner request)
            DatagramSocket socket = new DatagramSocket(portNumber);

            // decide on leader election implementation
            LeaderElection election = new BullyLeaderElection(members, thisNode, socket);

            // initialise to receive UDP messages on this node and add them to the message queue
            MessageReceiver receiver = new MessageReceiver(messageQueue, socket);
            new Thread(receiver).start();

            // initialise to consume messages from the message queue
            MessageConsumer consumer = new MessageConsumer(messageQueue, members, socket, election, thisNode);
            new Thread(consumer).start();

            if (!(thisNode instanceof Leader))
            {
                // use previous member to retrieve all current members list to update
                String[] prevMember = args[2].split(":");
                InetAddress prevAddress = InetAddress.getByName(prevMember[0]);
                int prevPort = Integer.parseInt(prevMember[1]);

                JoinRequest sender = new JoinRequest(members, prevAddress, prevPort, thisNode, socket);
                new Thread(sender).start();
            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}

