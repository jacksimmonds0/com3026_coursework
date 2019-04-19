package com.surrey.com3026.coursework.member;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Member
{
    private int id;

    private InetAddress ipAddress;

    private int portNumber;

    public Member(int id, int portNumber) throws UnknownHostException
    {
        this.id = id;
        this.ipAddress = InetAddress.getLocalHost();
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
}
