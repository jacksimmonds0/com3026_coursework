package com.surrey.com3026.coursework.message;

import com.surrey.com3026.coursework.member.Member;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

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

    public byte[] getSignature()
    {
        return signature;
    }

    public void setSignature(byte[] signature)
    {
        this.signature = signature;
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
