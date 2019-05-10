package com.surrey.com3026.coursework.message;

import com.surrey.com3026.coursework.member.Member;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class for encapsulating the necessary fields for sending messages between nodes, serialized by JAXB
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Message
{

    @XmlElement
    private String type;

    @XmlElement(name = "member")
    private List<Member> members;

    @XmlElement
    private Member responder;

    @XmlElement
    private Map<Integer, Integer> timestamps;

    @XmlElement
    private byte[] signature;

    /**
     * Default constructor for JAXB
     */
    public Message()
    {
    }

    /**
     * When sending a message without any information (current members) i.e. a request
     *
     * @param type
     *          the type of message to send
     * @param responder
     *          the information for this node to ensure the receiver knows who sent the message
     *          as to who to respond to
     */
    public Message(String type, Member responder)
    {
        this.type = type;
        this.responder = responder;
    }

    /**
     * When sending a message with the information for the current members list
     *
     * @param type
     *          the type of message to send
     * @param members
     *          the list of members to send in this message
     * @param responder
     *          the information for this node to ensure the receiver knows who sent the message
     */
    public Message(String type, List<Member> members, Member responder)
    {
        this.type = type;
        this.members = members;
        this.responder = responder;
        this.timestamps = createVectorClockMap(members);
    }

    public String getType()
    {
        return type;
    }

    public List<Member> getMembers()
    {
        return members;
    }

    public Member getResponder()
    {
        return responder;
    }

    public void setResponder(Member responder)
    {
        this.responder = responder;
    }

    public Map<Integer, Integer> getTimestamps()
    {
        return timestamps;
    }

    public void setTimestamps(Map<Integer, Integer> timestamps)
    {
        this.timestamps = timestamps;
    }

    public byte[] getSignature()
    {
        return signature;
    }

    public void setSignature(byte[] signature)
    {
        this.signature = signature;
    }



    /**
     * Creating a vector clock map where the key is the members ID and the value is the timestamp
     *
     * @param members
     *          the members to generate the map of timestamps from
     * @return the map of timestamps
     */
    public Map<Integer, Integer> createVectorClockMap(List<Member> members)
    {
        return members.stream()
                .collect(Collectors.toMap(Member::getId, Member::getLamportTimestamp));
    }

    @Override
    public String toString()
    {
        if (members != null)
        {
            return "{type=" + type +
                    ", members=" + members.toString() +
                    ", responder=" + responder.toString() +
                    "}";
        }
        else
        {
            return "{type=" + type +
                    ", responder=" + responder.toString() +
                    "}";
        }
    }
}
