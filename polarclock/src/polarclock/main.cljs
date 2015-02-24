(ns polarclock.main
  (:require [reagent.core :refer [atom] :as r]
            [figwheel.client :as fw]))

(defn polar->cartesian [cx cy radius angle-deg]
  (let [rad (/ (* js/Math.PI (- angle-deg 90)) 180.0)]
    [(+ cx (* radius (js/Math.cos rad)))
     (+ cy (* radius (js/Math.sin rad)))]))

  
(defn arc [cx cy r db de color width]
  (let [[ax ay] (polar->cartesian cx cy r db)
        [lx ly] (polar->cartesian cx cy r de)
        large? (if (< (- de db) 180) 0 1)
        dir? 1]
    [:path {:d (str "M " ax " " ay " "
                    "A" r " " r " 0 " large? " " dir? " " lx " " ly)
            :fill "none"
            :stroke color
            :stroke-width width}]))

(defn arc-text [cx cy r ang text]
  (let [[x y] (polar->cartesian cx cy r ang)]
    [:text {:x x :y y :text-anchor "middle"
            :transform (str "rotate(" ang " " x "," y ")")} text]))

(defonce time (let [a (atom (js/Date.))]
                (js/setInterval #(reset! a (js/Date.)) 1000)
                a))


(defn pad2 [s]
  (let [s (str s)]
    (if (< (count s) 2)
      (str "0" s)
      s)))

(defn clock []
  (let [t @time
        h (.getHours t)
        m (.getMinutes t)
        s (.getSeconds t)]
    [:span
     [:svg {:width 500 :height 500}

      ;; tuntiviisari
      [arc 250 250 90 0 (* 15 h) "green" 30]
      [arc-text 250 250 88 (- (* 15 h) 7) (str h)]

      ;; minuuttiviisari
      [arc 250 250 140 0 (* 6 m) "red" 30]
      [arc-text 250 250 138 (- (* 6 m) 5) (str m)]

      ;; sekuntiviisari
      [arc 250 250 190  0 (* 6 s) "blue" 30]
      [arc-text 250 250 186 (- (* 6 s) 4) (str s)] ;210

      ;; piirretään vielä normaali kellonaika
      [:text {:x 250 :y 250 :text-anchor "middle"}
       (str (pad2 h) ":" (pad2 m) ":" (pad2 s))]
      ]
     (* 6 s)]))


    
(defn #^:export main []
  (r/render [clock] (.getElementById js/document "polarclockapp")))

(fw/start {:on-jsload (fn []
                        (.log js/console "Uunituoretta koodia.")
                        (main))})
