package com.surrey.com3026.coursework.member;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * An extension of the member class to differentiate a specific node as a leader of the group
 * Also has annotations for JAXB
 */
@XmlRootElement
public class Leader extends Member {

    @XmlElement
    private boolean isLeader;

    /**
     * Default constructor for JAXB
     */
    public Leader()
    {
    }

    /**
     * Instantiate the group leader object
     *
     * @param id
     *          the id of the leader
     * @param portNumber
     *          the port number of the leader
     */
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
