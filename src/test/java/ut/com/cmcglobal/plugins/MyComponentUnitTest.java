package ut.com.cmcglobal.plugins;

import org.junit.Test;
import com.cmcglobal.plugins.api.MyPluginComponent;
import com.cmcglobal.plugins.impl.MyPluginComponentImpl;

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