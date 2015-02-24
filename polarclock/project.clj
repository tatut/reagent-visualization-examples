(defproject polarclock "0.1.0-SNAPSHOT"
  :description "Reagent polarclock"
  :dependencies [[org.clojure/clojure "1.7.0-alpha5"]
                 [org.clojure/clojurescript "0.0-2913"]
                 [reagent "0.5.0-alpha3"]
                 [figwheel "0.2.5-SNAPSHOT"]]
  :plugins [[lein-cljsbuild "1.0.5"]
            [lein-figwheel "0.2.5-SNAPSHOT"]]

  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src"]
                        :compiler {:optimizations :none
                                   :source-map true
                                   :output-to "resources/public/polarclock.js"
                                   :output-dir "resources/public/out"}}]}


  )


