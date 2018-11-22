(ns abc.core
    "Implementation of the abc scenario in Anglican
     Usage: in abc/ run: 
       lein run number-samples number-persons number-printjobs printer-jammed?(true/false) 
                    number-obs(max is 25) inference-method
      (e.g. lein run 100 3 3 true 25 smc)"
    (:gen-class)
    (:use [anglican emit runtime])
    (:use [anglican.core :exclude [-main]])
    (:require [anglican.stat :as s])
)

;;; implementations of actions

(defm possible-states-help [state number-rooms i] 
    "Helper function takes a state, number of rooms, index i, returns all states with possible rooms at index i in list
     (i.e. returns all possible locations of person_i)."
    (if (= (nth state i) -1) (conj [] (assoc state i 0))
        
        (if (= (nth state i) 0)
            (loop [state-list-final [] j -1]
            (if (= j number-rooms)
                (if (not= (assoc state i j) state)
                (conj state-list-final (assoc state i j)) state-list-final)
                (recur (if (not= (assoc state i j) state)
                        (conj state-list-final (assoc state i j)) state-list-final) (inc j))))
        
        (loop [state-list-final [] j 0]
            (if (= j number-rooms)
            (if (not= (assoc state i j) state)
                (conj state-list-final (assoc state i j)) state-list-final)
            (recur (if (not= (assoc state i j) state)
                        (conj state-list-final (assoc state i j)) state-list-final) (inc j)))))))
    
(defm possible-states [state number-rooms]
    "Calls helper function above, with all indexes of state-0.
     Returs a list of new possible locations of persons."
    (if (not= (count state) 0)
        (loop [state-list-final [] i 0]
        (if (= i (dec (count state)))
            (concat (possible-states-help state number-rooms i) state-list-final)
            (recur (concat (possible-states-help state number-rooms i) state-list-final) (inc i))))
    []))

(defm assoc-states [state-full state-list-rooms]
    "Helper function that inserts location sub-state in complete state."
    (loop [states [] i 0]
        (if (= i (count state-list-rooms))
        states
        (recur (conj states (assoc state-full 0 (nth state-list-rooms i))) (inc i)))))


(defm at-location? [state person location]
    "Checks if a person i is at location x given a state."
    (= (nth (nth state 0) person) location))

(defm has-item? [state person item]
    "Checks if a person i has the item x."
    (= (nth (nth state 1) person) item))

(defm action-print [state-0]
    "Checks if a print action could have happened, returns a list with possible printjobs."
    (loop [state-list [] index 0]
        (if (= index (count (nth state-0 2)))
        state-list
        (recur (if (and (= (nth (nth state-0 2) index) false) (not (nth state-0 6)) (nth state-0 5))
                    (conj state-list (assoc state-0 2 (assoc (nth state-0 2) index true)))
                    state-list)
                (inc index)))))
    
(defm action-repair-printer [state-0]
    "Checks if someone could have repaired the printer,
     returns possible new states."
    (if (nth state-0 6)
        (loop [state-list [] i 0]
        (if (= i (count (nth state-0 0)))
            state-list
            (recur (if (and (at-location? state-0 i 1) (has-item? state-0 i "Nothing"))
                    (conj state-list (assoc state-0 6 false))
                    state-list)
                    (inc i))))
        []))


(defm action-get-coffee [state-0]
    "Checks if someone got coffee,
     returns possible new states."
    (loop [state-list [] i 0]
        (if (= i (count (nth state-0 1)))
        state-list
        (recur (if (and (at-location? state-0 i 2) (nth state-0 3) (nth state-0 4) (has-item? state-0 i "Nothing"))
                    (conj state-list (assoc state-0 1 (assoc (nth state-0 1) i "Coffee")))
                    state-list)
                (inc i)))))

(defm action-fetch-paper [state-0]
    "Checks if someone fetched paper,
     returns possible new states."
    (loop [state-list [] i 0]
        (if (= i (count (nth state-0 1)))
        state-list
        (recur (if (and (at-location? state-0 i 3) (has-item? state-0 i "Nothing"))
                    (conj state-list (assoc state-0 1 (assoc (nth state-0 1) i "Paper")))
                    state-list)
                (inc i)))))

