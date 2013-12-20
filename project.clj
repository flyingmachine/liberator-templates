(defproject com.flyingmachine/liberator-templates "0.1.0"
  :description "Create macros which create liberator resources"
  :url "https://github.com/flyingmachine/liberator-templates"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]]
  :profiles {:dev {:dependencies [[liberator "0.10.0"]
                                  [midje "1.5.0"]
                                  [ring-mock "0.1.4"]
                                  [ring "1.2.1"]
                                  [ring-middleware-format "0.3.0"]
                                  [com.flyingmachine/webutils "0.1.6"]]}})
