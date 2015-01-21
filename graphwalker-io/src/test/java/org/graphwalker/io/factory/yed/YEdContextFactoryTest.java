package org.graphwalker.io.factory.yed;

/*
 * #%L
 * GraphWalker Input/Output
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

import org.graphwalker.core.condition.VertexCoverage;
import org.graphwalker.core.generator.RandomPath;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.machine.DryRunContext;
import org.graphwalker.core.machine.Machine;
import org.graphwalker.core.machine.SimpleMachine;
import org.graphwalker.io.factory.ContextFactory;
import org.graphwalker.io.factory.ContextFactoryException;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Paths;

import static org.graphwalker.core.model.Edge.RuntimeEdge;
import static org.hamcrest.core.Is.is;

/**
 * @author Nils Olsson
 */
public class YEdContextFactoryTest {

    @Test(expected = ContextFactoryException.class)
    public void fileDoesNotExistsOnFileSystem() {
        Context context = new YEdContextFactory().create(Paths.get("graphml/LKHDIODSOSUBD.graphml"));
    }

    @Test
    public void shared() {
        Context sharedAContext = new YEdContextFactory().create(Paths.get("graphml/SharedA.graphml"));
        Assert.assertNotNull(sharedAContext);
        Assert.assertThat(sharedAContext.getModel().getVertices().size(), is(2));
        Assert.assertThat(sharedAContext.getModel().getEdges().size(), is(6));

        Context sharedBContext = new YEdContextFactory().create(Paths.get("graphml/SharedB.graphml"));
        Assert.assertNotNull(sharedBContext);
        Assert.assertThat(sharedBContext.getModel().getVertices().size(), is(2));
        Assert.assertThat(sharedBContext.getModel().getEdges().size(), is(1));
    }

    @Test
    public void login() {
        new YEdContextFactory().create(Paths.get("graphml/Login.graphml"));
    }

    @Test
    public void uc01() {
        Context context = new YEdContextFactory().create(Paths.get("graphml/UC01.graphml"));

        // Since the model id the Model.RuntimeModel,the Start vertex is removed from the graph.
        Assert.assertThat(context.getModel().getVertices().size(), is(7)); // one of the vertices is the start vertex and that shouldn't be a part of the model
        Assert.assertThat(context.getModel().getEdges().size(), is(12));
        Assert.assertThat(context.getModel().findEdges("e_init").size(), is(1));
        Assert.assertThat(context.getModel().findEdges("e_AddBookToCart").size(), is(1));
        Assert.assertThat(context.getModel().findEdges("e_ClickBook").size(), is(1));
        Assert.assertThat(context.getModel().findEdges("e_EnterBaseURL").size(), is(1));
        Assert.assertThat(context.getModel().findEdges("e_SearchBook").size(), is(4));
        Assert.assertThat(context.getModel().findEdges("e_ShoppingCart").size(), is(3));
        Assert.assertThat(context.getModel().findEdges("e_StartBrowser").size(), is(1));
        Assert.assertNull(context.getModel().findEdges(""));

        Assert.assertThat(context.getModel().findVertices("v_BaseURL").size(), is(1));
        Assert.assertThat(context.getModel().findVertices("v_BookInformation").size(), is(1));
        Assert.assertThat(context.getModel().findVertices("v_BrowserStarted").size(), is(1));
        Assert.assertThat(context.getModel().findVertices("v_BrowserStopped").size(), is(1));
        Assert.assertThat(context.getModel().findVertices("v_OtherBoughtBooks").size(), is(1));
        Assert.assertThat(context.getModel().findVertices("v_SearchResult").size(), is(1));
        Assert.assertThat(context.getModel().findVertices("v_ShoppingCart").size(), is(1));

        Assert.assertThat(context.getModel().findEdges("e_init").get(0).getTargetVertex().getName(), is("v_BrowserStopped"));

        Assert.assertThat(context.getModel().findEdges("e_StartBrowser").get(0).getSourceVertex().getName(), is("v_BrowserStopped"));
        Assert.assertThat(context.getModel().findEdges("e_StartBrowser").get(0).getTargetVertex().getName(), is("v_BrowserStarted"));
    }

    @Test
    public void efsmWithReqtags1() {
        Context context = new YEdContextFactory().create(Paths.get("graphml/reqtags/ShoppingCart.graphml"));
        Assert.assertThat("Number of vertices", context.getModel().getVertices().size(), is(6));
        Assert.assertThat("Number of edges", context.getModel().getEdges().size(), is(11));
        // TODO Fix req
        //Assert.assertThat("Number of requirements", context.getRequirements().size(), is(5));
    }

