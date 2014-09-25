package org.graphwalker.core.generator;

/*
 * #%L
 * GraphWalker Core
 * %%
 * Copyright (C) 2011 - 2014 GraphWalker
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
import org.graphwalker.core.condition.ReachedVertex;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.machine.TestExecutionContext;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;
import org.junit.Test;

/**
 * @author Nils Olsson
 */
public class CombinedPathTest {

    Vertex start = new Vertex();
    Vertex v1 = new Vertex().setName("v1");
    Vertex v2 = new Vertex().setName("v2");
    Model model = new Model()
        .addEdge(new Edge().setSourceVertex(start).setTargetVertex(v1))
        .addEdge(new Edge().setSourceVertex(v1).setTargetVertex(v2));

    @Test
    public void simpleTest() {
        CombinedPath pathGenerator = new CombinedPath();
        pathGenerator.addPathGenerator(new RandomPath(new ReachedVertex("v1")));
        pathGenerator.addPathGenerator(new RandomPath(new ReachedVertex("v2")));
        Context context = new TestExecutionContext(model, pathGenerator);
        context.setCurrentElement(start.build());
        while (context.getPathGenerator().hasNextStep()) {
            context.getPathGenerator().getNextStep();
            System.out.println(context.getCurrentElement());
        }
    }

    @Test(expected = NoPathFoundException.class)
    public void failTest() {
        CombinedPath pathGenerator = new CombinedPath();
        pathGenerator.addPathGenerator(new RandomPath(new ReachedVertex("v1")));
        pathGenerator.addPathGenerator(new RandomPath(new ReachedVertex("v2")));
        Context context = new TestExecutionContext(model, pathGenerator);
        context.setCurrentElement(start.build());
        while (context.getPathGenerator().hasNextStep()) {
            context.getPathGenerator().getNextStep();
            System.out.println(context.getCurrentElement());
        }
        context.getPathGenerator().getNextStep();
    }

    @Test
    public void toStringTest() {
        CombinedPath pathGenerator = new CombinedPath();
        Assert.assertEquals(pathGenerator.toString(), "");
        pathGenerator.addPathGenerator(new RandomPath(new ReachedVertex("v1")));
        Assert.assertEquals("RandomPath(ReachedVertex(v1))", pathGenerator.toString());
        pathGenerator.addPathGenerator(new RandomPath(new ReachedVertex("v2")));
        Assert.assertEquals("RandomPath(ReachedVertex(v1)) AND RandomPath(ReachedVertex(v2))", pathGenerator.toString());
    }
}
