package com.surrey.com3026.coursework.message;

import com.surrey.com3026.coursework.member.Member;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

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

    public Message()
    {
    }

    public Message(String type, Member responder)
    {
        this.type = type;
        this.responder = responder;
    }

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
            return "{ type=" + type +
                    ", members=" + members.toString() +
                    ", responder=" + responder.toString() +
                    "}";
        }
        else
        {
            return "{ type=" + type +
                    ", responder=" + responder.toString() +
                    "}";
        }
    }
}