    @Test
    public void efsmWithReqtags2() {
        Context context = new YEdContextFactory().create(Paths.get("graphml/reqtags/EFSM_with_REQTAGS.graphml"));
        Assert.assertThat("Number of vertices", context.getModel().getVertices().size(), is(7));
        Assert.assertThat("Number of edges", context.getModel().getEdges().size(), is(19));
        // TODO Fix req
        //Assert.assertThat("Number of requirements", context.getRequirements().size(), is(6));
    }

    @Test
    public void guards() {
        Context context = new YEdContextFactory().create(Paths.get("graphml/Guards.graphml"));
        Assert.assertThat("Number of vertices", context.getModel().getVertices().size(), is(2));
        Assert.assertThat("Number of edges", context.getModel().getEdges().size(), is(2));
    }

    @Test
    public void singleEdge() {
        Context context = new YEdContextFactory().create(Paths.get("graphml/blocked/singleEdge.graphml"));
        Assert.assertThat(context.getModel().getVertices().size(), is(2));
        Assert.assertThat(context.getModel().getEdges().size(), is(2));
    }

    @Test
    public void singleVertex() {
        Context context = new YEdContextFactory().create(Paths.get("graphml/blocked/singleVertex.graphml"));
        Assert.assertThat(context.getModel().getVertices().size(), is(1));
        Assert.assertThat(context.getModel().getEdges().size(), is(1));
    }

    @Test
    public void singleVertex2() {
        Context context = new YEdContextFactory().create(Paths.get("graphml/blocked/singleVertex2.graphml"));
        Assert.assertThat(context.getModel().getVertices().size(), is(1));
        Assert.assertThat(context.getModel().getEdges().size(), is(1));
    }

    @Test
    public void readInit() {
        Context context = new YEdContextFactory().create(Paths.get("graphml/init/init.graphml"));
        Assert.assertThat(context.getModel().getActions().size(), is(2));
    }

    @Test
    public void readLoginAndCrashModels() {
        ContextFactory factory = new YEdContextFactory();
        Context login = factory.create(Paths.get("graphml/shared_state/Login.graphml"));
        Context crash = factory.create(Paths.get("graphml/shared_state/Crash.graphml"));
        for (RuntimeEdge edge : crash.getModel().getEdges()) {
            Assert.assertNotNull(edge.getSourceVertex());
            Assert.assertNotNull(edge.getTargetVertex());
        }
    }

    @Test(expected = ContextFactoryException.class)
    public void blockedBranch1() {
        new YEdContextFactory().create(Paths.get("graphml/blocked/blockedBranch1.graphml"));
    }

    @Test
    public void blockedBranch2() {
        Context context = new YEdContextFactory().create(Paths.get("graphml/blocked/blockedBranch2.graphml"));
        Assert.assertThat(context.getModel().getVertices().size(), is(3));
        Assert.assertThat(context.getModel().getEdges().size(), is(4));
    }

    @Test
    public void blockedVertex1() {
        Context context = new YEdContextFactory().create(Paths.get("graphml/blocked/blockedVertex1.graphml"));
        Assert.assertThat(context.getModel().getVertices().size(), is(2));
        Assert.assertThat(context.getModel().getEdges().size(), is(2));
    }

    @Test(expected = ContextFactoryException.class)
    public void blockedVertex2() {
        new YEdContextFactory().create(Paths.get("graphml/blocked/blockedVertex2.graphml"));
    }

    @Test(expected = ContextFactoryException.class)
    public void blockedVertex3() {
        new YEdContextFactory().create(Paths.get("graphml/blocked/blockedVertex3.graphml"));
    }

    @Test
    public void dryRun() {
        Context context = new YEdContextFactory().create(Paths.get("graphml/UC01.graphml"));
        context.setPathGenerator(new RandomPath(new VertexCoverage(100)));
        Machine machine = new SimpleMachine(context);
        while (machine.hasNextStep()) {
            machine.getNextStep();
        }
    }

    @Test
    public void dryRunContext() {
        Context context = new YEdContextFactory().create(Paths.get("graphml/UC01.graphml"));
        context.setPathGenerator(new RandomPath(new VertexCoverage(100)));
        Machine machine = new SimpleMachine(new DryRunContext(context));
        while (machine.hasNextStep()) {
            machine.getNextStep();
        }
    }
}
