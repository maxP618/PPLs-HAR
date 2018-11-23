/** Implementation of the abc scenario in Figaro
  *
  * Usage: 
  * Assuming the Figaro jar name is “figaro_2.12-5.0.0.0-fat.jar” 
  * and is in the “/Applications/figaro” directory, you can run:
  * scala -cp /Applications/figaro/figaro_2.12-5.0.0.0-fat.jar Abc.scala 
  *         nSamples nPers nPJ jammed?(true/false) nObs(max is 25)
  * e.g.
  * scala -cp /Applications/figaro/figaro_2.12-5.0.0.0-fat.jar Abc.scala 1000 3 3 true 25
*/

import com.cra.figaro.algorithm.filtering.ParticleFilter
import com.cra.figaro.language._
import com.cra.figaro.library.compound.If

object Abc {

  val locations = List("outside", "door", "printer", "coffeemachine", "paperstack", "watertap", "coffeejar")

  /** checks if a person is at the given location */
  def isAtLocation(state0: List[List[_]], person: Int, location: Any): Boolean = {
    return state0(0)(person) == location
  }

  /** checks if a person has the given item */
  def hasPersonItem(state0: List[List[_]], person: Int, item: String): Boolean = {
    return state0(1)(person) == item
  }

  /** checks if the person has his hands free */
  def hasHandsFree(state0: List[List[_]], person: Int): Boolean = {
    return state0(1)(person) == "nothing"
  }

  def indexOfLocation(loc: Any): Int = {
    return locations.indexOf(loc)-1
  }

  /** Implementation of actions
    *
    * Use the following weights: 
    * jam-printer: 0.25; repair-printer: 2; getCoffee: 4, print: 3; fetch: 2; put: 2; move: 1
    */

  /** Returns a list of tuples with all successor states after possible goto actions.
      First element of a tuple is the weight of sampling this particular state. */
  def actionGoto(state0: List[List[_]]): List[(Double, List[List[_]])] = {
    var states = List[(Double, List[List[_]])]()
    val locs = state0(0)
    for(i <- 0 until locs.length) {
      locs(i) match {
        case "outside" => states = states ++ List((1.0 -> state0.updated(0, locs.updated(i, "door"))))
        case "door" => for (loc <- locations.filter(_ != locs(i))) {
          states = states ++ List(1.0 -> state0.updated(0, locs.updated(i, loc)))
        }
        case _ => for (loc <- locations.tail.filter(_ != locs(i))) {
          states = states ++ List(1.0 -> state0.updated(0, locs.updated(i, loc)))
        }
      }
    }
    return states
  }

  /** Returns a list of tuples with all possible successor states after a print action.
      First element of a tuple is the weight of sampling this particular state. */
  def actionPrint(state0: List[List[_]]): List[(Double, List[List[_]])] = {
    var states = List[(Double, List[List[_]])]()
    val pjs = state0(2)
    for(i <- 0 until pjs.length) {
      if(pjs(i) == false && state0(3)(2) == true && state0(3)(3) == false) {
        states = states ++ List(3.0 -> state0.updated(2, pjs.updated(i, true)))
      }
    }
    return states
  }
 
  /** Returns the successor state in which the prinnter jammed. */
  def actionPrinterJammed(state0: List[List[_]]): List[(Double, List[List[_]])] = {
    if(state0(3)(3) == false) {
      return List(0.25 -> state0.updated(3, state0(3).updated(3, true)))
    }
    return List()
  }

  /** Returns a list of tuples with all successor states after possible repair-printer actions. */
  def actionRepairPrinter(state0: List[List[_]]): List[(Double, List[List[_]])] = {
    var states = List[(Double, List[List[_]])]()
    for(i <- 0 until state0(0).length) {
      if(isAtLocation(state0, i, "printer") && hasHandsFree(state0, i) && state0(3)(3) == true) {
        states = states ++ List(2.0 -> state0.updated(3, state0(3).updated(3, false)))
      }
    }
    return states
  }

  /** Returns a list of tuples with all successor states after possible get-coffee actions. */
  def actionGetCoffee(state0: List[List[_]]): List[(Double, List[List[_]])] = {
    var states = List[(Double, List[List[_]])]()
    for(i <- 0 until state0(0).length) {
      if(isAtLocation(state0, i, "coffeemachine") && state0(3)(0) == state0(3)(1) == true && hasHandsFree(state0, i)) {
        states = states ++ List(4.0 -> state0.updated(1, state0(1).updated(i, "coffee")))
      }
    }
    return states
  }

  val locProvides = Map[Any, Any]("watertap" -> "water", "paperstack" -> "paper", "coffeejar" -> "groundcoffee")

  /** Returns a list of tuples with all successor states after possible fetch actions. */
  def actionFetch(state0: List[List[_]]): List[(Double, List[List[_]])] = {
    var states = List[(Double, List[List[_]])]()
    for(i <- 0 until state0(0).length) {
      if(hasHandsFree(state0, i) && (isAtLocation(state0, i, "paperstack")
        || isAtLocation(state0, i, "watertap") || isAtLocation(state0, i, "coffeejar"))) {
        states = states ++ List(2.0 -> state0.updated(1, state0(1).updated(i, locProvides(state0(0)(i)))))
      }
    }
    return states
  }

  val resourceOf = Map[Any, Any]("water" -> "coffeemachine", "groundcoffee" -> "coffeemachine", "paper" -> "printer", "nothing" -> null, "coffee" -> null)

  /** checks if the location has the given item */
  def hasLocItem(state0: List[List[_]], item: Any): Boolean = {
    item match {
      case "water" => state0(3)(0) == true
      case "groundcoffee" => state0(3)(1) == true
      case "paper" => state0(3)(2) == true
      case _ => false
    }
  }

