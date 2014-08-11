package org.graphwalker.core.statistics;

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

import org.graphwalker.core.machine.ExecutionContext;
import org.graphwalker.core.model.Element;
import org.graphwalker.core.model.Path;
import org.graphwalker.core.model.Vertex;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Nils Olsson
 */
public final class Profiler {

    private final ExecutionContext context;
    private final Profile profile = new Profile();
    private long timestamp = 0;

    public Profiler(ExecutionContext context) {
        this.context = context;
    }

    public void start() {
        timestamp = System.nanoTime();
    }

    public void stop() {
        Element element = context.getCurrentElement();
        if (null != element) {
            profile.addExecution(element, new Execution(timestamp, System.nanoTime() - timestamp));
        }
    }

    public boolean isVisited(Element element) {
        return profile.containsKey(element);
    }

    public long getTotalVisitCount() {
        return profile.getTotalExecutionCount();
    }

    public List<Element> getUnvisitedElements() {
        List<Element> elementList = new ArrayList<>();
        for (Element e : context.getModel().getElements()) {
            if (!context.getModel().getStartVertices().contains(e) && !isVisited(e)) {
                elementList.add(e);
            }
        }
        return elementList;
    }

    public Path<Element> getPath() {
        return profile.getPath();
    }

    @Deprecated
    public void removeStartVertex(Element element) {
        profile.addExecution(element, new Execution(0, 0));
    }
}
