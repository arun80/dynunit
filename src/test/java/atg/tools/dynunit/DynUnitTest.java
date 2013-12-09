package atg.tools.dynunit;

import atg.nucleus.Nucleus;
import atg.nucleus.logging.InfoLogEvent;
import atg.tools.dynunit.droplet.SimpleFormHandler;
import atg.tools.dynunit.nucleus.logging.ApacheLogListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import javax.inject.Named;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author msicker
 * @version 1.0.0
 */
public class DynUnitTest {

    @Inject
    @Nuke("src/test/resources/config/simple")
    private Nucleus nucleus;

    @Inject
    @Named("/test/SimpleFormHandler")
    private SimpleFormHandler handler;

    @Inject
    @Named("/atg/dynamo/service/logging/ApacheLog4j2")
    private ApacheLogListener logListener;

    @Before
    public void setUp()
            throws Exception {
        DynUnit.init(this);
    }

    @After
    public void tearDown()
            throws Exception {
        DynUnit.stop(this);
    }

    @Test
    public void testNucleusInjected()
            throws Exception {
        assertThat(nucleus, is(notNullValue()));
    }

    @Test
    public void testSimpleFormHandlerResolved()
            throws Exception {
        assertThat(handler, is(notNullValue()));
        assertThat(handler.getErrorURL(), is(equalTo("/test.jsp")));
    }

    @Test
    public void testLogListener()
            throws Exception {
        assertThat(logListener, is(notNullValue()));
        logListener.logEvent(new InfoLogEvent("This is a test info message."));
    }
}
