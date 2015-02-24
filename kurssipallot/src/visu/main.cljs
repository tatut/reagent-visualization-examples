(ns visu.main
  (:require [cljs.core.async :refer [<! >! timeout chan]]
            [reagent.core :refer [atom] :as r]
            [clojure.string :as str]
            [figwheel.client :as fw])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defonce kurssit (atom []))

(comment (add-watch kurssit ::debug
           (fn [_ _ old new]
             (.log js/console "kurssit: " (pr-str old) "\n=>\n" (pr-str new)))))


(defn animoi "Apuri arvon animoimiseksi"
  ([alku loppu kesto paivita-fn]
     (animoi alku loppu kesto paivita-fn nil))
  ([alku loppu kesto paivita-fn valmis-fn]
     (let [alkuaika (js/Date.now)
           loppuaika (+ alkuaika kesto)
           arvovali (- loppu alku)]
       (go (loop [aika (js/Date.now)]
             (if (>= aika loppuaika)
               ;; Animaatio loppu: päivitä viimeinen arvo ja kutsu optionaalista valmis callbackai
               (do (paivita-fn loppu)
                   (when valmis-fn
                     (valmis-fn)))

               ;; Animoidaan vielä. Tähän voisi käyttää eri interpolointeja hienommaksi animaatioksi
               ;; esim. bardo kirjastoa: https://github.com/pleasetrythisathome/bardo
               (do
                 (paivita-fn (+ alku (* (/ (- aika alkuaika) kesto) arvovali)))
                 (<! (timeout 5))
                 (recur (js/Date.now)))))))))
               
(defn poista-kurssi
  "Poistaa kurssin: animoi ensin opacityn nollaan ja sitten poistaa."
  [id]
  (animoi 1.0 0.0 3000
          (fn [p]
            (swap! kurssit (fn [kurssit]
                             (mapv #(if (= id (:courseid %))
                                      (assoc % :opacity p)
                                      %)
                                   kurssit))))
          (fn []
            (swap! kurssit (fn [kurssit]
                             (filter #(not= (:courseid %) id) kurssit))))))

(defn kurssi
  "Yksittäinen kurssi: SVG ympyrä, jota klikkaamalle poistetaan. Mukana myös hover tooltip."
  [{:keys [name courseid participants opacity x y ]}]
  (let [hover (atom false)]
    (fn [{:keys [name courseid participants opacity x y ]}]
      [:g
       [:circle {:cx x :cy y :r participants
                 :stroke "yellow"
                 :stroke-width 3
                 :fill "blue"
                 :fill-opacity (or opacity 1.0)
                 :on-mouse-over #(reset! hover true)
                 :on-mouse-out #(reset! hover false)
                 :on-click #(poista-kurssi courseid)}]
       (when @hover
         [:text {:x (- x (/ participants 2)) :y (+ 15 y participants)} name])])))

(defn visu
  "Visualisaation pääkomponentti: SVG elementti ja kaikki kurssit"
  []
  [:svg {:width 960 :height 500 :style {:background-color "white"}}
   (for [k @kurssit]
     ^{:key (:courseid k)}
     [kurssi k])])



(defn ^:export paivita
  "Päivittää uudet datat sivun textarean CSV tekstistä. Ei mitään virhetarkistuksia."
  []
  (let [rivit (-> js/document
                  (.getElementById "kurssidata")
                  .-value
                  (str/split "\n"))
        otsikko-rivi (first rivit)
        kurssi-rivit (filter #(not (str/blank? %)) (rest rivit))
        kentat (map #(keyword (str/lower-case (str/trim %)))
                    (str/split otsikko-rivi ","))]
    (reset! kurssit (vec (map-indexed (fn [i rivi]
                                        (let [d (zipmap kentat
                                                        (map str/trim (str/split rivi ",")))]
                                          (assoc d :participants (js/parseInt (:participants d))
                                                 :x (+ 77 (* 100 i))
                                                 :y (+ 77 (* 50 i)))))
                                      kurssi-rivit)))))

    
        
(defn ^:export main []
  (paivita)
  (r/render [visu] (.getElementById js/document "visuapp"))

  ;; Sekunti startin jälkeen, animoidaan vähän lisäosallistujia 1. kurssiin
  (js/setTimeout (fn []
                   (animoi (get-in @kurssit [0 :participants])
                           99
                           750
                           (fn [p] (swap! kurssit #(assoc-in % [0 :participants] p)))))
                 1000))

(fw/start {:websocket-url   "ws://localhost:3449/figwheel-ws"
           :on-jsload (fn [] (.log js/console "Aaah... uutta koodia!"))})
