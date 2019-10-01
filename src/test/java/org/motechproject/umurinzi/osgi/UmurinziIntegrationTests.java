package org.motechproject.umurinzi.osgi;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Umurinzi bundle integration tests suite.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        UmurinziWebIT.class,
        UmurinziConfigServiceIT.class,
        LookupServiceIT.class,
        SubjectServiceIT.class
})
public class UmurinziIntegrationTests {
}
