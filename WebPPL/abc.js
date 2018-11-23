/*
Implementation of the abc scenario in WebPPL
 Usage: 
 webppl abc.js -- nSamples nPers nPJ jammed?(true/false) nObs(max is 25) algorithm(SMC or incrementalMH) 
 e.g. 
 webppl abc.js -- 1000 3 3 true 25 SMC
*/

// state = [[loc1 loc2 loc3], [item1 item2 item3], [pj1 pj2 pj3], cm-water?, cm-coffee?, p-paper?, p-jammed?]

// functions to manipulate arrays
var last = function(arr) {
    return arr[arr.length-1];
}

var insertAt = function(ar, i, x) {
    return ar.slice(0, i).concat([x]).concat(ar.slice(i));
}

var removeAt = function(ar, i) {
    return ar.slice(0, i).concat(ar.slice(i + 1));
}

var replaceAt = function(ar, i, x) {
    return ar.slice(0, i).concat([x]).concat(ar.slice(i + 1));
}

var drop = function(n, ar) {
    return n > ar.length ? [] : ar.slice(n);
}

var take = function(n, ar) {
    return n >= ar.length ? ar : ar.slice(0, n);
}


var locations = ["outside", "door", "printer", "coffeemachine", "paperstack", "watertap", "coffeejar"]

var isAtLocation = function(state, person, location) {
    return state[0][person] == location
}

var hasItem = function(state, person, item) {
    return state[1][person] == item
}

var hashandsFree = function(state, person) {
    return state[1][person] == "nothing"
}

// helper, returns possible goto actions given a state
var gotoActionRecur = function(state0, i, states, n) {
    if (i == state0[0].length) {
        return states
    } else {
        if (state0[0][i] == "outside") {                      
            return gotoActionRecur(state0, i+1, states.concat([replaceAt(state0, 0, replaceAt(state0[0], i, "door"))]), 1)
        } else {
            return n > 6 ?
                gotoActionRecur(state0, i+1, states, 1) :
                state0[0][i] == "door" && n == 1 ? 
                    gotoActionRecur(state0, i, states.concat([replaceAt(state0, 0, replaceAt(state0[0], i, "outside"))]), n+1) :
                    locations[n] == state0[0][i] ?
                        gotoActionRecur(state0, i, states, n+1) :
                        gotoActionRecur(state0, i, states.concat([replaceAt(state0, 0, replaceAt(state0[0], i, locations[n]))]), n+1)
        }
    }
}

// returns possible move actions given a state
var gotoAction = function(state0) {
    return gotoActionRecur(state0, 0, [], 1)
}

// helper, returns possible print actions
var printActionRecur = function(state0, states, i) {
    if (i == state0[2].length) {
        return states
    } else {
        return !(state0[2][i]) && !(state0[6]) && state0[5] ?
            printActionRecur(state0, states.concat([replaceAt(state0, 2, replaceAt(state0[2], i, true))]), i+1) :
            printActionRecur(state0, states, i+1)
    }
}

// returns possible print actions
var printAction = function(state0) {
    return printActionRecur(state0, [], 0)
}

// returns printer-jammed state if printer is not jammed in given state
var printerjammedAction = function(state0) {
    return state0[6] ? [] : [replaceAt(state0, 6, true)]
}

// helper, returns possible repair states
var repairPrinterActionRecur = function(state0, states, i) {
    if (i == state0[0].length) {
        return states
    } else {
        return hashandsFree(state0, i) && isAtLocation(state0, i, "printer") && state0[6] ?
            repairPrinterActionRecur(state0, states.concat([replaceAt(state0, 6, false)]), i+1) :
            repairPrinterActionRecur(state0, states, i+1)
    }
}

// returns possible repair-printer states
var repairPrinterAction = function(state0) {
    return repairPrinterActionRecur(state0, [], 0)
}