(defm action-fetch-water [state-0]
    "Checks if someone fetched water,
     returns possible new states."
    (loop [state-list [] i 0]
        (if (= i (count (nth state-0 1)))
        state-list
        (recur (if (and (at-location? state-0 i 4) (has-item? state-0 i "Nothing"))
                    (conj state-list (assoc state-0 1 (assoc (nth state-0 1) i "Water")))
                    state-list)
                (inc i)))))

(defm action-fetch-groundcoffee [state-0]
    "Checks if someone fetched ground coffee,
     returns possible new states."
    (loop [state-list [] i 0]
        (if (= i (count (nth state-0 1)))
        state-list
        (recur (if (and (at-location? state-0 i 5) (has-item? state-0 i "Nothing"))
                    (conj state-list (assoc state-0 1 (assoc (nth state-0 1) i "Ground-Coffee")))
                    state-list)
                (inc i)))))

(defm action-replenish-paper [state-0]
    "Checks if someone replenished paper,
     returns possible new states."
    (loop [state-list [] i 0]
        (if (= i (count (nth state-0 1)))
        state-list
        (recur (if (and (has-item? state-0 i "Paper") (at-location? state-0 i 1) (not (nth state-0 5)))
                    (conj state-list (assoc (assoc state-0 1 (assoc (nth state-0 1) i "Nothing")) 5 true))
                    state-list)
                (inc i)))))

(defm action-replenish-water [state-0]
    "Checks if someone replenished water,
     returns possible new states."
    (loop [state-list [] i 0]
        (if (= i (count (nth state-0 1)))
        state-list
        (recur (if (and (has-item? state-0 i "Water") (at-location? state-0 i 2) (not (nth state-0 3)))
                    (conj state-list (assoc (assoc state-0 1 (assoc (nth state-0 1) i "Nothing")) 3 true))
                    state-list) (inc i)))))

(defm action-replenish-groundcoffee [state-0]
    "Checks if someone replenished ground coffee,
     returns possible new states."
    (loop [state-list [] i 0]
        (if (= i (count (nth state-0 1)))
        state-list
        (recur (if (and (has-item? state-0 i "Ground-Coffee") (at-location? state-0 i 2) (not (nth state-0 4)))
                    (conj state-list (assoc (assoc state-0 1 (assoc (nth state-0 1) i "Nothing")) 4 true))
                    state-list) (inc i)))))

(defm action-printer-jammed [state-0]
    "Returns a state, where the printer jammed."
    (if (nth state-0 6)
        []
        [(assoc state-0 6 true)]))

(defm put-weight [states weight]
    "Helper that takes vector of states and a weight and returns a vector [[state1 weight1] ...]"
    (map (fn [state] [state weight]) states))


;; add weights to the corresponding actions
;; split up the concats in seperate functions to avoid compile errors in "predict-states"
(defm add-weight1 [state-0 number-rooms]
    (concat 
    (put-weight (assoc-states state-0 (possible-states (first state-0) number-rooms)) 1)
    (put-weight (action-print state-0) 3)
    (put-weight (action-repair-printer state-0) 2)
    (put-weight (action-printer-jammed state-0) 0.25)
    (put-weight (action-get-coffee state-0) 4)))

(defm add-weight2 [state-0 number-rooms]        
    (concat
    (put-weight (action-fetch-water state-0) 2)
    (put-weight (action-fetch-groundcoffee state-0) 2)
    (put-weight (action-fetch-paper state-0) 2)))

(defm add-weight3 [state-0 number-rooms]        
    (concat
    (put-weight (action-replenish-water state-0) 2)
    (put-weight (action-replenish-groundcoffee state-0) 2)
    (put-weight (action-replenish-paper state-0) 2)))
        
(defm predict-states [state-0 number-rooms]
    "Returns a distribution of all possible states given a state.
     Use the following weights: 
     jam-printer: 0.25; repair-printer: 2; getCoffee: 4, print: 3; fetch: 2; put: 2; move: 1"
    (categorical (concat (add-weight1 state-0 number-rooms) 
        (add-weight2 state-0 number-rooms) (add-weight3 state-0 number-rooms))))

