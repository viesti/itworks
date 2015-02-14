(ns itworks.core
  (:require [clojure.browser.repl :as repl]
            [schema.core :as s]))

;; (repl/connect "http://localhost:9000/repl")

(enable-console-print!)

(println "Hello world!")

(do
  (defrecord Recursive [derefable]
    s/Schema
    (walker [this]
      (let [a (atom nil)]
        (reset! a (s/start-walker
                   (let [old s/subschema-walker]
                     (clojure.core/fn [s] (if (= s this) #(@a %) (old s))))
                   @derefable))))
    (explain [this]
      (let [{:keys [ns name]} (meta derefable)]
        (list 'recursive (str ns "/" name)))))

  (defn recursive
  "Support for (mutually) recursive schemas by passing a var that points to a schema,
  e.g (recursive #'ExampleRecursiveSchema)."
  [schema]
  (Recursive. schema)))

(def BinaryTree
  (s/maybe
   {:value s/Int
    :left (recursive #'BinaryTree)
    :right (recursive #'BinaryTree)}))

(println "empty tree:" (s/check BinaryTree nil))
(println "correct tree:" (s/check BinaryTree {:value 4 :right {:value 2 :left nil :right nil} :left nil}))

(println "broken tree" (s/check BinaryTree {:value "4" :right {:value 2 :left nil :right nil} :left nil}))
(println (s/explain BinaryTree))