// helper, returns possible states where a person gets coffee
var getCoffeeActionRecur = function(state0, states, i) {
    if (state0[1].length == i) {
        return states
    } else {
        return isAtLocation(state0, i, "coffeemachine")  && state0[3] && state0[4] && hashandsFree(state0, i) ?
            getCoffeeActionRecur(state0, states.concat([replaceAt(state0, 1, replaceAt(state0[1], i, "coffee"))]), i+1) :
            getCoffeeActionRecur(state0, states, i+1)
    }
}

// returns possible states where a person gets coffee
var getCoffeeAction = function(state0) {
    return getCoffeeActionRecur(state0, [], 0)
}

var locProvides = {
    "watertap" : "water",
    "coffeejar" : "groundcoffee",
    "paperstack" : "paper"
}

// helper, checks if someone could have fetched something, using the locProvides dictionary
var fetchActionRecur = function(state0, states, i) {
    if (state0[0].length == i) {
        return states
    } else {
        return hashandsFree(state0, i) && 
            (isAtLocation(state0, i, "watertap") || isAtLocation(state0, i, "coffeejar") || isAtLocation(state0, i, "paperstack")) ?
                fetchActionRecur(state0, states.concat([replaceAt(state0, 1, replaceAt(state0[1], i, locProvides[state0[0][i]]))]), i+1) :
                fetchActionRecur(state0, states, i+1)
    }
}

// returns states in which someone fetched an item
var fetchAction = function(state0) {
    return fetchActionRecur(state0, [], 0)
}

var ressourceOf = {
    "paper" : "printer",
    "water" : "coffeemachine",
    "groundcoffee" : "coffeemachine"
}

// checks if an location has the given item
var locHasItem = function(state0, item) {
    return item == "paper" ? 
        state0[5] :
        item == "water" ?
            state0[3] :
            item == "groundcoffee" ?
                state0[4] :
                true
}

var stateIndex = {
    "water" : 3,
    "groundcoffee" : 4,
    "paper" : 5
}

// helper, checks if someone replenished something
var replenishActionRecur = function(state0, states, i) {
    if (state0[0].length == i) {
        return states
    } else {
        return isAtLocation(state0, i, ressourceOf[state0[1][i]]) && !(locHasItem(state0, state0[1][i])) ?
            replenishActionRecur(state0, states.concat([replaceAt(replaceAt(state0, 1, replaceAt(state0[1], i, "nothing")), stateIndex[state0[1][i]], true)]), i+1) :
            replenishActionRecur(state0, states, i+1)            
    }
}

// returns states in which someone replenished an item
var replenishAction = function(state0) {
    return replenishActionRecur(state0, [], 0)
}

// returns a list of probabilities 
// that will later gets mapped to the states list
var weightList = function(states, weight) {
    return states == [] ? [] : repeat(states.length, function() { return weight })
}

// returns a distribution of all possible successor states given a state
// use the following weights
// jam-printer: 0.25; repair-printer: 2; getCoffee: 4, print: 3; fetch: 2; put: 2; move: 1
var predictStates = function(state0) {
    var prints = printAction(state0)
    var jammed = printerjammedAction(state0)
    var repairs = repairPrinterAction(state0)
    var coffees = getCoffeeAction(state0)
    var fetches = fetchAction(state0)
    var replenishes = replenishAction(state0)
    var gotos = gotoAction(state0)
    return Categorical({
       ps: [].concat(weightList(prints, 3)).concat(weightList(jammed, 0.25)).
            concat(weightList(repairs, 2)).concat(weightList(coffees, 4)).concat(weightList(fetches, 2)).
            concat(weightList(replenishes, 2)).concat(weightList(gotos, 1)) , 
       vs: [].concat(prints).concat(jammed).concat(repairs).concat(coffees).
            concat(fetches).concat(replenishes).concat(gotos)
    })
}

