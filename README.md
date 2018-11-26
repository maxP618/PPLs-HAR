#
This project contains three implementations of a causal probabilistic model for human activity recognition. The purpose is (1) to see whether one can easily implement causal, generative probabilistic models in PPLs, and (2) to compare inference performance of different PPLs. 
All three implementations implement and allow to perform inference in the office scenario [described here.](<https://dx.doi.org/10.1145/2370216.2370443>). 


**[Anglican](#Anglican)**<br>
**[WebPPL](#WebPPL)**<br>
**[Figaro](#Figaro)**<br>

## Anglican
The source code can be found at: PPLs-HAR/Anglican/abc/src/core.clj .
### Installation
Installing Leiningen (Anglican):
```shell
# Download lein script and place it to your $PATH
$ mkdir ~/bin
$ cd ~/bin
$ wget http://git.io/XyijMQ

# Make executable
$ chmod a+x ˜/bin/lein

# Add ~/bin to path
$ echo 'export PATH="$HOME/bin:$PATH"' >> ~/.bashrc

# Run lein and it will download the self-install package
$ lein
```
### Usage
Compile + Run:
```shell
$ cd PPLs-HAR/Anglican/abc
$ lein run number-samples number-persons number-printjobs printer-jammed?(true/false) number-obs(max is 25) inference-method

# e.g. 
$ lein run 100 3 3 true 25 smc
```

#### Standalone JAR
Alternatively, a standalone .jar is provided which can be run with Java:
```shell
$ cd PPLs-HAR/Anglican/
$ java -jar abc--standalone.jar number-samples number-persons number-printjobs printer-jammed?(true/false) number-obs(max is 25) inference-method

# e.g. 
$ java -jar abc--standalone.jar 100 3 3 true 25 smc
```

#### Supported Inference methods:
| Method       | Description         |
| ------------- |:-------------------------:|
| smc      |  Sequential Monte Carlo |
| importance      | Importance sampling     | 
| lmh | Lightweight Metropolis-Hastings      |
|   rmh    | Random-walk Lightweight Metropolis-Hastings  | 
|     almh  |  Adaptive scheduling lightweight Metropolis-Hastings | 
|    pgibbs   | Particle Gibbs (iterated conditional SMC)  | 
|   pimh    |  Particle independent Metropolis-Hastings | 
|   pgas    | 	Particle Gibbs with ancestor sampling  | 
|    pmcmc   |  	Interacting particle Markov chain Monte Carlo | 


--------------------------
## WebPPL
The source code can be found at: PPLs-HAR/WebPPL/abc.js .

### Installation
+ Install [node.js](<https://nodejs.org/en/>)
+ Install WebPPL by running: ``` npm install -g webppl ```

### Usage
```shell
$ cd PPLs-HAR/WebPPL
$ webppl abc.js -- nSamples nPers nPJ jammed?(true/false) nObs(max is 25) algorithm(SMC or incrementalMH)
# e.g. 
$ webppl abc.js -- 1000 3 3 true 25 SMC
```

#### Supported Inference methods:
| Method       | Description         |
| ------------- |:-------------------------:|
| SMC    |  Sequential Monte Carlo |
| incrementalMH      | Lightweight Incrementalized MCMC     | 

--------------------------

## Figaro
The source code can be found at: PPLs-HAR/Figaro/Abc.scala .

### Installation
The current version of Figaro, as of November 2018 is 5.0.0, and is available for Scala 2.12 and Scala 2.11.
+ Download the Scala binaries from: https://www.scala-lang.org/download/ and follow the installation guide at http://scala-lang.org/download/install.html.
+ Download Figaro from https://www.cra.com/figaro.
+ The directory, Figaro is installed to, contains the Figaro jar. The jar name ends with "fat". Remember this directory, as you will need it to run Figaro programs.

### Usage
Assuming the Figaro jar name is “figaro_2.12-5.0.0.0-fat.jar” and is in the “/Applications/figaro” directory, you can run:
```shell
$ cd PPLs-HAR/Figaro
$ scala -cp /Applications/figaro/figaro_2.12-5.0.0.0-fat.jar Abc.scala nSamples nPers nPJ jammed?(true/false) nObs(max is 25)
# e.g. 
$ scala -cp /Applications/figaro/figaro_2.12-5.0.0.0-fat.jar Abc.scala 1000 3 3 true 25
```
#### Supported Inference methods:
| Method       | Description         |
| ------------- |:-------------------------:|
| Particle Filter  |  Sequential Monte Carlo |
