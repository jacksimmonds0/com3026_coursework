package com.surrey.com3026.coursework.message.checker;

import com.surrey.com3026.coursework.member.Leader;
import com.surrey.com3026.coursework.member.Member;
import com.surrey.com3026.coursework.message.MessageReceiver;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Class to check that all expected members have responded, after new joiner message sent
 * to all members (other than the initial responder)
 *
 */
public class MembersResponseChecker implements Runnable
{
    private static final int TIMEOUT = 10000;

    private MessageReceiver receiver;

    public MembersResponseChecker(MessageReceiver receiver)
    {
        this.receiver = receiver;
    }

    @Override
    public void run()
    {
        // check for all the members who have not responded after a defined delay timeout
        new Timer().schedule(
                new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        List<Member> remaining = receiver.getMembersToCheckAccepted();
                        if(!remaining.isEmpty())
                        {
                            System.out.println("MEMBERS NOT RESPONDING");

                            for(Member member : remaining)
                            {
                                if(member instanceof Leader)
                                {
                                    System.out.println("LEADER: " + member.toString());
                                    // trigger leader election
                                }
                                else
                                {
                                    System.out.println("MEMBER: " + member.toString());
                                    // need to inform all members of new members list
                                }
                            }

                        }
                    }
                }, TIMEOUT
        );

    }
}
