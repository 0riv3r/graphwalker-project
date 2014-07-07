/*
 * #%L
 * GraphWalker Command Line Interface
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
package org.graphwalker.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.MissingCommandException;
import com.beust.jcommander.ParameterException;
import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.grizzly.http.server.HttpServer;
import org.graphwalker.cli.antlr.GeneratorFactory;
import org.graphwalker.cli.antlr.GeneratorFactoryException;
import org.graphwalker.cli.commands.Methods;
import org.graphwalker.cli.commands.Offline;
import org.graphwalker.cli.commands.Online;
import org.graphwalker.cli.commands.Requirements;
import org.graphwalker.cli.service.Restful;
import org.graphwalker.core.generator.PathGenerator;
import org.graphwalker.core.machine.ExecutionContext;
import org.graphwalker.core.machine.Machine;
import org.graphwalker.core.machine.MachineException;
import org.graphwalker.core.machine.SimpleMachine;
import org.graphwalker.core.model.*;
import org.graphwalker.io.factory.GraphMLModelFactory;
import org.graphwalker.io.factory.ModelFactoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class CLI {
  private static final Logger logger = LoggerFactory.getLogger(CLI.class);
  JCommander jc;
  Options options;
  Offline offline;
  Online online;
  Methods methods;
  Requirements requirements;

  public static void main(String[] args) {
    CLI cli = new CLI();
    try {
      cli.run(args);
    } catch (Exception e) {
      // We should have caught all exceptions up until here, but there
      // might have been problems with the command parser for instance...
      System.err.println(e);
      logger.error("An error occurred when running command: " + StringUtils.join(args, " "), e);
    }
  }

  /**
   * Parses the command line.
   *
   * @param args
   */
  private void run(String[] args) {
    options = new Options();
    jc = new JCommander(options);
    jc.setProgramName("java -jar graphwalker.jar");

    offline = new Offline();
    jc.addCommand("offline", offline);

    online = new Online();
    jc.addCommand("online", online);

    methods = new Methods();
    jc.addCommand("methods", methods);

    requirements = new Requirements();
    jc.addCommand("requirements", requirements);

    try {
      jc.parse(args);

      // Parse for commands
      if (jc.getParsedCommand() != null) {
        if (jc.getParsedCommand().equalsIgnoreCase("offline")) {
          RunCommandOffline();
        } else if (jc.getParsedCommand().equalsIgnoreCase("online")) {
          RunCommandOnline();
        } else if (jc.getParsedCommand().equalsIgnoreCase("methods")) {
          RunCommandMethods();
        } else if (jc.getParsedCommand().equalsIgnoreCase("requirements")) {
          RunCommandRequirements();
        }
      }

      // Parse for arguments
      else if (options.version) {
        System.out.println(printVersionInformation());
      }

      // No commands or options were found
      else {
        jc.usage();
      }

    } catch (MissingCommandException e) {
      System.err.println("I did not see a valid command.");
      System.err.println("");
      jc.usage();
    } catch (ParameterException e) {
      System.err.println("An error occurred when running command: " + StringUtils.join(args, " "));
      System.err.println(e.getMessage());
      System.err.println("");
      jc.usage();
    } catch (ModelFactoryException e) {
      System.err.println("An error occurred when running command: " + StringUtils.join(args, " "));
      System.err.println(e.getMessage());
      System.err.println("");
      jc.usage();
    } catch (GeneratorFactoryException e) {
      System.err.println("An error occurred when running command: " + StringUtils.join(args, " "));
      System.err.println(e.getMessage());
      System.err.println("");
      jc.usage();
    } catch (Exception e) {
      System.err.println("An error occurred when running command: " + StringUtils.join(args, " "));
      System.err.println(e.getMessage());
      logger.error("An error occurred when running command: " + StringUtils.join(args, " "), e);
    }
  }

  private void RunCommandRequirements()  throws Exception {
    GraphMLModelFactory factory = new GraphMLModelFactory();

    Model.RuntimeModel model = null;
    String modelFileName = requirements.model;
    try {
      model = factory.create(modelFileName).build();
    } catch (ModelFactoryException e) {
      throw new ModelFactoryException("Could not parse the model: '" + modelFileName + "'. Does it exists and is it readable?");
    }

    SortedSet<Requirement> reqs = new TreeSet<>();
    for (Vertex.RuntimeVertex vertex : model.getVertices()) {
      for (Requirement req : vertex.getRequirements()) {
        reqs.add(req);
      }
    }

    for (Requirement req : reqs) {
      System.out.println(req.getKey());
    }
  }

  private void RunCommandMethods()  throws Exception {
    GraphMLModelFactory factory = new GraphMLModelFactory();

    Model.RuntimeModel model = null;
    String modelFileName = methods.model;
    try {
      model = factory.create(modelFileName).build();
    } catch (ModelFactoryException e) {
      throw new ModelFactoryException("Could not parse the model: '" + modelFileName + "'. Does it exists and is it readable?");
    }

    SortedSet<String> names = new TreeSet<>();
    for (Vertex.RuntimeVertex vertex : model.getVertices()) {
      names.add(vertex.getName());
    }
    for (Edge.RuntimeEdge edge : model.getEdges()) {
      names.add(edge.getName());
    }

    for (String name : names) {
      System.out.println(name);
    }
  }

  private void RunCommandOnline()  throws Exception {
    GraphMLModelFactory factory = new GraphMLModelFactory();
    ArrayList<ExecutionContext> executionContexts = new ArrayList<>();
    Iterator itr = online.model.iterator();
    while(itr.hasNext()) {
      Model model = null;
      String modelFileName = (String) itr.next();
      try {
        model = factory.create(modelFileName);
      } catch (ModelFactoryException e) {
        throw new ModelFactoryException("Could not parse the model: '" + modelFileName + "'. Does it exists and is it readable?");
      }

      PathGenerator pathGenerator = GeneratorFactory.parse((String) itr.next());
      ExecutionContext context = new ExecutionContext(model, pathGenerator);
      SetStartVertex(context);
      executionContexts.add(context);
    }

    if (online.restful) {
      // Todo: We cannot iterate of multiple models and
      // instantiating the HttpServer every time.
      // The creation of HttpServer needs to be done outside the while-loop
      ResourceConfig rc = new DefaultResourceConfig();
      rc.getSingletons().add(new Restful(new SimpleMachine(executionContexts)));
      HttpServer server = GrizzlyServerFactory.createHttpServer("http://0.0.0.0:9999", rc);
      System.out.println("Press Control+C to end...");
      try {
        server.start();
        Thread.currentThread().join();
      } catch (Exception e) {
        logger.error("An error occurred when running command online: ", e);
      } finally {
        server.stop();
      }
    }
  }

  private void RunCommandOffline()  throws Exception {
    GraphMLModelFactory factory = new GraphMLModelFactory();
    ArrayList<ExecutionContext> executionContexts = new ArrayList<>();
    Iterator itr = offline.model.iterator();
    while(itr.hasNext()) {
      Model model = null;
      String modelFileName = (String) itr.next();
      try {
        model = factory.create(modelFileName);
        model.setName(modelFileName);
      } catch (ModelFactoryException e) {
        throw new ModelFactoryException("Could not parse the model: '" + modelFileName + "'. Does it exists and is it readable?\n"+
        e.getMessage());
      }

      PathGenerator pathGenerator = GeneratorFactory.parse((String) itr.next());
      ExecutionContext context = new ExecutionContext(model, pathGenerator);
      SetStartVertex(context);
      executionContexts.add(context);
    }

    SimpleMachine machine = new SimpleMachine(executionContexts);
    while (machine.hasNextStep()) {
      try {
        machine.getNextStep();
      } catch ( MachineException e) {
        ;
      } finally {
        if ( offline.unvisited ) {
          if (offline.verbose) {
            String basename = FilenameUtils.getBaseName(machine.getCurrentContext().getModel().getName());
            System.out.print(basename + " " + machine.getCurrentContext().getProfiler().getUnvisitedElements().size() +
                "(" + machine.getCurrentContext().getModel().getElements().size() + ") : ");
          }
          for( Element e: machine.getCurrentContext().getProfiler().getUnvisitedElements() ) {
            System.out.print(e.getName());
            if (offline.verbose) {
                System.out.print("(" + e.getId() + ")");
            }
            System.out.print(" ");
          }
          System.out.println();
        } else if (machine.getCurrentContext().getCurrentElement().hasName()) {
          System.out.print(machine.getCurrentContext().getCurrentElement().getName());
          if (offline.verbose) {
            System.out.print("(" + machine.getCurrentContext().getCurrentElement().getId() + ")");
          }
          System.out.println();
        }
      }
    }
  }

  private void SetStartVertex( ExecutionContext context ) {
    // Backward compatibility with GW2, which expects a Start node to exists
    List<Vertex.RuntimeVertex> list = context.getModel().findVertices("Start");
    if (null != list && !list.isEmpty() && list.get(0) != null) {
      context.setCurrentElement(context.getModel().findVertices("Start").get(0));
      context.getProfiler().removeStartVertex(context.getCurrentElement());

    // New GW3 syntax for graphml models, which defines the Start as the vertex with no in-edges
    } else {
      for (Vertex.RuntimeVertex vertex : context.getModel().getVertices() ) {
        if (context.getModel().getInEdges(vertex).size()==0){
          context.setNextElement(vertex);
          return;
        }
      }
    }
  }

  private String printVersionInformation() {
    String version = "org.graphwalker version: " + getVersionString() + System.getProperty("line.separator");
    version += System.getProperty("line.separator");

    version += "org.graphwalker is open source software licensed under MIT license" + System.getProperty("line.separator");
    version += "The software (and it's source) can be downloaded from http://graphwalker.org" + System.getProperty("line.separator");
    version += "For a complete list of this package software dependencies, see TO BE DEFINED" + System.getProperty("line.separator");

    return version;
  }

  private String getVersionString() {
    Properties properties = new Properties();
    InputStream inputStream = null;
    try {
      inputStream = getClass().getResourceAsStream("/org/graphwalker/resources/version.properties");
      properties.load(inputStream);
      inputStream.close();
    } catch (IOException e) {
      ;
    } finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (Exception e) {
          logger.error("An error occurred when trying to get the version string", e);
        }
      }
    }
    StringBuilder stringBuilder = new StringBuilder();
    if (properties.containsKey("version.major")) {
      stringBuilder.append(properties.getProperty("version.major"));
    }
    if (properties.containsKey("version.minor")) {
      stringBuilder.append(".");
      stringBuilder.append(properties.getProperty("version.minor"));
    }
    if (properties.containsKey("version.fix")) {
      stringBuilder.append(".");
      stringBuilder.append(properties.getProperty("version.fix"));
    }
    if (properties.containsKey("version.git.commit")) {
      stringBuilder.append(", git commit ");
      stringBuilder.append(properties.getProperty("version.git.commit"));
    }
    return stringBuilder.toString();
  }

  private String generateListOfValidGenerators() {
    return "";
  }

  private String generateListOfValidStopConditions() {
    return "";
  }

  private boolean helpNeeded(String module, boolean condition, String message) {
    if (condition) {
      System.out.println(message);
      System.out.println("Type 'java -jar graphwalker.jar help " + module + "' for help.");
    }
    return condition;
  }
}
