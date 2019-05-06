package com.surrey.com3026.coursework.member;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class holding information on the current members in the group network
 */
public class Members
{
    private List<Member> members;

    /**
     * Default constructor to create a new list of members
     */
    public Members()
    {
        super();
        this.members = new ArrayList<>();
    }

    /**
     * Construct a members object with an existing list of members
     *
     * @param members
     *          the list of members
     */
    public Members(List<Member> members)
    {
        super();
        this.members = members;
    }

    /**
     * Add a member to the current list of members
     *
     * @param member
     *          the member to add
     */
    public void addMember(Member member)
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
    public List<Member> getMembers()
    {
        return members;
    }

    /**
     * @param members
     *          the current members to set to
     */
    public void setMembers(List<Member> members)
    {
        this.members = members;
    }

    /**
     * Remove a sub-set of members from the current list
     *
     * @param membersToRemove
     *          the list to remove
     */
    public void removeMembers(List<Member> membersToRemove)
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
    public void replaceMember(int index, Member replacer)
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
