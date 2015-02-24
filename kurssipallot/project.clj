(defproject kurssipallot "0.0.1-SNAPSHOT"
  :description "Reagent kurssipallot"
  
  :dependencies [[org.clojure/clojure "1.7.0-alpha5"]
                 [org.clojure/clojurescript "0.0-2913"]

                 [lively "0.2.0"] 
                 [reagent "0.5.0-alpha3" :exclusions [[cljsjs/react :classifier "*"]]]
                 [cljsjs/react-with-addons "0.12.2-7"]
                 
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]

                 [figwheel "0.2.5-SNAPSHOT"]
                 ]
  
  :plugins [[lein-cljsbuild "1.0.5"]
            [lein-figwheel "0.2.5-SNAPSHOT"]
            ]
  
  :cljsbuild {
              :builds [{:id "dev"
                        :source-paths ["src"]
                        :compiler {:optimizations :none
                                   :source-map true
                                   ;;:preamble ["reagent/react.js"]
                                   :output-to "resources/public/visu.js"
                                   :output-dir "resources/public/out"}
                        }
                       {:id "prod"
                        :source-paths ["src"]
                        :compiler {:optimizations :advanced
                                   :output-to "visu.js"
                                   }
                        }
                       ]}

  :clean-targets #^{:protect false} ["resources/public/out"
                                     "resources/public/visu.js"]

  :figwheel {:http-server-root "public"
             :server-port 3449}

  )
