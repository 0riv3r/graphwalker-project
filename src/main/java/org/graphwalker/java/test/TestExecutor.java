package org.graphwalker.java.test;

/*
 * #%L
 * GraphWalker Java
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

import org.graphwalker.core.condition.ReachedStopCondition;
import org.graphwalker.core.condition.StopCondition;
import org.graphwalker.core.event.Observer;
import org.graphwalker.core.generator.PathGenerator;
import org.graphwalker.core.machine.Context;
import org.graphwalker.core.machine.Machine;
import org.graphwalker.core.machine.MachineException;
import org.graphwalker.core.machine.SimpleMachine;
import org.graphwalker.core.model.*;
import org.graphwalker.dsl.antlr.generator.GeneratorFactory;
import org.graphwalker.io.factory.ContextFactoryScanner;
import org.graphwalker.java.annotation.*;
import org.graphwalker.java.annotation.Model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.graphwalker.core.model.Model.RuntimeModel;

/**
 * @author Nils Olsson
 */
public final class TestExecutor implements Executor {

    private final Configuration configuration;
    private final Set<Machine> machines = new HashSet<>();
    private final Map<Context, MachineException> failures = new HashMap<>();

    public TestExecutor(Configuration configuration) {
        this.configuration = configuration;
        List<Context> contexts = createContexts(AnnotationUtils.findTests());
        if (!contexts.isEmpty()) {
            this.machines.add(createMachine(contexts));
        }
    }

    public TestExecutor(Context... contexts) {
        this.configuration = new Configuration();
        configureContexts(contexts);
        if (0 < contexts.length) {
            this.machines.add(createMachine(contexts));
        }
    }

    private Machine createMachine(Context... contexts) {
        return createMachine(Arrays.asList(contexts));
    }

    @SuppressWarnings("unchecked")
    private Machine createMachine(List<Context> contexts) {
        Machine machine = new SimpleMachine(contexts);
        for (Context context: contexts) {
            if (context instanceof Observer) {
                machine.addObserver((Observer)context);
            }
        }
        return machine;
    }

    public Set<Machine> getMachines() {
        return machines;
    }

    private List<Context> createContexts(Set<Class<? extends Context>> testClasses) {
        List<Context> contexts = new ArrayList<>();
        for (Class<? extends Context> testClass: testClasses) {
            GraphWalker annotation = testClass.getAnnotation(GraphWalker.class);
            if (isTestIncluded(annotation, testClass.getName())) {
                Context context = createContext(testClass);
                configureContext(context, annotation);
                contexts.add(context);
            }
        }
        return contexts;
    }

    private void configureContexts(Context... contexts) {
        for (Context context: contexts) {
            configureContext(context);
        }
    }

    private void configureContext(final Context context) {
        GraphWalker annotation = context.getClass().getAnnotation(GraphWalker.class);
        if (null != annotation) {
            configureContext(context, annotation);
        }
    }

    private void configureContext(final Context context, GraphWalker annotation) {
        if (!"".equals(annotation.value())) {
            context.setPathGenerator(GeneratorFactory.parse(annotation.value()));
        } else {
            context.setPathGenerator(createPathGenerator(annotation));
        }
        if (!"".equals(annotation.start())) {
            context.setNextElement(getElement(context.getModel(), annotation.start()));
        }
        // TODO: support classes with multiple models
        Set<Model> models = AnnotationUtils.getAnnotations(context.getClass(), Model.class);
        if (!models.isEmpty()) {
            Path path = Paths.get(models.iterator().next().file());
            ContextFactoryScanner.get(path).create(path, context);
        }
    }

