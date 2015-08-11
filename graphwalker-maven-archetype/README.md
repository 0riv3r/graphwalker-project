GraphWalker Maven Archetype [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.graphwalker/graphwalker-maven-archetype/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.graphwalker/graphwalker-maven-archetype)
================

A GraphWalker maven archetype. To help create boilerplate GraphWalker maven projects.

To create a sample project:

~~~sh
%> mvn archetype:generate -B -DarchetypeGroupId=org.graphwalker -DarchetypeArtifactId=graphwalker-maven-archetype -DarchetypeVersion=3.0.1-SNAPSHOT -DgroupId=com.company -DartifactId=myProject
~~~

Then cd into the project:
~~~sh
%> cd myProject
~~~
Build and run the test:
~~~sh
%> mvn org.graphwalker:graphwalker-mave3.0.1-SNAPSHOTgin:3.0.0:test
~~~

