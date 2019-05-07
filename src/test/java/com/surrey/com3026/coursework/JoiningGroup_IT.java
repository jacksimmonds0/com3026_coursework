package com.surrey.com3026.coursework;

import com.surrey.com3026.coursework.member.Member;
import com.surrey.com3026.coursework.member.Leader;
import com.surrey.com3026.coursework.message.Message;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * Testing plan
 *
 * Synchronisation_IT   - when a member joins, all members have the correct list get info from all nodes(?)
 * Replication_IT       - TODO how to test this different to sync?
 * FaultTolerance_IT    - testing leader election, create group, kill leader, join node, get info and assert new leader correct
 * Recovery_IT          - create group, kill node, spin same node back up, can get same members list correctly
 * Security_IT          - don't sign the message properly / change the message between signing and sending
 */
public class JoiningGroup_IT extends AbstractNodeTester
{

}
