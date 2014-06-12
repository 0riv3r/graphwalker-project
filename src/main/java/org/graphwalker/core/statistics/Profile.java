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

import org.graphwalker.core.model.Element;
import org.graphwalker.core.model.Path;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author Nils Olsson
 */
public final class Profile extends HashMap<Element, ProfileUnit> {

    private final Path<Element> path = new Path<>();

    public void addExecution(Element element, Execution execution) {
        path.push(element);
        if (!containsKey(element)) {
            put(element, new ProfileUnit(execution));
        } else {
            get(element).addExecution(execution);
        }
    }

    public Path<Element> getPath() {
        return path;
    }

    public long getTotalExecutionCount() {
        return getTotalExecutionCount(Element.class);
    }

    public long getTotalExecutionCount(Class<? extends Element> type) {
        long count = 0;
        for (Element element: keySet()) {
            if (type.isAssignableFrom(element.getClass())) {
                count += get(element).getExecutionCount();
            }
        }
        return count;
    }

    public long getTotalExecutionTime() {
        return getTotalExecutionTime(TimeUnit.NANOSECONDS);
    }

    public long getTotalExecutionTime(TimeUnit unit) {
        return getTotalExecutionTime(Element.class, unit);
    }

    public long getTotalExecutionTime(Class<?> type, TimeUnit unit) {
        long executionTime = 0;
        for (Element element: keySet()) {
            if (type.isAssignableFrom(element.getClass())) {
                executionTime += get(element).getTotalExecutionTime(unit);
            }
        }
        return TimeUnit.NANOSECONDS.convert(executionTime, unit);
    }

    public long getFirstExecutionTimestamp() {
        return getFirstExecutionTimestamp(Element.class);
    }

    public long getFirstExecutionTimestamp(Class<? extends Element> type) {
        return getFirstExecutionTimestamp(type, TimeUnit.NANOSECONDS);
    }

    public long getFirstExecutionTimestamp(Class<? extends Element> type, TimeUnit unit) {
        long timestamp = Long.MAX_VALUE;
        for (Element element: keySet()) {
            if (type.isAssignableFrom(element.getClass())) {
                long firstExecutionTimestamp = get(element).getFirstExecutionTimestamp(unit);
                if (timestamp > firstExecutionTimestamp) {
                    timestamp = firstExecutionTimestamp;
                }
            }
        }
        return timestamp;
    }

    public long getLastExecutionTimestamp() {
        return getLastExecutionTimestamp(Element.class);
    }

    public long getLastExecutionTimestamp(Class<? extends Element> type) {
        return getLastExecutionTimestamp(type, TimeUnit.NANOSECONDS);
    }

    public long getLastExecutionTimestamp(Class<? extends Element> type, TimeUnit unit) {
        long timestamp = Long.MIN_VALUE;
        for (Element element: keySet()) {
            if (type.isAssignableFrom(element.getClass())) {
                long lastExecutionTimestamp = get(element).getLastExecutionTimestamp(unit);
                if (timestamp < lastExecutionTimestamp) {
                    timestamp = lastExecutionTimestamp;
                }
            }
        }
        return timestamp;
    }
}
