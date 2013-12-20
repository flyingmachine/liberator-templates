(defproject com.flyingmachine/liberator-templates "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]]
  :profiles {:dev {:dependencies [[liberator "0.10.0"]
                                  [midje "1.5.0"]
                                  [ring-mock "0.1.4"]
                                  [ring "1.2.1"]
                                  [ring-middleware-format "0.3.0"]
                                  [com.flyingmachine/webutils "0.1.6"]]}})
