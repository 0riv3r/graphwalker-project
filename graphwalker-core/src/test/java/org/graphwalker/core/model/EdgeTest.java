package org.graphwalker.core.model;

/*
 * #%L
 * GraphWalker Core
 * %%
 * Copyright (C) 2005 - 2014 GraphWalker
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

/**
 * @author Nils Olsson
 */
public class EdgeTest {

    @Test
    public void create() {
        Edge edge = new Edge()
                .setGuard(new Guard("script"))
                .setName("name")
                .setSourceVertex(new Vertex())
                .setTargetVertex(new Vertex())
                .setActions(Arrays.asList(new Action("action2"), new Action("action3")))
                .addAction(new Action("action1"))
                .setWeight(.5);
        Assert.assertNotNull(edge);
        Assert.assertEquals("name", edge.getName());
        Assert.assertEquals("name", edge.build().getName());
        Assert.assertNotNull(edge.getSourceVertex());
        Assert.assertNotNull(edge.build().getTargetVertex());
        Assert.assertNotNull(edge.getTargetVertex());
        Assert.assertNotNull(edge.build().getTargetVertex());
        Assert.assertNotNull(edge.getGuard());
        Assert.assertNotNull(edge.build().getGuard());
        Assert.assertTrue(edge.build().hasGuard());
        Assert.assertEquals(edge.getGuard(), edge.build().getGuard());
        Assert.assertNotNull(edge.getActions());
        Assert.assertThat(edge.getActions().size(), is(3));
        Assert.assertThat(edge.getWeight(), is(.5));
        Assert.assertNotNull(edge.build().getActions());
        Assert.assertThat(edge.build().getActions().size(), is(3));
        Assert.assertThat(edge.build().getWeight(), is(.5));
        Assert.assertFalse(edge.setGuard(null).build().hasGuard());
        Assert.assertFalse(edge.setGuard(new Guard("")).build().hasGuard());
    }

    @Test
    public void testEquality() throws Exception {
        Edge e1 = new Edge().setId("ID1");
        Edge e2 = new Edge().setId("ID1");
        Assert.assertThat(e1.build(), is(e2.build()));
    }

    @Test
    public void testInequality() throws Exception {
        Edge e1 = new Edge().setId("ID1");
        Edge e2 = new Edge().setId("ID2");
        Assert.assertThat(e1.build(), not(e2.build()));
    }

    @Test
    public void testProperties() throws Exception {
        Edge edge = new Edge().setId("ID");
        Assert.assertFalse(edge.build().hasProperties());
        Assert.assertTrue(edge.setProperty("x", "y").build().hasProperties());
        Assert.assertThat((String)edge.getProperty("x"), is("y"));
        Assert.assertThat((String)edge.build().getProperty("x"), is("y"));
        //Assert.assertThat((String)edge.remove("x"), is("y"));
        //Assert.assertFalse(edge.build().hasProperty("x"));
    }
}
