# jdcallgraph
JDCallgraph - Dynamic call graph generation for Java. Uses [Javassist](http://jboss-javassist.github.io/javassist/) to instrument the target application.

[![Build Status](https://travis-ci.org/dkarv/jdcallgraph.svg?branch=master)](https://travis-ci.org/dkarv/jdcallgraph)

## How-To
Download the jdcallgraph.jar or build it using `mvn install`.
You have to invoke `java` with the `-javaagent:/path/to/jdcallgraph.jar=/path/to/config.ini`. A few examples:

### Plain Java/Jar
```
java -javaagent:/path/to/jdcallgraph.jar=/path/to/config.ini example.Main
```
or
```
java -javaagent:/path/to/jdcallgraph.jar=/path/to/config.ini -jar target.jar
```

### Maven
Simply invoke Maven with the `-DargLine=-javaagent:/path/to/jdcallgraph.jar=/path/to/config.ini` parameter. There is a [wrapper script](./wrapper/mvn.sh) you can use like normal Maven:
```
./mvn.sh test -f /path/to/pom.xml
```

### Ant
I did not succeed starting it with the ANT_OPTS environment variable. Instead you can edit your build.xml:
Change the lines
```
<junit fork="no">
  ...
</junit>
```
To
```
<junit fork="yes" forkmode="once">
  <jvmarg value="-javaagent:/path/to/jdcallgraph.jar=/path/to/config.ini" />
  ...
</junit>
```
