package org.graphwalker.core.condition;

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

import org.graphwalker.core.generator.RandomPath;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.machine.Machine;
import org.graphwalker.core.machine.SimpleMachine;
import org.graphwalker.core.machine.TestExecutionContext;
import org.graphwalker.core.model.Edge;
import org.graphwalker.core.model.Model;
import org.graphwalker.core.model.Vertex;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.core.Is.is;

/**
 * @author Nils Olsson
 */
public class TimeDurationTest {

    @Test
    public void testConstructor() {
        TimeDuration timeDuration = new TimeDuration(100, TimeUnit.SECONDS);
        Assert.assertThat(timeDuration.getDuration(), is(100l));
    }

    @Test
    public void testFulfilment() {
        Vertex vertex = new Vertex();
        Model model = new Model().addEdge(new Edge().setSourceVertex(vertex).setTargetVertex(vertex));
        long start = System.nanoTime();
        Context context = new TestExecutionContext(model, new RandomPath(new TimeDuration(1000L, TimeUnit.MILLISECONDS)));
        context.setNextElement(vertex);
        Machine machine = new SimpleMachine(context);
        while (machine.hasNextStep()) {
            machine.getNextStep();
        }
        long stop = System.nanoTime();
        Assert.assertEquals(1000, (stop - start) / 1000000, 200);
    }

    @Test
    public void testIsFulfilled() {
        Vertex vertex = new Vertex();
        Model model = new Model().addEdge(new Edge().setSourceVertex(vertex).setTargetVertex(vertex));
        Context context = new TestExecutionContext(model, new RandomPath(new TimeDuration(1000L, TimeUnit.MILLISECONDS)));
        Assert.assertFalse(context.getPathGenerator().getStopCondition().isFulfilled());
        context.setNextElement(vertex);
        Machine machine = new SimpleMachine(context);
        while (machine.hasNextStep()) {
            machine.getNextStep();
        }
        Assert.assertTrue(context.getPathGenerator().getStopCondition().isFulfilled());
    }
}
