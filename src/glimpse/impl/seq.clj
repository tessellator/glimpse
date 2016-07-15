(ns glimpse.impl.seq)

(defn resize
  "Resizes a collection to be a specified size.

  If the collection is smaller than n, pad the collection with a provided value
  or nil."
  ([n coll]
   (resize n coll nil))
  ([n coll val]
   (take n (concat coll (repeat val)))))
