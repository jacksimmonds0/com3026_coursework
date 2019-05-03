package com.surrey.com3026.coursework.member;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.net.InetAddress;
import java.net.UnknownHostException;

@XmlRootElement
@XmlSeeAlso(Leader.class)
public class Member
{
    private static final String LOCALHOST = "127.0.0.1";

    @XmlAttribute
    protected int id;

    @XmlElement
    protected String ipAddress;

    @XmlElement
    protected int portNumber;

    public Member()
    {
    }

    public Member(int id, int portNumber)
    {
        this.id = id;
        // for the purposes of this implementation, IP address limited to localhost
        this.ipAddress = LOCALHOST;
        this.portNumber = portNumber;
    }

    public int getId()
    {
        return id;
    }

    public InetAddress getIp() throws UnknownHostException
    {
        return InetAddress.getByName(ipAddress);
    }

    public String getIpAddress()
    {
        return ipAddress;
    }

    public int getPortNumber()
    {
        return portNumber;
    }

    @Override
    public String toString() {
        return "{" +
                 "id=" + id +
                ", ipAddress=" + ipAddress +
                ", portNumber=" + portNumber +
                '}';
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj == this)
        {
            return true;
        }
        else if(!(obj instanceof Member))
        {
            return false;
        }

        Member objMember = (Member) obj;
        return id == objMember.getId() && ipAddress.equals(objMember.getIpAddress())
                && portNumber == objMember.getPortNumber();
    }
}
