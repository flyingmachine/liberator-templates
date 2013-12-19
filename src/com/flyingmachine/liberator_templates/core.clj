(ns com.flyingmachine.liberator-templates.core
  (:require [liberator.core :refer (defresource)]))

(def expanders {})

(defn expand
  [action [key form]]
  (if-let [expander (get expanders key)]
    (expander action form)
    [key form]))

(defn resource-name
  [template-name]
  (symbol (clojure.string/replace (str template-name) #"def" "")))

(defn paramlist
  [shared-paramlist decisions]
  (let [maybe-paramlist (first decisions)]
    (if (vector? maybe-paramlist)
      maybe-paramlist
      shared-paramlist)))

(defn expand-decisions
  [resource-name decisions]
  (if-not (empty? decisions)
    (reduce into (map #(expand (keyword resource-name) %)
                      (partition 2 decisions)))))

(defn deftemplate
  [shared-options template-name & template-decisions]
  `(defmacro ~template-name
     [& decisions#]
     (let [paramlist# (paramlist (quote ~(:paramlist shared-options)) decisions#)
           resource-name# (quote ~(resource-name template-name))]
       `(defresource ~resource-name# ~paramlist#
          ~~@(:shared-decisions shared-options)
          ~~@template-decisions
          ~@(expand-decisions resource-name# decisions#)))))

(defmacro deftemplates
  [options & templates]
  `(do ~@(map #(apply deftemplate options %) templates)))

(deftemplates
  {:paramlist [params auth]
   :decision-abbreviations expanders
   :shared-decisions [:available-media-types ["application/json"]]}
  (defshow :allowed-methods [:get]))