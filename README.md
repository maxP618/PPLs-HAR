# 
## Anglican
The source code can be found under: PPLs-HAR/Anglican/abc/src/core.clj .
### Installation
------------
Installing Leiningen (Anglican):
```shell
# Download lein script and place to your $PATH
$ mkdir ~/bin
$ cd ~/bin
$ wget http://git.io/XyijMQ

# Make executable
$ chmod a+x ˜/bin/lein

# Add ~/bin to path
$ echo 'export PATH="$HOME/bin:$PATH"' >> ~/.bashrc

# Run lein and it will download the self-install package
$lein
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
Also, there is provided a standalone .jar. Run this simply with Java.
```shell
$ cd PPLs-HAR/Anglican/
$ java -jar abc--standalone.jar number-samples number-persons number-printjobs printer-jammed?(true/false) number-obs(max is 25) inference-method

#e.g. 
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