; Observations of presence sensors
; 25 observations, each index of a observation matches a location.
; [Door Printer CoffeeMachine PaperStack WaterTap CoffeeJar]
(def evaluation-obs [[1 0 0 0 0 0] [0 1 0 0 0 0] [0 1 0 0 0 0] [0 0 0 1 0 0] [0 0 0 1 0 0] [0 1 0 0 0 0] [1 1 0 0 0 0] [0 1 0 0 1 0] [0 1 0 0 1 0] [0 1 0 0 1 0] [0 0 0 0 1 1] [0 0 0 0 1 1] [0 0 0 0 1 1] [0 0 0 0 1 1] [0 0 0 0 1 1] [0 0 1 0 0 1] [0 0 1 0 0 0] [0 0 1 0 0 0] [0 0 1 0 0 0] [0 0 1 0 0 0] [0 0 1 0 0 0] [1 0 1 0 0 0] [0 0 1 0 0 0] [1 0 0 0 0 0] [0 0 0 0 0 0]]) 
 
(defn create-state-0 [n-person n-printjobs printer-jammed]
    "Returns the intial state.
     The state of the enviroment has following representation:
      [persons-loaction persons-items printjobs coffee-machine-water coffee-machine-coffee printer-paper printer-jammed]
        - persons-location: [loc_1 ... loc_n]   	
        - persons-items: [item_1 ... item_n]		
        - printjobs: [done?_1 ... done_m]
        - coffee-machine-water: true/false
        - coffee-machine-coffee: true/false
        - printer-paper: true/false
        - printer-jammed: true/false"
    (conj
        []
        (into [] (for [i (range n-person)] -1))
        (into [] (for [i (range n-person)] "Nothing"))
        (into [] (for [i (range n-printjobs)] false))
        false
        false
        false
        printer-jammed))    

(defm get-obs [state]
    "Returns the observation that matches the given state"
    (let [poss (nth state 0)
            compobs (loop [n 0
                obs [0 0 0 0 0 0]]
            (if (= n (count poss))
            obs
            ;obs[poss[i]]=1
            (recur (inc n) (if (not= (nth poss n) -1) (assoc obs (nth poss n) 1) obs))
            ))]
            (categorical [[compobs 1.0]])))


;; Define the query which returns the last state of a sequence of states
;; this query gets passed to the inference algorithm
(defquery abc_
    [state-0 observations]
      (if (= (count observations) 0)
       state-0
      (loop [statet state-0
             remobs observations]
            (let [obs (nth remobs 0)
                  state-dist (predict-states statet 5)
                  state-1 (sample state-dist)]
              (observe (get-obs state-1) obs)
              (if (= (count remobs) 1)
                state-1
                (recur state-1 (rest remobs)))))))


;; different inference methods    

; Sequential Monte Carlo
(defn sampler-smc [n-person n-printjobs printer-jammed n-obs query n-samples] 
    (doquery :smc query [(create-state-0 n-person n-printjobs printer-jammed) (take n-obs evaluation-obs)] :number-of-particles n-samples))
    
; Importance sampling(likelihood weighting)
(defn sampler-importance [n-person n-printjobs printer-jammed n-obs query n-samples]
    (doquery :importance query [(create-state-0 n-person n-printjobs printer-jammed) (take n-obs evaluation-obs)]))

; Lightweight Metropolis-Hastings
(defn sampler-lmh [n-person n-printjobs printer-jammed n-obs query n-samples]
    (doquery :lmh query [(create-state-0 n-person n-printjobs printer-jammed) (take n-obs evaluation-obs)]))

; Random-walk Lightweight Metropolis-Hastings
; uses default parameters
; :alpha - probability of using a local MCMC move (default: 0.5) 
; :sigma - spread of the local move (default: 1)
(defn sampler-rmh [n-person n-printjobs printer-jammed n-obs query n-samples]
    (doquery :rmh query [(create-state-0 n-person n-printjobs printer-jammed) (take n-obs evaluation-obs)]))

