package com.surrey.com3026.coursework;

import org.apache.log4j.Logger;

public class JoinGroup
{
    private static final Logger LOG =  Logger.getLogger(JoinGroup.class);

    public static void main(String[] args)
    {

        int id = Integer.parseInt(args[0]);
        int portNumber = Integer.parseInt(args[1]);

        // instantiate the Node object to start the program
        Node node;

        if(args.length == 2)
        {
            node = new Node(id, portNumber);
            LOG.debug("Leader created with id=" + id + ", port=" + portNumber);
        }
        else if (args.length == 3)
        {
            String prevMemberInfo = args[2];
            node = new Node(id, portNumber, prevMemberInfo);
            LOG.debug("Member created with id=" + id + ", port=" + portNumber + " - will attempt to join by " +
                    "contacting the node on " + prevMemberInfo);
        }
        else
        {
            LOG.error("Too few arguments, need to have ID, port, and optionally previousIP:previousPort");
            return;
        }

        // start the node to join the group
        node.startNode();

    }
}

