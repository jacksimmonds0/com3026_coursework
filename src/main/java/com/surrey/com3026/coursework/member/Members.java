package com.surrey.com3026.coursework.member;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class holding information on the current members in the group network
 */
@XmlRootElement(name = "members")
@XmlAccessorType(XmlAccessType.FIELD)
public class Members
{
    @XmlElement
    private List<Member> members;

    /**
     * Default constructor to create a new list of members
     */
    public Members()
    {
        super();
        // ensure list is synchronised as the Members object will be accessed by multiple threads
        this.members = Collections.synchronizedList(new ArrayList<>());
    }

    /**
     * Add a member to the current list of members
     *
     * @param member
     *          the member to add
     */
    public synchronized void addMember(Member member)
    {
        // prevent duplicates
        if (!members.contains(member))
        {
            this.members.add(member);
        }
    }

    /**
     * @return the current list of members
     */
    public synchronized List<Member> getMembers()
    {
        return members;
    }

    /**
     * @param members
     *          the current members to set to
     */
    public synchronized void setMembers(List<Member> members)
    {
        this.members = members;
    }

    /**
     * Remove a sub-set of members from the current list
     *
     * @param membersToRemove
     *          the list to remove
     */
    public synchronized void removeMembers(List<Member> membersToRemove)
    {
        for(Member member : membersToRemove)
        {
            this.members.remove(member);
        }
    }

    /**
     * Replace a member at an index in the list of current members
     *
     * @param index
     *          the index to replace at
     * @param replacer
     *          the member to replace the previous member with
     */
    public synchronized void replaceMember(int index, Member replacer)
    {
        members.set(index, replacer);
    }

    @Override
    public String toString()
    {
        return members.stream()
                .map(Member::toString)
                .collect(Collectors.joining(", ", "[", "]"));
    }


}
