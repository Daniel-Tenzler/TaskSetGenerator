## REQUIREMENTS

- OS: Windows 10.0 (The Basic Generation Method _may_ also work on other systems)
- [Java version 20](https://www.oracle.com/java/technologies/javase/jdk20-archive-downloads.html)
- [Maven version 3.9.7](https://maven.apache.org/download.cgi)
- [git-scm](https://git-scm.com/) 

## Sources

- [TGFF](https://robertdick.org/projects/tgff/), last visited on 29.05.2024
- [ApacheCommonsMath](https://commons.apache.org/proper/commons-math/), last visited on 29.05.2024
- [Google-Or-Tools](https://developers.google.com/optimization), last visited on 29.05.2024

## Installation

- 'git clone https://git.tu-berlin.de/research1/framework-for-scheduling-algorithm-evalutation/task-set-generation/task-set-generator-dahlia.git' OR Download source code as zip from 'https://git.tu-berlin.de/research1/framework-for-scheduling-algorithm-evalutation/task-set-generation/task-set-generator-dahlia.git'
- navigate into the root project folder (tsgprojekt)
- open a console and execute 'mvn clean compile package install -DskipTests'  ( Skipping tests is highly recommended )
- New artifact is located at '/target/taskgenerator-1.0.jar'
- OPTIONAL: If building a new artifact fails, a backup artifact is located at '/out/artifacts/tasksetgenerator_jar/tasksetgenerator.jar'


## USAGE

To start the generation using the **Basic Generation Method** (recommended), open the console where the .jar file is located and use the command

``` java -jar taskgenerator-1.0 PATH/TO/INPUT/FILE``` (RECOMMENDED)

OR

``` java -jar taskgenerator-1.0 0``` (NOT RECOMMENDED)


--- 

To start the generation using the **TGFF-based Generation Method**, open the console where the .jar file is located and use the command

``` java -jar taskgenerator-1.0-file PATH/TO/TGFFINPUT/FILE``` (RECOMMENDED)

OR

``` java -jar taskgenerator-1.0-file 1``` (NOT RECOMMENDED)


---

### Example Input file 

Example Input files are located at /inputFiles. \
IMPORTANT: In Windows, the path needs to be specified with '/' and not '\\'.\
Example Path: 'C:/Users/User/test/exampleInput.txt'

#### Usage Hints
- Use the absolute path of the input file
- It is not recommended to use the direct generation (java -jar taskgenerator-1.0 0 and java -jar taskgenerator-1.0 1). They only support a subset of input parameters
- If the direct generation is used, make sure to write decimal numbers as (X.Y) and NOT as (X,Y)
- If the direct generation is used, write all inputs in the same line separated by whitespaces
- Depending on the operating system, the input file path must be specified using '/' or '\\'


## Format for an input file for Basic Generation:

In the following, we describe how to build an input-file line by line. An example input file can be found in /inputFiles/exampleInput.txt
Important notes for creating an input-file:
- do not leave trailing whitespaces at the end of a line.
- all integer and double values must be positive or 0 unless otherwise stated.
- do not leave empty lines in the input file.
- all input files must start with either 'Basic' or 'TGFF'.

```
Basic
```
'Basic' denotes that the Basic Generation Method should be used. This line is required to be the first line of the input file.

---

```
tasknum [min : int] [max : int]
resourcenum [min : int] [max : int]
residencyconstraints [min : int] [max : int]
totalprecedencerelations [min : int] [max : int]
periodpercentage [min : int] [max : int]
periodlength [min : int] [max : int]
```
'tasknum' denotes the number of tasks. \
'resourcenum' denotes the number of resources. \
'residencyconstraints' denotes the number of residency constraints.\
'totalprecedencerelations' denotes the total number of precedence relations.\
'periodpercentage' denotes the period percentage of the task set.\
'periodlength' denotes the period length of each task.

Each of these parameters requires a minimum and maximum value. These values must be integers.\
Example: 'tasknum 1 15'\
This example forces the task set to have between 1 and 15 tasks. Both bounds are inclusive.

---

``` 
multiresidency [boolean]
phasedreleasetimes [boolean]
posthocprecedence [boolean]
posthocresidency [boolean]
```
'multiresidency' controls the generation of multiple resource identifiers per residency constraint.\
'phasedreleasetimes' controls the generation of phases for all periodic tasks.\
'posthocprecedence' controls the generation of precedence constraints after a schedule has been found.\
'posthocresidency' controls the generation of residency constraints after a schedule has been found.

Each of these parameters requires a boolean value. \
Example: 'multiresidency true'\
This example enables the generation of multiple resource identifiers per residency constraint.

---
```
subgraphnumber [int]
numberofworkerthreads [int]
solutioncount [int]
```
'subgraphnumber' is an alternative parameter for the generation of precedence relations. It forces the generation of precedence relations with the given number of subgraphs. It is only used if 'totalprecedencerelations' is set to 0.\
'numberofworkerthreads' controls the number of threads the CP-Solver uses. See [Documentation](https://github.com/google/or-tools/blob/stable/ortools/sat/docs/troubleshooting.md#improving-performance-with-multiple-workers). It is recommended to set this value to 1.\
'solutioncount' controls how many solutions should be produced by the CP-Solver.

Each of these parameters requires an integer value. \
Example: 'subgraphnumber 5'\
This example forces the generation of precedence relations with exactly 5 subgraphs.

---

```
releasetimes [min : int] [max : int]
deadlines [min : int] [max : int]
wcet [min : int] [max : int]
```
These parameters enforce minimum and maximum values for the release time, deadline and WCET value of all tasks.\
Each of these parameters requires a minimum and maximum integer value. \
If set to -1, the corresponding value is disabled during generation.\
Example: 'releasetimes -1 10'\
This example forces the generation of tasks with a release time of maximum 10. The minimum release time is ignored.

---

``` 
saveoutput 
maximizewcet
deadlineequaltoperiod
debug
```
These optional parameters enable additional functions of the task set generator.
'saveoutput' enables the generation of a '.dot' and '.json' file at '/output' for every generated task set. \
'maximizewcet' adds an objective to the solver that forces the solver to attempt to maximize the total accumulated WCET of the task set.\
'deadlineequaltoperiod' forces all deadlines to be equal to the period of the task (for periodic tasks).\
'debug' prints debug messages to the console during generation. \

These parameters do not require an additional value. Adding them to the file enables the stated functionality.

---

The complete template for the Basic Generation Method input file:

```
Basic
tasknum [min : int] [max : int]
resourcenum [min : int] [max : int]
residencyconstraints [min : int] [max : int]
totalprecedencerelations [min : int] [max : int]
periodpercentage [min : int] [max : int]
periodlength [min : int] [max : int]
multiresidency [boolean]
phasedreleasetimes [boolean]
posthocprecedence [boolean]
posthocresidency [boolean]
subgraphnumber [int]
numberofworkerthreads [int]
solutioncount [int]
releasetimes [min : int] [max : int]
deadlines [min : int] [max : int]
wcet [min : int] [max : int]
saveoutput 
maximizewcet
deadlineequaltoperiod
debug
```

---

Optional Distribution parameters template:
```
residencydistribution [distribution]
periodicitydistribution [distribution]
periodlengthdistribution [distribution]
NORDistribution [distribution]
NOPrDistribution [distribution]
NOTasksDistribution [distribution]
NOResDistribution [distribution]

periodlengthpoissonmean [double]
periodicityPoissonMean [double]
residencyPoissonMean [double]
NORPoissonMean [double]
NOPrPoissonMean [double]
NOTasksPoissonMean [double]
NOResPoissonMean [double]
periodLengthBinomialP [double]
periodLengthBinomialN [int]
periodicityBinomialP [double]
periodicityBinomialN [int]
residencyBinomialP [double]
residencyBinomialN [int]
NORBinomialP [double]
NORBinomialN [int]
NOPrBinomialP [double]
NOPrBinomialN [int]
NOTasksBinomialP [double]
NOTasksBinomialN [int]
NOResBinomialP [double]
NOResBinomialN [int]
```
distribution : [UNIFORM,GEOMETRIC,POISSON,BINOMIAL]

In total, 7 constraints are supported with the provided distributions:\
'residencydistribution' controls the distribution of residency constraints among tasks.\
'periodicitydistribution' controls the distribution of periodic tasks among all tasks.\
'periodlengthdistribution' controls the distribution of period lengths within the set min and max values.\
'NORDistribution' controls the distribution of the number of residency constraints within the set min and max values.\
'NOPrDistribution' controls the distribution of the number of precedence constraints within the set min and max values.\
'NOTasksDistribution' controls the distribution of the number of tasks within the set min and max values.\
'NOResDistribution' controls the distribution of the number of resources within the set min and max values.

Only add lines depending on the selected distribution.

For uniform and geometric distribution, set the according distribution parameter value.
Example:
``` 
NOPrDistribution UNIFORM
```
``` 
NOTasksDistribution GEOMETRIC
```

For poisson distribution, add the relevant PoissonMean parameter.\
Example: 
``` 
periodlengthdistribution POISSON
periodlengthpoissonmean 6.0
```

For binomial distribution, add the relevant BinomialP and BinomialN parameter.\
Example:
``` 
NORDistribution BINOMIAL
NORBinomialP 0.5
NORBinomialN 10
```

If you do not use poisson or binomial distribution, do not add PoissonMean or BinomialN or BinomialP parameters. They will be ignored.

## Format for an input file for TGFF Generation:

In the following, we describe how to build an input-file line by line. An example input file can be found in /inputFiles/exampleTGFFInput.txt
Important notes for creating an input-file:
- do not leave trailing whitespaces at the end of a line.
- all integer and double values must be positive or 0 unless otherwise stated.
- do not leave empty lines in the input file.
- all input files must start with either 'Basic' or 'TGFF'.


```
TGFF
```
'TGFF' denotes that the TGFF-based Generation Method should be used. This line is required to be the first line of the input file.

---
```
graphcount [int]
```
'graphcount' denotes the number of graphs TGFF should produce. It requires an integer value above 0.

``` 
taskcount [average : int] [multiplier : int]
```
'taskcount' denotes the average number of tasks per graph, as well as a multiplier for that value. Both average and multiplier require integer values above 0.

``` 
periodpercentage [double]
```
'periodpercentage' denotes the percentage of periodic tasks within the task set. It requires a value between 0.0 and 1.0. It is recommended to set this value to 1.0.

``` 
multistartnodeprob [double]
```
'multistartnodeprob' denotes the chance for multiple start nodes to occur in each graph. It requires a value between 0.0 and 1.0.

``` 
startnodes [average : int] [multiplier : int]
```
'startnodes' denotes the number of start nodes per graph if multiple start nodes occur in the graph, as well as a multiplier for that value. Both average and multiplier require integer values above 0.

``` 
taskdegree [in : int] [out : int]
```
'taskdegree' denotes the maximum In-Degree and Out-Degree of all tasks. Both values need to be integers.

``` 
harddeadlineprob [double]
```
'harddeadlineprob' denotes the probability for deadlines to be hard. It requires a value within 0.0 and 1.0. It is recommended to set this value to 1.0.

``` 
resourcenum [int]
```
'resourcenum' denotes the number of resources for the task set. It requires a positive integer value. Setting this value to -1 forces the generator to calculate the number of resources from the DAG produced by TGFF.


``` 
tasktransitiontime [double]
```
'tasktransitiontime' denotes the time taken between tasks. TGFF uses this value to calculate deadlines and periods. It is recommended to set this value to 1.0, 2.0, 3.0 or 4.0.

``` 
periodmul [int-list seperated by whitespace]
```
'periodmul' is a list of integer value seperated by whitespaces. It provides multipliers for the periods for each graph. \
Example: 'periodmul 1 2 3 4'\
This example multiplies each graph period by a random value chosen from the list [1,2,3,4].


``` 
cleanupfiles [boolean]
posthocresidency [boolean]
calculateperiods [boolean]
multiresidency [boolean]
```

'cleanupfiles' controls whether the files generated by TGFF should be deleted after the task set has been generated.\
'posthocresidency' controls the generation of residency constraints after a schedule has been found.\
'calculateperiods' controls whether to use TGFF generated periods (false) or whether the TSG calculates its own periods for all graphs (true).\
'multiresidency' controls the generation of multiple resource identifiers for all residency constraints.

Each of these parameters requires a boolean value. \
Example: 'multiresidency true'\
This example enables the generation of multiple resource identifiers per residency constraint.

``` 
numberofworkerthreads [int]
solutioncount [int]
```
'numberofworkerthreads' controls the number of threads the CP-Solver uses. See [Documentation](https://github.com/google/or-tools/blob/stable/ortools/sat/docs/troubleshooting.md#improving-performance-with-multiple-workers). It is recommended to set this value to 1.\
'solutioncount' controls how many solutions should be produced by the CP-Solver.

Each of these parameters requires an integer value. \
Example: 'solutioncount 5'\
This example forces the generation of 5 task sets.


``` 
releasetimes [min : int] [max : int]
deadlines [min : int] [max : int]
wcet [min : int] [max : int]
```
These parameters enforce minimum and maximum values for the release time, deadline and WCET value of all tasks.\
Each of these parameters requires a minimum and maximum integer value. \
If set to -1, the corresponding value is disabled during generation.\
Example: 'releasetimes -1 10'\
This example forces the generation of tasks with a release time of maximum 10. The minimum release time is ignored.

``` 
residencyconstraints [min : int] [max : int]
```
'residencyconstraints' denotes the minimum and maximum number of residency constraints. For the TGFF-based Generation Method, this parameter is only enforced if posthocresidency is set to true. The min and max values must be integers equal to or above 0.


``` 
residencydistribution [distribution]
residencyPoissonMean [double]
residencyBinomialP [double]
residencyBinomialN [int]
```
This is the only distribution for TGFF that is supported. It is similar to the distributions mentioned for the Basic Generation Method. Only add lines according to the chosen distribution.

``` 
saveoutput
maximizewcet
deadlineequaltoperiod
debug
```
These optional parameters enable additional functions of the task set generator.
'saveoutput' enables the generation of a '.dot' and '.json' file at '/output' for every generated task set. \
'maximizewcet' adds an objective to the solver that forces the solver to attempt to maximize the total accumulated WCET of the task set.\
'deadlineequaltoperiod' forces all deadlines to be equal to the period of the task (for periodic tasks).\
'debug' prints debug messages to the console during generation. \

These parameters do not require an additional value. Adding them to the file enables the stated functionality.

---

The complete template for the TGFF-based Generation Method input file:

```
TGFF
graphcount [int]
taskcount [average : int] [multiplier : int]
periodpercentage [double]
multistartnodeprob [double]
startnodes [average : int] [multiplier : int]
taskdegree [in : int] [out : int]
harddeadlineprob [double]
resourcenum [int]
tasktransitiontime [double]
periodmul [int-list seperated by whitespace]
cleanupfiles [boolean]
posthocresidency [boolean]
calculateperiods [boolean]
multiresidency [boolean]
numberofworkerthreads [int]
solutioncount [int]
releasetimes [min : int] [max : int]
deadlines [min : int] [max : int]
wcet [min : int] [max : int]
residencyconstraints [min : int] [max : int]
residencydistribution [distribution]
residencyPoissonMean [double]
residencyBinomialP [double]
residencyBinomialN [int]
saveoutput
maximizewcet
deadlineequaltoperiod
debug
```
