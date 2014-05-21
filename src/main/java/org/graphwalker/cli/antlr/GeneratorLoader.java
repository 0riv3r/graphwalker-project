package org.graphwalker.cli.antlr;

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


import org.antlr.v4.runtime.misc.NotNull;
import org.graphwalker.cli.CLI_Parser;
import org.graphwalker.cli.CLI_ParserBaseListener;
import org.graphwalker.core.condition.*;
import org.graphwalker.core.generator.AStarPath;
import org.graphwalker.core.generator.CombinedPath;
import org.graphwalker.core.generator.PathGenerator;
import org.graphwalker.core.generator.RandomPath;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by krikar on 5/14/14.
 */
public class GeneratorLoader extends CLI_ParserBaseListener {

    StopCondition stopCondition = null;
    ArrayList<PathGenerator> pathGenerators = new ArrayList<>();
    ArrayList<StopCondition> stopConditions = new ArrayList<>();

    @Override
    public void exitBooleanAndExpression(@NotNull CLI_Parser.BooleanAndExpressionContext ctx) {
        if ( ctx.AND().size() > 0 ) {
            CombinedCondition combinedCondition = new CombinedCondition();
            combinedCondition.addStopCondition(stopConditions.get(0));
            combinedCondition.addStopCondition(stopConditions.get(1));
            stopCondition = combinedCondition;
         }
    }

    @Override
    public void exitStopCondition(@NotNull CLI_Parser.StopConditionContext ctx) {
        if ( ctx.getChild(0).getText().equalsIgnoreCase("never") ) {
            stopConditions.add(new Never());
        } else if ( ctx.getChild(0).getText().equalsIgnoreCase("edge_coverage") ) {
            stopConditions.add(new EdgeCoverage(Integer.parseInt(ctx.getChild(2).getText())));
        } else if ( ctx.getChild(0).getText().equalsIgnoreCase("vertex_coverage") ) {
            stopConditions.add(new VertexCoverage(Integer.parseInt(ctx.getChild(2).getText())));
        } else if ( ctx.getChild(0).getText().equalsIgnoreCase("reached_vertex") ) {
            stopConditions.add( new ReachedVertex(ctx.getChild(2).getText()));
        } else if ( ctx.getChild(0).getText().equalsIgnoreCase("reached_edge") ) {
            stopConditions.add(new ReachedEdge(ctx.getChild(2).getText()));
        } else if ( ctx.getChild(0).getText().equalsIgnoreCase("time_duration") ) {
            stopConditions.add(new TimeDuration(Long.parseLong(ctx.getChild(2).getText()), TimeUnit.SECONDS));
        }
    }

    @Override
    public void exitLogicalExpression(@NotNull CLI_Parser.LogicalExpressionContext ctx) {
        if ( ctx.OR().size() > 0 ) {
            AlternativeCondition alternativeCondition = new AlternativeCondition();
            alternativeCondition.addStopCondition(stopConditions.get(0));
            alternativeCondition.addStopCondition(stopConditions.get(1));
            stopCondition = alternativeCondition;
        }
    }

    @Override
    public void exitGenerator(@NotNull CLI_Parser.GeneratorContext ctx) {
        if ( stopConditions.size() == 1 ) {
            stopCondition = stopConditions.get(0);
        }

        if ( ctx.getChild(0).getText().equalsIgnoreCase("random") ) {
            pathGenerators.add(new RandomPath(stopCondition));
        }
        else if ( ctx.getChild(0).getText().equalsIgnoreCase("a_star") ) {
            pathGenerators.add(new AStarPath((NamedStopCondition)stopCondition));
        }
        stopConditions.clear();
    }

    public PathGenerator getGenerator() {
        if ( pathGenerators.size() == 0 ) {
          return null;
        } else if ( pathGenerators.size() == 1 ) {
          return pathGenerators.get(0);
        }

        CombinedPath combinedPath = new CombinedPath();
        for ( PathGenerator pathGenerator : pathGenerators ) {
            combinedPath.addPathGenerator(pathGenerator);
        }
        return combinedPath;
    }
}
