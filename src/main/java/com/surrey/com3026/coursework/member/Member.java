package com.surrey.com3026.coursework.member;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Class for modelling a member of the group with appropriate information, annotations so
 * it can be marshalled/unmarhalled via JAXB
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
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

    @XmlElement
    protected int lamportTimestamp;

    /**
     * Default constructor for JAXB
     */
    public Member()
    {
    }

    /**
     * Instantiate a member of the group
     *
     * @param id
     *          the ID of the member
     * @param portNumber
     *          the port number of the member
     */
    public Member(int id, int portNumber)
    {
        this.id = id;
        // for the purposes of this implementation, IP address limited to localhost
        this.ipAddress = LOCALHOST;
        this.portNumber = portNumber;
        this.lamportTimestamp = 0;
    }

    /**
     * @return the members ID
     */
    public int getId()
    {
        return id;
    }

    /**
     * Get the IP address as an {@link InetAddress}
     *
     * @return the {@link InetAddress} for the IP string for this member
     * @throws UnknownHostException if the host does not exist
     */
    public InetAddress getIp() throws UnknownHostException
    {
        return InetAddress.getByName(ipAddress);
    }

    /**
     * @return the members IP address as a String
     */
    public String getIpAddress()
    {
        return ipAddress;
    }

    /**
     * @return the members port number it is listening on
     */
    public int getPortNumber()
    {
        return portNumber;
    }

    /**
     * @return the logical lamport timestamp for this member object
     */
    public int getLamportTimestamp()
    {
        return lamportTimestamp;
    }

    /**
     * Set the logical lamport timestamp for this node
     *
     * @param lamportTimestamp
     *          the timestamp to be set
     */
    public void setLamportTimestamp(int lamportTimestamp)
    {
        this.lamportTimestamp = lamportTimestamp;
    }

    /**
     * Increment the lamport timestamp
     */
    public void incrementTimestamp()
    {
        lamportTimestamp++;
    }

    @Override
    public String toString() {
        return "{" +
                 "id=" + id +
                ", ipAddress=" + ipAddress +
                ", portNumber=" + portNumber +
                ", lamportTimestamp=" + lamportTimestamp +
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

    @Override
    public int hashCode()
    {
        // since the ID should be unique, the same object is reflected by the ID property
        // so this can act as the hash code
        return id;
    }
}
