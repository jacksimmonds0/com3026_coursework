package com.surrey.com3026.coursework.member;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Members
{
    private List<Member> members;

    public Members()
    {
        super();
        this.members = new ArrayList<>();
    }


    public Members(Member... members)
    {
        this(Arrays.asList(members));
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

    public void addMembers(Member... members)
    {
        this.members.addAll(Arrays.asList(members));
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