// helper, creates the state related observation 
var getObsRecur = function(state, i, obs) {
    return i == state[0].length ?
        obs :
        locations.indexOf(state[0][i]) == 0 ?
            getObsRecur(state, i+1, obs) :
            getObsRecur(state, i+1, replaceAt(obs, locations.indexOf(state[0][i])-1, 1))
}

// returns the observation that matches the given state
var getObs_ = function(state) {
    return getObsRecur(state, 0, [0, 0, 0, 0, 0, 0])
}

// samples a successor state from the predictStates distribution
var transition = function(state0) {
    return sample(predictStates(state0))
}

// Observations of presence sensors
// 25 observations, each index of a observation matches a location.
// [Door, Printer, CoffeeMachine, PaperStack, WaterTap, CoffeeJar]
var evalObs = [[1, 0, 0, 0, 0, 0], [0, 1, 0, 0, 0, 0], [0, 1, 0, 0, 0, 0], [0, 0, 0, 1, 0, 0], [0, 0, 0, 1, 0, 0], [0, 1, 0, 0, 0, 0], [1, 1, 0, 0, 0, 0], [0, 1, 0, 0, 1, 0], [0, 1, 0, 0, 1, 0], [0, 1, 0, 0, 1, 0], [0, 0, 0, 0, 1, 1], [0, 0, 0, 0, 1, 1], [0, 0, 0, 0, 1, 1], [0, 0, 0, 0, 1, 1], [0, 0, 0, 0, 1, 1], [0, 0, 1, 0, 0, 1], [0, 0, 1, 0, 0, 0], [0, 0, 1, 0, 0, 0], [0, 0, 1, 0, 0, 0], [0, 0, 1, 0, 0, 0], [0, 0, 1, 0, 0, 0], [1, 0, 1, 0, 0, 0], [0, 0, 1, 0, 0, 0], [1, 0, 0, 0, 0, 0], [0, 0, 0, 0, 0, 0]]


/* Return the intial state.
   The state of the enviroment has following representation:
    [persons-loaction, persons-items, printjobs, coffee-machine-water, coffee-machine-coffee, printer-paper, printer-jammed]
    - persons-location: [loc_1, ..., loc_n]   	
    - persons-items: [item_1, ..., item_n]		
    - printjobs: [done?_1, ..., done_m]
    - coffee-machine-water: true/false
    - coffee-machine-coffee: true/false
    - printer-paper: true/false
    - printer-jammed: true/false */
var createState0 = function(nPers, nPJ, jammed) {
    [].concat([repeat(nPers, function() { return "outside" })]).
        concat([repeat(nPers, function() { return "nothing" })]).
        concat([repeat(nPJ, function() { return false })]).
        concat([false, false, false, jammed])
} 

// model
// returns the final state given the intial state and observation sequence
var abcRecur = function(state0, obs) {
    if (obs.length == 0) {
        return state0
    } else {
        var newState = sampleWithFactor(
            predictStates(state0),
            function(v) { return _.isEqual(getObs_(v), obs[0]) ? 0 : -Infinity })

        return abcRecur(newState, obs.slice(1))
    }    
}
var abc = function(nPers, nPJ, jammed, nObs) {
    return function() { abcRecur(createState0(nPers, nPJ, jammed), take(nObs, evalObs)) }
}

// main

var usage = "webppl abc.js -- nSamples nPers nPJ jammed?(true/false) nObs(max is 25) algorithm(SMC or incrementalMH) \n e.g. webppl abc.js -- 1000 3 3 true 25 SMC"

var main = function(argv) {
    if ((argv['_'].length != 7) || (argv['h'] != undefined)) return usage

    var model = abc(argv['_'][2], argv['_'][3], argv['_'][4], argv['_'][5])
    if(argv['_'][6] == "SMC"){
        return Infer({ model: model, method: argv['_'][6], particles: argv['_'][1] })
    }
    else if (argv['_'][6] == "incrementalMH"){
        return Infer({ model: model, method: argv['_'][6], samples: argv['_'][1] })
    }
    else return usage
} 


main(argv)