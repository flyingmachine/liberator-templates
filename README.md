# Liberator Templates

Liberator Templates allows you to abstract patterns in your
[liberator][http://clojure-liberator.github.io/liberator/] resources
so that you can define resources more consistently and concisely.

## Installation

Include the following dependency in your `project.clj` file:

```clojure
[com.flyingmachine/liberator-templates "0.1.1"]
```

## Usage

`com.flyingmachine.liberator-templates.core/deftemplates` allows you
to specify:

* a default param list
* shared decisions
* "abbreviations"

### Default Param List

I've found it useful to define the same set of params for all my
resources:

```clojure
(deftemplates
  {:paramlist [params auth]}
  (defquery)
  (defshow))
```

The above creates two macros, `defquery` and `defshow`. Here's what
these macros produce:

```clojure
(defquery) ;=>
(liberator.core/defresource query [params auth])

;; You can define all your liberator decisions as well
(defquery
  :available-media-types ["application/json"]
  :handle-ok (fn [_] (magic-database/get-stuff)))
; =>
(liberator.core/defresource query
 [params auth]
 :available-media-types ["application/json"]
 :handle-ok (fn [_] (magic-database/get-stuff)))

;; defshow works the same way
(defshow) ;=>
(liberator.core/defresource show [params auth])
```

You can also override the params when you call each individual macro:

```clojure
;; If show doesn't check authentication:
(defshow [params]) ;=>
(liberator.core/defresource show [params])
```

### Shared Decisions

If all your resources use JSON and you're tired of writing
`:available-media-types ["application/json"]` for each one, then you
can do this:

```clojure
(deftemplates
  {:paramlist [params auth]
   :shared-decisions [:available-media-types ["application/json"]]}
  (defquery)
  (defshow))

(defquery) ;=>
(liberator.core/defresource query
 [params auth]
 :available-media-types ["application/json"])
```

### Abbreviations

This part's a bit weirder. In my own code, I was handling validation
by doing something like the following:

```clojure
(defn errors-in-ctx
  [ctx]
  (select-keys ctx [:errors]))

(defresource update!
  [params auth]
  :malformed? (fn [_] (if (not (valid? params))
                       [true {:errors {:blah "blah"}}]))
  :handle-malformed errors-in-ctx)
```

Abbreviations let you do this instead:

```clojure
(defupdate!
  :invalid? (fn [_] (if (not (valid? params))
                       [true {:errors {:blah "blah"}}])))
```

Here's a full example:

```clojure
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
  {:paramlist [params auth]
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

;; Calling a template with an abbreviation:
(defupdate!
  :invalid? (fn [_] (if (not (valid? params))
                     [true {:errors {:blah "blah"}}]))
  :authorized? (fn [_] (if auth
                        {:record (magic-database/find (:id params))}))
  :return record-in-ctx)

(liberator.core/defresource update! [params auth]
  :available-media-types ["application/json"]
  :allowed-methods [:put]
  :exists? record-in-ctx
  :can-put-to-missing? false
  :malformed? (fn [_] (if (not (valid? params)) [true {:errors {:blah "blah"}}]))
  :handle-malformed errorsin-ctx
  :authorized? (fn [_] (if auth {:record (magic-database/find (:id params))}))
  :new? false
  :respond-with-entity? true
  :handle-ok record-in-ctx)
```

So, abbreviations consist of two parts:

1. A map of the abbreviation to the function that expands it. In this
   case, we're mapping the `:invalid?` abbreviation to the `invalid`
   function. Likewise with `:return` / `return`.
2. Expansion functions which take the resource's name as a keyword and
   the form associated with the abbreviation, and return a seq of
   decisions.

Take `invalid?`. It actually ignores the resource name and just
returns a seq of

```clojure
`[:malformed? ~form :handle-malformed errors-in-ctx]
```

Notice the syntax quoting. This is necessary because the function is
being called to create a new macro.

## The json-crud set

Liberator Templates comes with one set of templates under
`com.flyingmachine.liberator-templates.sets.json-crud`. It's basically
the code snippet shown above.
