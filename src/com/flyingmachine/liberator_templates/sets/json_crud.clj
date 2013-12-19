(ns com.flyingmachine.liberator-templates.sets.json-crud
  (:require [com.flyingmachine.liberator-templates.core :refer (deftemplates)]))

(deftemplates
  {:paramlist [params auth]
   :decision-abbreviations expanders
   :shared-decisions {:available-media-types ["application/json"]}}
  (defshow)
  (defcreate!
    :allowed-methods [:post])
  (defupdate!
    :allowed-methods [:put]
    :exists? nil)
  (defcreate!
    :allowed-methods [:delete]
    :exists? nil))