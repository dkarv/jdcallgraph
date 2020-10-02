# jdcallgraph
JDCallgraph - Dynamic call graph generation for Java. Uses 
[ByteBuddy](http://bytebuddy.net/) to instrument the target application.

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

## Configuration options
You can pass the path to a configuration file in the following way:
```
java -javaagent:jdcallgraph=/path/to/config.ini -jar target.jar
```
The config file has to contain key-value-pairs in a syntax similar to YAML. Whitespace as well as lines starting with # are ignored:
```
# this is a comment
key1: false
# a list of values
key2: 1, 2, 3
```
You can check out the configurations in the [example folder](./examples). Options not present in 
your config take the value from 
[defaults.ini](./jdcallgraph/src/main/resources/com/dkarv/jdcallgraph/defaults.ini)
The following options are supported:

#### Output directory `outDir` (String)
Specify an output directory. Either absolute or relative path to a folder.
JDCallgraph will put all the resulting graphs and log files into this folder.

#### Log Level `logLevel` (int)
Specify a log level. From 6 - TRACE (the most verbose) to 0 - OFF (no logging).

#### Console Output `logConsole` (boolean)
Switch logging to console on or off. When set to true it will not only log to the logfiles but also print log statements to the console.
The output is mixed with the output of the original program.

#### Multigraph `multiGraph` (boolean)
Whether the resulting graphs should contain duplicate edges. 
Note that JDCallgraph has to keep a list of known edges which might lead to memory issues with large graphs.

#### Group graphs by `groupBy` (Enum)
Specify the criteria that is used to group the graphs. It will output one graph per category. Valid options are:
- THREAD: The default. One graph per thread of the observed program.
- ENTRY: A new graph for each entry method. An entry method is every interesting method with an empty stacktrace

#### Output format `writeTo` ([Enum])
Specify one or multiple output formats. Multiple formats are separated by comma: `DOT,TRACE`. Valid options:
- TRACE: Write a file called `trace.csv`. It contains one row per entry method and a list of methods that come after behind (Typically a list of methods covered per test).
- COVERAGE: Write a `coverage.csv` file. The opposite of TRACE. For each method it lists the tests this method was covered in.
