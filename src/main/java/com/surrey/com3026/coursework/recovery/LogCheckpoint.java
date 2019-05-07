package com.surrey.com3026.coursework.recovery;

import com.surrey.com3026.coursework.member.Member;
import com.surrey.com3026.coursework.member.Members;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class LogCheckpoint
{
    private static final Logger LOG = Logger.getLogger(LogCheckpoint.class);

    private static final String LOG_FILENAME = "node.log";

    private Members members;

    /**
     *
     * @param members
     *          the current {@link Members} to update if found in the log file
     */
    public LogCheckpoint(Members members)
    {
        this.members = members;
    }

    /**
     * Iterate backwards over the log file and try to find the last instance of the members list
     * If its found, update the members object with that list
     *
     * @return true if members have been updated (to send a joiner message to everyone), false otherwise
     */
    public boolean getLastMembersFromLogFile()
    {
        List<String> logFile = loadLogFileToList();

        if (!logFile.isEmpty())
        {
            for (int i = logFile.size() - 1; i >= 0; i--)
            {
                String line = logFile.get(i);
                String logMessage = line.split(" - ")[1];

                if (logMessage.startsWith("<?xml"))
                {
                    List<Member> currentMembers = unmarshallMembersFromLog(logMessage).getMembers();

                    if (currentMembers != null)
                    {
                        // update members list based on log file if it exists
                        currentMembers.forEach(member -> members.addMember(member));
                        LOG.debug("Members retrieved from checkpoint: " + members);

                        return true;
                    }
                }
            }
        }

        LOG.debug("No checkpoint found in log file.");
        return false;
    }

    private List<String> loadLogFileToList()
    {
        List<String> array = new ArrayList<>();
        try(Scanner s = new Scanner(new File(LOG_FILENAME));)
        {
            while (s.hasNextLine())
            {
                array.add(s.nextLine());
            }
        }
        catch(FileNotFoundException e)
        {
            // debug as this is expected if the node is starting for the first time
            LOG.debug("No log file found.");
        }

        return array;
    }

    private Members unmarshallMembersFromLog(String logMessage)
    {
        Members currentMembers = null;
        try
        {
            JAXBContext context = JAXBContext.newInstance(Members.class);
            Unmarshaller unmarshallerObj = context.createUnmarshaller();
            currentMembers = (Members) unmarshallerObj.unmarshal(new StringReader(logMessage));
        }
        catch (JAXBException e)
        {
            LOG.error("Unable to unmarshall members from log file: ", e);
        }

        return currentMembers;
    }
}
