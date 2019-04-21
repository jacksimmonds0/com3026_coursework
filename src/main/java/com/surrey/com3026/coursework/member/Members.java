package com.surrey.com3026.coursework.member;

import java.util.ArrayList;
import java.util.List;

/**
 * Class holding information on the current members in the group network
 */
public class Members
{
    private List<Member> members;

    public Members()
    {
        super();
        this.members = new ArrayList<>();
    }

    public Members(List<Member> members)
    {
        super();
        this.members = members;
    }

    public void addMember(Member member)
    {
        this.members.add(member);
    }

    public List<Member> getMembers()
    {
        return members;
    }

    public void setMembers(List<Member> members)
    {
        this.members = members;
    }

    public void removeMembers(List<Member> membersToRemove)
    {
        for(Member member : membersToRemove)
        {
            this.members.remove(member);
        }
    }

    @Override
    public String toString()
    {
        StringBuilder s = new StringBuilder();
        for(Member m : members)
        {
            s.append(m.toString()).append("\n");
        }

        return s.toString();
    }


}
