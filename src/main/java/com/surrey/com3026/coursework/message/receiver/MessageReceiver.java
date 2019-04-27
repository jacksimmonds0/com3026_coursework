package com.surrey.com3026.coursework.message.receiver;

import com.surrey.com3026.coursework.message.Message;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.BlockingQueue;


/**
 * Class to listen for messages from other nodes that have joined or are trying to join the group
 */
public class MessageReceiver implements Runnable
{
    private static final int BUFFER_SIZE = 4096;

    private BlockingQueue messageQueue;

    private DatagramSocket socket;

    /**
     * Instantiate a receiver to get messages from the socket and place them on the queue
     *
     * @param messageQueue
     *          the {@link BlockingQueue} to place messages on
     * @param socket
     *          the socket to receive packets on
     */
    public MessageReceiver(BlockingQueue messageQueue, DatagramSocket socket)
    {
        this.messageQueue = messageQueue;
        this.socket = socket;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run()
    {
        try
        {
            // continuous loop, always need to listen for messages while the node is still running
            while(true)
            {

                byte[] buffer = new byte[BUFFER_SIZE];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                socket.receive(packet);
                String messageReceived = new String(packet.getData(), packet.getOffset(), packet.getLength());

                // unmarshalling the message to a Message object to then place on the queue
                JAXBContext context = JAXBContext.newInstance(Message.class);
                Unmarshaller unmarshallerObj = context.createUnmarshaller();
                Message message = (Message) unmarshallerObj.unmarshal(new StringReader(messageReceived));

                if (message != null)
                {
                    messageQueue.put(message);
                }
            }
        }
        catch (IOException | JAXBException | InterruptedException e)
        {
            e.printStackTrace();
        }
    }


}
