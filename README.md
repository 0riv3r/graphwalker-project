graphwalker-project [![Build Status](https://travis-ci.org/GraphWalker/graphwalker-project.svg?branch=master)](https://travis-ci.org/GraphWalker/graphwalker-project)
===================

Documentation on http://graphwalker.org/

### How to build

Build and install GraphWalker modules locally on your machine

```bash
git clone https://github.com/GraphWalker/graphwalker-project.git
cd graphwalker-project
mvn install
```

### Build standalone command-line tool

```bash
mvn package -pl graphwalker-cli -am
```

The jar is in:
```bash
graphwalker-cli/target/graphwalker-cli-3.2.1.jar
```
