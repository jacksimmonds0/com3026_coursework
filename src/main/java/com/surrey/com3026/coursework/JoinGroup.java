package com.surrey.com3026.coursework;

import com.surrey.com3026.coursework.member.Leader;
import com.surrey.com3026.coursework.member.Member;
import com.surrey.com3026.coursework.message.MessageReceiver;
import com.surrey.com3026.coursework.message.MessageSender;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class JoinGroup
{

    public static void main(String[] args)
    {
        try
        {
            int id = Integer.parseInt(args[0]);
            int portNumber = Integer.parseInt(args[1]);
            InetAddress localAddress = InetAddress.getLocalHost();

            // initialise to retrieve UDP messages on this node
            MessageReceiver receiver = new MessageReceiver(portNumber);
            new Thread(receiver).start();

            if(args.length == 2)
            {
                Member member = new Leader(id, portNumber);
                System.out.println("Leader created");
            }
            else
            {
                Member member = new Member(id, portNumber);

                // use previous member to retrieve all current members list to update
                String[] prevMember = args[2].split(":");
                InetAddress prevIp = InetAddress.getByName(prevMember[0]);
                int prevPort = Integer.parseInt(prevMember[1]);

                MessageSender sender = new MessageSender();
                sender.sendMessage(prevIp, prevPort, "I love the leader!");
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}