  val itemIndex =  Map[Any, Int]("water" -> 0, "groundcoffee" -> 1, "paper" -> 2, "nothing" -> -1)
  
  /** Returns a list of tuples with all successor states after possible replenish actions. */
  def actionReplenish(state0: List[List[_]]): List[(Double, List[List[_]])] = {
    var states = List[(Double, List[List[_]])]()
    for(i <- 0 until state0(0).length) {
      if(isAtLocation(state0, i, resourceOf(state0(1)(i))) && !(hasLocItem(state0, state0(1)(i)))) {
        states = states ++ List(2.0 -> state0.updated(1, state0(1).updated(i, "nothing")).updated(3, state0(3).updated(itemIndex(state0(1)(i)), true)))
      }
    }
    return states
  }

  /** takes a state, returns a distribution of successor states */
  def predictStates(state0: List[List[_]]): AtomicSelect[List[List[_]]] = {
    val allstates1 = actionGoto(state0) ++ actionRepairPrinter(state0) ++ actionFetch(state0)
    val allstates2 = actionReplenish(state0) ++ actionGetCoffee(state0) ++ actionPrint(state0) ++ actionPrinterJammed(state0)
    val allstates = allstates2 ++ allstates1
    val stateDist = new AtomicSelect[List[List[_]]](new String(), allstates, new ElementCollection {})
    return stateDist
  }

  // Observations of presence sensors
  // 25 observations, each index of a observation matches a location.
  // List(Door, Printer, CoffeeMachine, PaperStack, WaterTap, CoffeeJar)
  val evalObs = List(List(1, 0, 0, 0, 0, 0), List(0, 1, 0, 0, 0, 0), List(0, 1, 0, 0, 0, 0), List(0, 0, 0, 1, 0, 0), List(0, 0, 0, 1, 0, 0), List(0, 1, 0, 0, 0, 0), List(1, 1, 0, 0, 0, 0), List(0, 1, 0, 0, 1, 0), List(0, 1, 0, 0, 1, 0), List(0, 1, 0, 0, 1, 0), List(0, 0, 0, 0, 1, 1), List(0, 0, 0, 0, 1, 1), List(0, 0, 0, 0, 1, 1), List(0, 0, 0, 0, 1, 1), List(0, 0, 0, 0, 1, 1), List(0, 0, 1, 0, 0, 1), List(0, 0, 1, 0, 0, 0), List(0, 0, 1, 0, 0, 0), List(0, 0, 1, 0, 0, 0), List(0, 0, 1, 0, 0, 0), List(0, 0, 1, 0, 0, 0), List(1, 0, 1, 0, 0, 0), List(0, 0, 1, 0, 0, 0), List(1, 0, 0, 0, 0, 0), List(0, 0, 0, 0, 0, 0))


  /** Create the intial state.
   * The state of the enviroment has following representation:
   * List(persons-loaction, persons-items, printjobs, List(coffee-machine-water, 
   *         coffee-machine-coffee, printer-paper, printer-jammed))
   * - persons-location: List(loc_1, ..., loc_n) 	
   * - persons-items: List(item_1, ..., item_n)		
   * - printjobs: List(done?_1, ..., done_m)
   * - coffee-machine-water: true/false
   * - coffee-machine-coffee: true/false
   * - printer-paper: true/false
   * - printer-jammed: true/false 
  */
  def createState0(nPers: Int, nPj: Int, jammed: Boolean): List[List[_]] = {
    val locs = for(x <- 0 until nPers) yield "outside"
    val items = for(x <- 0 until nPers) yield  "nothing"
    val pjs = for (x <- 0 until nPj) yield false
    val env = List[Any](false, false, false, jammed)
    val state0 = List[List[Any]](locs.toList, items.toList, pjs.toList, env)
    return state0
  }

  /** returns the observation that matches the given state */
  def getObs(state0: List[List[_]]): Element[List[Int]] = {
    var obs = List(0, 0, 0, 0, 0, 0)
    val locs = state0(0)
    for(i <- 0 until locs.length) {
      if(locs(i) != "outside") {
        obs = obs.updated(indexOfLocation(locs(i)), 1)
      }
    }
    return Constant(obs)
  }

  /** Transition model, which is a function from a universe representing
    * the distribution of states at one time point to a universe representing the distribution
    * at the next time point.
    */
  def transition(prevU: Universe): Universe = {
    val newU = Universe.createNew()
    val statet = prevU.get[List[List[_]]]("statetp1")
    val dist = Chain(statet, predictStates)("statetp1", newU)
    val obs = Chain(statet, getObs)("obs", newU)
    val copiedstatet = Chain(statet, (s: List[List[_]]) => Constant(s))("copiedstatet", newU)
    newU
  }

  /** Main method
   * arguments: nParticles, nPers, nPJ, jammed, nObs
   */
  def main(args: Array[String]): Unit = {
    // create the initial universe and pass it with the transition model to the particle filter
    val initialU = Universe.createNew()
    val state0c = Constant(createState0(args(1).toInt, args(2).toInt, args(3).toBoolean))("copiedstatet", initialU)
    val state0 = Chain(state0c, predictStates)("statetp1", initialU)

    val pf = ParticleFilter(initialU, transition, args(0).toInt)
    pf.start()
    for(i <- 0 to (args(4).toInt-1)) {
      pf.advanceTime(List(NamedEvidence("obs", Observation(evalObs(i)))))
    }
    val dist = pf.currentDistribution("copiedstatet")
    pf.stop()
    pf.kill()
    dist take dist.length foreach println
  }
}