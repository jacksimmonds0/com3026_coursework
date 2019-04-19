package com.surrey.com3026.coursework.member;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Member
{
    private static final String LOCALHOST = "127.0.0.1";

    protected int id;

    protected InetAddress ipAddress;

    protected int portNumber;

    public Member(int id, int portNumber) throws UnknownHostException
    {
        this.id = id;
        // for the purposes of this implementation, IP address limited to localhost
        this.ipAddress = InetAddress.getByName(LOCALHOST);
        this.portNumber = portNumber;
    }

    public int getId()
    {
        return id;
    }

    public InetAddress getIpAddress()
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
                ", ipAddress=" + ipAddress.getHostAddress() +
                ", portNumber=" + portNumber +
                '}';
    }
}
