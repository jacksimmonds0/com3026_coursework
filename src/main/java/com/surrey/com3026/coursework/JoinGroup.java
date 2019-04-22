package com.surrey.com3026.coursework;

import com.surrey.com3026.coursework.member.Leader;
import com.surrey.com3026.coursework.member.Member;
import com.surrey.com3026.coursework.member.Members;
import com.surrey.com3026.coursework.message.MessageReceiver;
import com.surrey.com3026.coursework.message.sender.JoinRequest;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class JoinGroup
{

    public static void main(String[] args)
    {
        try
        {
            int id = Integer.parseInt(args[0]);
            int portNumber = Integer.parseInt(args[1]);

            // instantiate the object holding the current list of members in the group
            Members members = new Members();
            Member thisNode = null;

            // create the message sender to send messages (e.g. a new joiner request)
            DatagramSocket socket = new DatagramSocket(portNumber);

            // initialise to receive UDP messages on this node
            MessageReceiver receiver = new MessageReceiver(members, socket);
            new Thread(receiver).start();

            if(args.length == 2)
            {
                thisNode = new Leader(id, portNumber);
                System.out.println("Leader created");
            }
            else
            {
                thisNode = new Member(id, portNumber);

                // use previous member to retrieve all current members list to update
                String[] prevMember = args[2].split(":");
                InetAddress prevAddress = InetAddress.getByName(prevMember[0]);
                int prevPort = Integer.parseInt(prevMember[1]);

                JoinRequest sender = new JoinRequest(members, prevAddress, prevPort, thisNode, socket);
                new Thread(sender).start();
            }
            members.addMember(thisNode);
            receiver.setThisNode(thisNode);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}

