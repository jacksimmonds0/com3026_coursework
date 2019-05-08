package com.surrey.com3026.coursework;

import com.surrey.com3026.coursework.member.Leader;
import com.surrey.com3026.coursework.member.Member;
import com.surrey.com3026.coursework.member.Members;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;

/**
 * Test cases to ensure the system can recover from failure - via log checkpoints
 */
public class Recovery_IT extends AbstractNodeTester
{
    private static final String EXAMPLE_LOG = "example-" + LOG_FILENAME;

    private static final String XML_LOG_ENTRY = "<?xml";

    private static final String NO_CHECKPOINT_ENTRY = "No checkpoint found in log file.";

    private static final String CHECKPOINT_FOUND = "Members retrieved from checkpoint: ";

    private static final String MEMBERS_LIST = "[{id=4, ipAddress=127.0.0.1, portNumber=8004}, {id=3, ipAddress=127.0.0.1, portNumber=8003}, {id=1, ipAddress=127.0.0.1, portNumber=8001, isLeader=true}, {id=2, ipAddress=127.0.0.1, portNumber=8002}]";


    @Test
    public void test_can_recover_previous_members_list_from_log() throws InterruptedException
    {
        useTestLogFile(LOG_FILENAME);
        startLogNodeTest(CHECKPOINT_FOUND);
    }

    @Test
    public void test_no_checkpoints_without_previous_log_file() throws InterruptedException
    {
        useTestLogFile(EXAMPLE_LOG);
        startLogNodeTest(NO_CHECKPOINT_ENTRY);
    }

    @Test
    public void test_writes_current_members_to_log_file() throws InterruptedException
    {
        // turn on logging for this test
        Logger.getLogger("com.surrey.com3026.coursework").setLevel(Level.DEBUG);

        useTestLogFile(EXAMPLE_LOG);

        // establish initial group
        Node n1 = createNode("1", "8001");
        Node n2 = createNode("2", "8002", "127.0.0.1:8001");
        Node n3 = createNode("3", "8003", "127.0.0.1:8002");
        startThreads(n1, n2, n3);

        // then add the new joiner
        Thread.sleep(TIMEOUT);
        Node n4 = createNode("4", "8004", "127.0.0.1:8003");
        startThreads(n4);

        Thread.sleep(TIMEOUT);

        List<Member> expectedMembers = Arrays.asList(
                new Leader(1, 8001),
                new Member(2, 8002),
                new Member(3, 8003),
                new Member(4, 8004)
        );

        assertLogFileEntry(expectedMembers, XML_LOG_ENTRY);
    }

    private void useTestLogFile(String filename)
    {
        File source = new File("src/test/resources/" + filename);
        File dest = new File(LOG_FILENAME);

        try
        {
            FileUtils.copyFile(source, dest);
        }
        catch (IOException e)
        {
            fail();
        }
    }

    private void startLogNodeTest(String expectedEntry) throws InterruptedException
    {
        // turn on logging for this test
        Logger.getLogger("com.surrey.com3026.coursework").setLevel(Level.DEBUG);

        Node n1 = createNode("1", "8001");
        startThreads(n1);
        Thread.sleep(TIMEOUT);

        assertLogFileEntry(null, expectedEntry);
    }


    /**
     * Finding the last instance of an expected entry in the log file
     *
     * @param expectedMembers
     *          the list of expected members to marshall and assert against
     * @param expectedEntry
     *          the expected log entry to the log file
     */
    private void assertLogFileEntry(List<Member> expectedMembers, String expectedEntry)
    {
        List<String> array = new ArrayList<>();
        try
        {
            array = Files.readAllLines(new File(LOG_FILENAME).toPath(), Charset.defaultCharset());
        }
        catch(IOException e)
        {
            fail();
        }

        boolean exists = new File(LOG_FILENAME).exists();

        if (!array.isEmpty())
        {
            for (int i = array.size() - 1; i >= 0; i--)
            {
                String line = array.get(i);
                String[] lineSplit = line.split(" - ");

                if (lineSplit.length != 2)
                {
                    continue;
                }

                String logMessage = lineSplit[1];
                if (logMessage.startsWith(expectedEntry))
                {
                    if (logMessage.startsWith(XML_LOG_ENTRY))
                    {
                        Members members = unmarshallMembers(logMessage);
                        List<Member> actualMembers = members.getMembers();

                        assertNotNull(members);
                        assertMembersListSame(expectedMembers, actualMembers);

                        // only assert the last instance of members
                        return;
                    }
                    else if (logMessage.startsWith(NO_CHECKPOINT_ENTRY))
                    {
                        assertEquals(NO_CHECKPOINT_ENTRY, logMessage);
                        return;
                    }
                    else if (logMessage.startsWith(CHECKPOINT_FOUND))
                    {
                        assertEquals(CHECKPOINT_FOUND + MEMBERS_LIST, logMessage);
                        return;
                    }
                }
            }
        }

        // no message found = fail test
        fail();
    }

    private Members unmarshallMembers(String logMessage)
    {
        try
        {
            JAXBContext context = JAXBContext.newInstance(Members.class);
            Unmarshaller unmarshallerObj = context.createUnmarshaller();
            return (Members) unmarshallerObj.unmarshal(new StringReader(logMessage));
        }
        catch (JAXBException e)
        {
            fail();
        }

        return null;
    }


}
