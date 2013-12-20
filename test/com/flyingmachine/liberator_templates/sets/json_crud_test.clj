(ns com.flyingmachine.liberator-templates.sets.json-crud-test
  (:require [com.flyingmachine.liberator-templates.sets.json-crud :refer :all]
            [flyingmachine.webutils.utils :refer (defnpd str->int)]
            [ring.mock.request :refer (request header content-type)]
            [clojure.data.json :as json]
            [ring.middleware.params :refer :all]
            [ring.middleware.keyword-params :refer :all]
            [ring.middleware.nested-params :refer :all]
            [ring.middleware.session :refer :all]
            [ring.middleware.format :refer :all]
            [compojure.core :as compojure.core :refer (GET PUT POST DELETE ANY defroutes)])
  (:use midje.sweet))


(defn validator
  [params]
  (fn [_]
    (let [id (str->int (:id params))]
      (cond
       (= id 1) (add-errors {:id "bad id!"})
       (= id 2) false
       (= id 3) [false {:record {:id 3}}]))))

(defshow
  :return (fn [_] {:id (str->int (:id params))}))

(defquery
  :return [{:id 1}])

(defupdate!
  [params auth]
  :invalid? (validator params)
  :return record-in-ctx)

(defcreate!
  :invalid? (validator params)
  :return record-in-ctx)

(defdelete!
  :invalid? (validator params))

(defroutes routes
  (GET "/entities" {params :params} (query params {}))
  (GET "/entities/:id" {params :params} (show params {}))
  (PUT "/entities/:id" {params :params} (update! params {}))
  (DELETE "/entities/:id" {params :params} (delete! params {}))
  (POST "/entities" {params :params} (create! params {})))

(def app
  (-> routes
      (wrap-restful-format :formats [:json-kw])
      wrap-keyword-params
      wrap-nested-params
      wrap-params))

(defnpd req
  [method path [params nil] [auth nil]]
  (-> (request method path params)
      (content-type "application/json")))

(defnpd res
  [method path [params nil] [auth nil]]
  (let [params (json/write-str params)]
       (app (req method path params auth))))

(defn data
  [response]
  (-> response
      :body
      json/read-str))

(defnpd response-data
  [method path [params nil] [auth nil]]
  (data (res method path params auth)))

(fact "show returns the id"
  (response-data :get "/entities/1")
  => (contains {"id" 1}))

(fact "query returns stuff"
  (response-data :get "/entities")
  => (contains [{"id" 1}]))

(facts "about update"
  (fact "returns invalid code for errors"
    (res :put "/entities/1")
    => (contains {:status 400}))
  (fact "returns the errors, too"
    (response-data :put "/entities/1")
    => (contains {"errors" {"id" "bad id!"}}))
  (fact "returns not implemented code when validation doesn't return record"
    (res :put "/entities/2")
    => (contains {:status 501}))
  (fact "returns 200 on successful update"
    (res :put "/entities/3")
    => (contains {:status 200}))
  (fact "returns record on successful update"
    (response-data :put "/entities/3")
    => (contains {"id" 3})))

(facts "about create"
  (fact "returns invalid code for errors"
    (res :post "/entities" {:id 1})
    => (contains {:status 400}))
  (fact "returns the errors, too"
    (response-data :post "/entities" {:id 1})
    => (contains {"errors" {"id" "bad id!"}}))
  (fact "returns 201 on successful post"
    (res :post "/entities" {:id 3})
    => (contains {:status 201}))
  (fact "returns record on successful post"
    (response-data :post "/entities" {:id 3})
    => (contains {"id" 3})))

(facts "about delete"
  (fact "returns invalid code for errors"
    (res :delete "/entities/1")
    => (contains {:status 400}))
  (fact "returns the errors, too"
    (response-data :delete "/entities/1")
    => (contains {"errors" {"id" "bad id!"}}))
  (fact "returns 404 when entity doesn't exist"
    (res :delete "/entities/2")
    => (contains {:status 404}))
  (fact "returns 204 on successful delete"
    (res :delete "/entities/3")
    => (contains {:status 204})))