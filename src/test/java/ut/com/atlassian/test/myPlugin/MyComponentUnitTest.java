package ut.com.atlassian.test.myPlugin;

import org.junit.Test;
import com.atlassian.test.myPlugin.api.MyPluginComponent;
import com.atlassian.test.myPlugin.impl.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent",component.getName());
    }
}