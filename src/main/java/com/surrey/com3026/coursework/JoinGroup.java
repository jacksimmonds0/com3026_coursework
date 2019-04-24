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
import java.sql.SQLOutput;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class JoinGroup
{

    public static void main(String[] args)
    {

        int id = Integer.parseInt(args[0]);
        int portNumber = Integer.parseInt(args[1]);

        // instantiate the Node object to start the program
        Node node = null;

        if(args.length == 2)
        {
            node = new Node(id, portNumber);
            System.out.println("Leader created");
        }
        else if (args.length == 3)
        {
            node = new Node(id, portNumber, args[2]);
        }
        else
        {
            System.out.println("Too few arguments, need to have ID, port, and optionally previousIP:previousPort");
            // TODO throw exception here?
            return;
        }

        // start the node to join the group
        node.startNode();

    }
}