    private boolean isTestIncluded(GraphWalker annotation, String name) {
        boolean belongsToGroup = false;
        for (String group: annotation.groups()) {
            for (String definedGroups: configuration.getGroups()) {
                if (SelectorUtils.match(definedGroups, group)) {
                    belongsToGroup = true;
                    break;
                }
            }
        }
        if (belongsToGroup) {
            for (String exclude : configuration.getExcludes()) {
                if (SelectorUtils.match(exclude, name)) {
                    return false;
                }
            }
            for (String include : configuration.getIncludes()) {
                if (SelectorUtils.match(include, name)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Context createContext(Class<? extends Context> testClass) {
        try {
            return testClass.newInstance();
        } catch (Throwable e) {
            throw new TestExecutionException("Failed to create context");
        }
    }

    private PathGenerator createPathGenerator(GraphWalker annotation) {
        try {
            Constructor constructor = null;
            try {
                constructor = annotation.pathGenerator().getConstructor(StopCondition.class);
            } catch (Throwable t) {
                constructor = annotation.pathGenerator().getConstructor(ReachedStopCondition.class);
            }
            if (null == constructor) {
                throw new TestExecutionException("Couldn't find a valid constructor");
            }
            return (PathGenerator)constructor.newInstance(createStopCondition(annotation));
        } catch (Throwable e) {
            throw new TestExecutionException(e);
        }
    }

    private StopCondition createStopCondition(GraphWalker annotation) {
        String value = annotation.stopConditionValue();
        Class<? extends StopCondition> stopCondition = annotation.stopCondition();
        if (value.isEmpty()) {
            try {
                return stopCondition.newInstance();
            } catch (Throwable e) {
                // ignore
            }
        }
        try {
            return stopCondition.getConstructor(new Class[]{String.class}).newInstance(value);
        } catch (Throwable e) {
            // ignore
        }
        try {
            return stopCondition.getConstructor(new Class[]{Long.TYPE}).newInstance(Long.parseLong(value));
        } catch (Throwable e) {
            // ignore
        }
        try {
            return stopCondition.getConstructor(new Class[]{Integer.TYPE}).newInstance(Integer.parseInt(value));
        } catch (Throwable e) {
            // ignore
        }
        try {
            return stopCondition.getConstructor(new Class[]{Double.TYPE}).newInstance(Double.parseDouble(value));
        } catch (Throwable e) {
            // ignore
        }
        try {
            return stopCondition.getConstructor(new Class[]{Float.TYPE}).newInstance(Float.parseFloat(value));
        } catch (Throwable e) {
            // ignore
        }
        throw new TestExecutionException();
    }

    private Element getElement(RuntimeModel model, String name) {
        List<Element> elements = model.findElements(name);
        if (null == elements || 0 == elements.size()) {
            throw new TestExecutionException("Start element not found");
        }
        if (1 < elements.size()) {
            throw new TestExecutionException("Ambiguous start element defined");
        }
        return elements.get(0);
    }

    public Executor execute() {
        if (!machines.isEmpty()) {
            executeAnnotation(BeforeExecution.class, machines);
            ExecutorService executorService = Executors.newFixedThreadPool(machines.size());
            for (final Machine machine : machines) {
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Context context = null;
                            while (machine.hasNextStep()) {
                                if (null != context) {
                                    executeAnnotation(BeforeElement.class, context);
                                }
                                context = machine.getNextStep();
                                executeAnnotation(AfterElement.class, context);
                            }
                        } catch (MachineException e) {
                            failures.put(e.getContext(), e);
                        }
                    }
                });
            }
            executorService.shutdown();
            try {
                executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
            } catch (InterruptedException e) {
                // ignore
            }
            executeAnnotation(AfterExecution.class, machines);
        }
        return this;
    }

    private void executeAnnotation(Class<? extends Annotation> annotation, Set<Machine> machines) {
        for (Machine machine: machines) {
            executeAnnotation(annotation, machine);
        }
    }

    private void executeAnnotation(Class<? extends Annotation> annotation, Machine machine) {
        for (Context context: machine.getContexts()) {
            executeAnnotation(annotation, context);
        }
    }

    private void executeAnnotation(Class<? extends Annotation> annotation, Context context) {
        AnnotationUtils.execute(annotation, context);
    }

    public boolean isFailure(Context context) {
        return failures.containsKey(context);
    }

    public MachineException getFailure(Context context) {
        return failures.get(context);
    }

    public Collection<MachineException> getFailures() {
        return failures.values();
    }
}
