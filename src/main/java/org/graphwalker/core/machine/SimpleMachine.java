package org.graphwalker.core.machine;

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

import org.graphwalker.core.event.EventType;
import org.graphwalker.core.generator.NoPathFoundException;
import org.graphwalker.core.model.Action;
import org.graphwalker.core.model.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.*;

import static org.graphwalker.core.model.Edge.RuntimeEdge;
import static org.graphwalker.core.model.Vertex.RuntimeVertex;

/**
 * @author Nils Olsson
 */
public final class SimpleMachine extends ObservableMachine {

    private static final Logger logger = LoggerFactory.getLogger(SimpleMachine.class);

    private final List<Context> contexts = new ArrayList<>();

    private Context currentContext;
    private ExceptionStrategy exceptionStrategy = new FailFastStrategy();

    public SimpleMachine(Context context) {
        this(Arrays.asList(context));
    }

    public SimpleMachine(List<Context> contexts) {
        this.contexts.addAll(contexts);
        for (Context context: contexts) {
            this.currentContext = context;
            execute(context.getModel().getActions());
        }
      this.currentContext = contexts.get(0);
    }

    public void setExceptionStrategy(ExceptionStrategy exceptionStrategy) {
        this.exceptionStrategy = exceptionStrategy;
    }

    @Override
    public Context getNextStep() {
        MDC.put("trace", UUID.randomUUID().toString());
        walk(currentContext);
        notifyObservers(currentContext.getCurrentElement(), EventType.BEFORE_ELEMENT);
        currentContext.getProfiler().start();
        execute(currentContext.getCurrentElement());
        currentContext.getProfiler().stop();
        notifyObservers(currentContext.getCurrentElement(), EventType.AFTER_ELEMENT);
        return currentContext;
    }

    private void walk(Context context) {
        if (null == context.getCurrentElement()) {
            if (null != context.getNextElement()) {
                context.setCurrentElement(context.getNextElement());
            } else if (context.getModel().hasSharedStates()) {
                // if we don't have a start vertex, but we have shared state, then we try to find another context to execute
                for (Context newContext: contexts) {
                    if (hasNextStep(newContext) && null != newContext.getCurrentElement() || null != newContext.getNextElement()) {
                        currentContext = newContext;
                        getNextStep();
                    }
                }
            } else {
                exceptionStrategy.handle(this, new MachineException(currentContext, new NoPathFoundException("No start element defined")));
            }
        } else {
            try {
                if (isVertex(currentContext.getCurrentElement())) {
                    RuntimeVertex vertex = (RuntimeVertex) currentContext.getCurrentElement();
                    if (vertex.hasSharedState() && hasPossibleSharedStates(vertex)) {
                        List<SharedStateTupel> candidates = getPossibleSharedStates(vertex.getSharedState());
                        // TODO: If we need other way of determine the next state, we should have some interface for this
                        Random random = new Random(System.nanoTime());
                        SharedStateTupel candidate = candidates.get(random.nextInt(candidates.size()));
                        if (!candidate.getVertex().equals(currentContext.getCurrentElement())) {
                            candidate.context.setCurrentElement(candidate.getVertex());
                            currentContext = candidate.context;
                        } else {
                            context.getPathGenerator().getNextStep(context);
                        }
                    } else {
                        context.getPathGenerator().getNextStep(context);
                    }
                } else {
                    context.getPathGenerator().getNextStep(context);
                }
            } catch (Throwable t) {
                exceptionStrategy.handle(this, new MachineException(currentContext, t));
            }
        }
        if (ExecutionStatus.NOT_EXECUTED.equals(context.getExecutionStatus())) {
            context.setExecutionStatus(ExecutionStatus.EXECUTING);
        }
    }

    private boolean isVertex(Element element) {
        return element instanceof RuntimeVertex;
    }

    private boolean hasPossibleSharedStates(RuntimeVertex vertex) {
        return null != vertex.getSharedState() && 0 < getPossibleSharedStates(vertex.getSharedState()).size();
    }

    private List<SharedStateTupel> getPossibleSharedStates(String sharedState) {
        List<SharedStateTupel> sharedStates = new ArrayList<>();
        for (Context context: contexts) {
            if (context.getModel().hasSharedState(sharedState)) {
                for (RuntimeVertex vertex : context.getModel().getSharedStates(sharedState)) {
                    //if (context.getPathGenerator().hasNextStep(context)) {
                        if (!context.getModel().getOutEdges(vertex).isEmpty()) {
                            sharedStates.add(new SharedStateTupel(context, vertex));
                        }
                    //}
                }
            }
        }
        return sharedStates;
    }

    @Override
    public boolean hasNextStep() {
        MDC.put("trace", UUID.randomUUID().toString());
        for (Context context: contexts) {
            if (hasNextStep(context)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasNextStep(Context context) {
        ExecutionStatus status = context.getExecutionStatus();
        if (ExecutionStatus.COMPLETED.equals(status) || ExecutionStatus.FAILED.equals(status)) {
            return false;
        }
        boolean hasMoreSteps = context.getPathGenerator().hasNextStep(context);
        if (!hasMoreSteps) {
            context.setExecutionStatus(ExecutionStatus.COMPLETED);
        }
        return hasMoreSteps;
    }

    private void execute(Element element) {
        try {
            if (element instanceof RuntimeVertex) {
                execute((RuntimeVertex) element);
            } else if (element instanceof RuntimeEdge) {
                execute((RuntimeEdge) element);
            }
        } catch (MachineException e) {
            exceptionStrategy.handle(this, e);
        }
    }

    private void execute(RuntimeEdge edge) {
        execute(edge.getActions());
        if (edge.hasName()) {
            currentContext.execute(edge.getName());
        }
    }

    private void execute(List<Action> actions) {
        for (Action action: actions) {
            currentContext.execute(action);
        }
    }

    private void execute(RuntimeVertex vertex) {
        if (vertex.hasName()) {
             currentContext.execute(vertex.getName());
        }
    }

    public Context getCurrentContext() {
        return currentContext;
    }

    private static class SharedStateTupel {

        private final Context context;
        private final RuntimeVertex vertex;

        private SharedStateTupel(Context context, RuntimeVertex vertex) {
            this.context = context;
            this.vertex = vertex;
        }

        public Context getContext() {
            return context;
        }

        public RuntimeVertex getVertex() {
            return vertex;
        }
    }

    public List<Context> getContexts() {
        return Collections.unmodifiableList(contexts);
    }
}
