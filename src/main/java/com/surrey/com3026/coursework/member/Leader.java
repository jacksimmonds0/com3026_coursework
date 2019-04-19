package com.surrey.com3026.coursework.member;

import java.net.UnknownHostException;

public class Leader extends Member {

    public Leader(int id, int portNumber) throws UnknownHostException
    {
        super(id, portNumber);
    }

    @Override
    public String toString() {
        return "{" +
                "id=" + id +
                ", ipAddress=" + ipAddress.getHostAddress() +
                ", portNumber=" + portNumber +
                ", isLeader=true" +
                '}';
    }
}
