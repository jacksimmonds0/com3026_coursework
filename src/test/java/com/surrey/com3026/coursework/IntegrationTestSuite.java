package com.surrey.com3026.coursework;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        Synchronization_IT.class,
        Replication_IT.class,
        FaultTolerance_IT.class,
        Recovery_IT.class,
        Security_IT.class
})
public class IntegrationTestSuite
{
}
