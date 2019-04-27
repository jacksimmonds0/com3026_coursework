package com.surrey.com3026.coursework.member;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Leader extends Member {

    @XmlElement
    private boolean isLeader;

    public Leader()
    {
    }

    public Leader(int id, int portNumber)
    {
        super(id, portNumber);
        this.isLeader = true;
    }

    @Override
    public String toString() {
        return "{" +
                "id=" + id +
                ", ipAddress=" + ipAddress +
                ", portNumber=" + portNumber +
                ", isLeader=" + isLeader +
                '}';
    }
}
