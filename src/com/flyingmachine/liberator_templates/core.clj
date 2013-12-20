(ns com.flyingmachine.liberator-templates.core
  (:require [liberator.core :refer (defresource)]))

(defn resource-name
  [template-name]
  (symbol (clojure.string/replace (str template-name) #"def" "")))

(defn paramlist
  "Each template can maybe start with a param list. If it does, use that."
  [shared-paramlist decisions]
  (let [maybe-paramlist (first decisions)]
    (if (vector? maybe-paramlist)
      maybe-paramlist
      shared-paramlist)))

(defn extract-paramlist
  [shared-paramlist decisions]
  (let [maybe-paramlist (first decisions)]
    (if (vector? maybe-paramlist)
      [maybe-paramlist (rest decisions)]
      [shared-paramlist decisions])))

(defn expand-decision
  [action abbreviations [key form]]
  (if-let [expander (get abbreviations key)]
    (expander action form)
    [key form]))

(defn expand-decisions
  [abbreviations resource-name decisions]
  (if-not (empty? decisions)
    (reduce into (map #(expand-decision (keyword resource-name) abbreviations %)
                      (partition 2 decisions)))))

(defn deftemplate
  [shared-options template-name & template-decisions]
  `(defmacro ~template-name
     [& decisions#]
     (let [[paramlist# decisions#] (extract-paramlist (quote ~(:paramlist shared-options)) decisions#)
           decision-abbreviations# ~(:decision-abbreviations shared-options)
           resource-name# (quote ~(resource-name template-name))]
       `(defresource ~resource-name# ~paramlist#
          ~~@(:shared-decisions shared-options)
          ~~@template-decisions
          ~@(expand-decisions decision-abbreviations# resource-name# decisions#)))))

(defmacro deftemplates
  [options & templates]
  `(do ~@(map #(apply deftemplate options %) templates)))
