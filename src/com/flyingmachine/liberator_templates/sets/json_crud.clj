(ns com.flyingmachine.liberator-templates.sets.json-crud
  (:require [com.flyingmachine.liberator-templates.core :refer (deftemplates)]))


(defn add-errors
  [errors]
  [true {:errors errors
         :representation {:media-type "application/json"}}])

(defn errors-in-ctx
  [ctx]
  {:errors (get ctx :errors)
   :representation {:media-type "application/json"}})

(defn record-in-ctx
  [ctx]
  (get ctx :record))

(defn invalid?
  [action form]
  `[:malformed? ~form :handle-malformed errors-in-ctx])

(def new?
   {:show    false
    :query   false
    :update! false
    :create! true})

(def handlers {:show    :handle-ok
               :query   :handle-ok
               :update! :handle-ok
               :create! :handle-created})

(defn return
  [action form]
  `[:new? ~(new? action)
    :respond-with-entity? true
    ~(handlers action) ~form])

(def abbreviations {:invalid? invalid?
                    :return return})

(deftemplates
  {:paramlist [params]
   :decision-abbreviations abbreviations
   :shared-decisions [:available-media-types ["application/json"]]}
  (defquery)
  (defshow)
  (defcreate!
    :allowed-methods [:post])
  (defupdate!
    :allowed-methods [:put]
    :exists? record-in-ctx
    :can-put-to-missing? false)
  (defdelete!
    :allowed-methods [:delete]
    :exists? record-in-ctx))