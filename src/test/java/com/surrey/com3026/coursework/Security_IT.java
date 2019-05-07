package com.surrey.com3026.coursework;

import com.surrey.com3026.coursework.member.Leader;
import com.surrey.com3026.coursework.member.Member;
import com.surrey.com3026.coursework.message.Message;
import com.surrey.com3026.coursework.message.MessageTypes;
import com.surrey.com3026.coursework.security.SignatureHandler;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.StringWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;

/**
 * Test cases to ensure the digital signature scheme works as intended, no message returned if either the integrity check
 * or the signature itself is wrong, and that a message is returned when both are correct
 */
public class Security_IT extends AbstractNodeTester
{
    @Test
    public void test_correct_signature_returns_get_info() throws InterruptedException
    {
        Node n1 = createNode("1", "8001");
        Node n2 = createNode("2", "8002", "127.0.0.1:8001");
        startThreads(n1, n2);

        Thread.sleep(TIMEOUT);

        // sending the correct message so response should be received containing message
        sendSecurityCheckMessage(8002, false, getTesterSignatureHandler(TESTER_ID));

        Message message = null;
        try
        {
            byte[] buf = new byte[4096];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            receivePacket(packet);

            String messageReceived = new String(packet.getData(), packet.getOffset(), packet.getLength());
            message = unmarshallMessage(messageReceived);
        }
        catch (IOException | JAXBException e)
        {
            fail();
        }

        List<Member> expected = Arrays.asList(
                new Leader(1, 8001),
                new Member(2, 8002)
        );

        // check information received is as expected
        assertNotNull(message);
        assertEquals(MessageTypes.ALL_CURRENT_MEMBERS, message.getType());
        assertEquals(expected, message.getMembers());
    }

    @Test
    public void test_no_response_when_message_changed_from_hash() throws InterruptedException
    {
        Node n1 = createNode("1", "8001");
        Node n2 = createNode("2", "8002", "127.0.0.1:8001");
        startThreads(n1, n2);

        Thread.sleep(TIMEOUT);

        // set message change to true for integrity check
        sendSecurityCheckMessage(8002, true, getTesterSignatureHandler(TESTER_ID));

        // no response expected due to integrity failure on receiver node
        if (responseReceived())
        {
            fail();
        }
    }

    @Test
    public void test_no_response_when_signature_using_wrong_key() throws InterruptedException
    {
        Node n1 = createNode("1", "8001");
        Node n2 = createNode("2", "8002", "127.0.0.1:8001");
        startThreads(n1, n2);

        Thread.sleep(TIMEOUT);

        // using the wrong ID for the key
        sendSecurityCheckMessage(8002, false, getTesterSignatureHandler(15));

        // no response expected as signature is from the wrong node so signature verification fails
        if (responseReceived())
        {
            fail();
        }
    }

    private boolean responseReceived()
    {
        byte[] buf = new byte[4096];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        try
        {
            receivePacket(packet);
        }
        catch (SocketTimeoutException e)
        {
            // success = no message received after timeout
            return false;
        }
        catch (IOException e)
        {
            fail();
        }

        try
        {
            String messageReceived = new String(packet.getData(), packet.getOffset(), packet.getLength());
            unmarshallMessage(messageReceived);

            // fail if can unmarshall message as no message should be received
            return true;
        }
        catch (JAXBException e)
        {
            return false;
        }
    }

    private DatagramPacket receivePacket(DatagramPacket packet) throws SocketTimeoutException, IOException
    {

        // set timeout for the socket to receive a message
        socket.setSoTimeout(TIMEOUT);
        socket.receive(packet);

        return packet;
    }

    private SignatureHandler getTesterSignatureHandler(int id)
    {
        return new SignatureHandler(getTesterKeyStore(id));
    }


    private void sendSecurityCheckMessage(int port, boolean changeMessage, SignatureHandler signatureHandler)
    {
        try
        {
            Member tester = new Member(TESTER_ID, 8999);
            Message message = new Message(MessageTypes.GET_INFO, tester);

            if (changeMessage)
            {
                // for testing if the message is different when signed this will be flagged and no response received
                Message differentMessage = new Message(MessageTypes.JOIN_REQUEST, tester);
                message.setSignature(signatureHandler.sign(differentMessage.toString().getBytes()));
            }
            else
            {
                message.setSignature(signatureHandler.sign(message.toString().getBytes()));
            }

            JAXBContext context = JAXBContext.newInstance(Message.class);

            Marshaller marshallObj = context.createMarshaller();
            marshallObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            final StringWriter dataWriter = new StringWriter();
            marshallObj.marshal(message, dataWriter);

            String messageString = dataWriter.toString();
            byte[] buffer = messageString.getBytes();

            DatagramPacket packet = new DatagramPacket(
                    buffer, buffer.length, InetAddress.getByName("127.0.0.1"), port
            );

            // creating socket for request/response
            socket.send(packet);
        }
        catch (Exception e)
        {
            fail();
        }
    }
}