; Adaptive scheduling lightweight Metropolis-Hastings
(defn sampler-almh [n-person n-printjobs printer-jammed n-obs query n-samples]
    (doquery :almh query [(create-state-0 n-person n-printjobs printer-jammed) (take n-obs evaluation-obs)]))

; Particle Gibbs (iterated conditional SMC)
(defn sampler-pgibbs [n-person n-printjobs printer-jammed n-obs query n-samples]
    (doquery :pgibbs query [(create-state-0 n-person n-printjobs printer-jammed) (take n-obs evaluation-obs)] :number-of-particles n-samples))

; Particle independent Metropolis-Hastings
(defn sampler-pimh [n-person n-printjobs printer-jammed n-obs query n-samples]
    (doquery :pimh query [(create-state-0 n-person n-printjobs printer-jammed) (take n-obs evaluation-obs)] :number-of-particles n-samples))
    
; Particle Gibbs with ancestor sampling
(defn sampler-pgas [n-person n-printjobs printer-jammed n-obs query n-samples]
    (doquery :pgas query [(create-state-0 n-person n-printjobs printer-jammed) (take n-obs evaluation-obs)] :number-of-threads 2))
    
; Interacting particle Markov chain Monte Carlo
(defn sampler-ipmcmc [n-person n-printjobs printer-jammed n-obs query n-samples]
    (doquery :ipmcmc query [(create-state-0 n-person n-printjobs printer-jammed) (take n-obs evaluation-obs)] :number-of-threads 2))

(defn empirical-final-state [n-samples n-person n-printjobs printer-jammed n-obs sampler-string]
    "Returns an empirical distribution of final states.
     Takes n samples,
     creates an intial state,
     takes k observations from the observation sequence (25 observations total)."
    (s/empirical-distribution 
        (s/collect-results
            (take n-samples 
                (cond
                    (= sampler-string "importance") (sampler-importance n-person n-printjobs printer-jammed n-obs abc_ n-samples)
                    (= sampler-string "smc") (sampler-smc n-person n-printjobs printer-jammed n-obs abc_ n-samples)
                    (= sampler-string "lmh") (sampler-lmh n-person n-printjobs printer-jammed n-obs abc_ n-samples)
                    (= sampler-string "rmh") (sampler-rmh n-person n-printjobs printer-jammed n-obs abc_ n-samples)
                    (= sampler-string "almh") (sampler-importance n-person n-printjobs printer-jammed n-obs abc_ n-samples)
                    (= sampler-string "pgibbs") (sampler-pgibbs n-person n-printjobs printer-jammed n-obs abc_ n-samples)
                    (= sampler-string "pimh") (sampler-pimh n-person n-printjobs printer-jammed n-obs abc_ n-samples)
                    (= sampler-string "pgas") (sampler-pgas n-person n-printjobs printer-jammed n-obs abc_ n-samples)
                    (= sampler-string "ipmcmc") (sampler-ipmcmc n-person n-printjobs printer-jammed n-obs abc_ n-samples))))))



;;; main method and command line handling

(def usage-print "Usage: lein run number-samples number-persons number-printjobs printer-jammed?(true/false) number-obs(max is 25) inference-method")

(defn parse-int
    "Parses string to int."
    [n]
    (try (Integer/parseInt n)
        (catch NumberFormatException e
        (println (str \' n \') "is not a valid integer")
        (println usage-print)
        (System/exit 1))))

(defn parse-bool
    "Parses string to int."
    [b]
    (if (#{"true"} b)
        true
        (if (#{"false"} b)
        false
        (do (println (str \' b \') "is not a valid boolean. Use \"true\" or \"false\"")
            (println usage-print)
            (System/exit 1)))))

(defn -main [& args]  
 (if (= (first args) "-h")
     (println usage-print)
     (if (not= (count args) 6)
         (println usage-print)
         (try           
             (println (empirical-final-state (parse-int (first args)) (parse-int (nth args 1)) (parse-int (nth args 2))
                 (parse-bool (nth args 3)) (parse-int (nth args 4)) (nth args 5)))
         (catch Exception e
             (println e)
             (println usage-print)
             (System/exit 1))))))

