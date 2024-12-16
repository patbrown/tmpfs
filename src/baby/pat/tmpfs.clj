(ns  baby.pat.tmpfs
    (:require [babashka.fs :as fs]
              [baby.pat.jes.vt :as vt]
              [clojure.java.io :as io]
              [clojure.java.shell]
              [clojure.spec.alpha :as s]
              [clojure.string :as string]
              [orchestra.core :refer [defn-spec]]
              [baby.pat.jes.vt.util :as u])
    (:import [java.io File]
             [java.nio.file Files Path Paths]
             [java.nio.file.attribute FileAttribute]))

(s/def ::vt/tmpdir #(u/type-of? % "TmpDir"))

(defn-spec tmpdir-naming-fn ::vt/str []
  (str (u/rand-of :female-names) "-" (u/rand-of :nato-alphabet)))

(defn-spec tmpfile-naming-fn ::vt/str []
  (str (u/rand-of :colors) "-" (u/rand-of :nationalities) "-" (u/rand-of :greek-alphabet)))

(defprotocol ITmpDir
    (create [this])
    (destroy [this])
    (archive [this])
    (create-file [this] [this extension] [this filename extension])
    (destroy-file [this file]))

  (def default-temp-dir-config
  {:root "bin/resources/tmp"
   :dir-prefix (fn [] (tmpdir-naming-fn))
   :file-prefix (fn [] (tmpfile-naming-fn))
   :archive-dir "bin/resources/tmp-archives"
   :extension ".edn"
   :archive-with (fn [{:keys [root dir archive-dir] :as this}]
                   (fs/copy-tree @dir (str archive-dir "/" (fs/relativize root (fs/file @dir)))))})

(defrecord TmpDir [id root dir-prefix file-prefix dir files extension archive-with]
  ITmpDir
  (create [{:keys [id] :as this}]
    (when-not (fs/exists? root)
      (fs/create-dirs root))
    (->> (Files/createTempDirectory
          (Paths/get root (into-array String []))
          (str id "_XXX_")
          (into-array FileAttribute []))
         (reset! dir)))
  (create-file [this]
    (create-file this extension (file-prefix)))
  (create-file [this extension]
    (create-file this extension (file-prefix)))
  (create-file [this extension file-prefix]
    (let [file ^File (File/createTempFile (str file-prefix "_XXX_")
                                          extension
                                          (io/file (str @dir)))
          path (fs/path file)
          parent (fs/parent file)
          nm (fs/file-name file)
          file-map {:file-prefix file-prefix
                    :filename nm
                    :parent parent
                    :file file}
          _ (swap! files assoc nm file-map)]
      file-map))
  (destroy-file [this filename]
    (let [{:keys [file]} (get @files filename)
          _ (swap! files dissoc filename)]
      (fs/delete-if-exists file)))
  (destroy [this]
    (when (fs/exists? (str @dir))
      (-> (str @dir)
          io/file
          fs/delete-tree)))
  (archive [this]
    (archive-with this)))

(defn-spec *tmpdir ::vt/tmpdir
  "Takes a config map and returns a function that takes another config map and makes a supatom."
  [overlay-config ::vt/map]
  (fn [config]
    (map->TmpDir (merge default-temp-dir-config overlay-config config))))

(defn-spec tmpdir-> ::vt/tmpdir
  "Simplest supatom creation function."
  ([] ((*tmpdir (let [nm (tmpdir-naming-fn)]
                  {:id nm
                   :dir (atom nil)
                   :files (atom {})})) {}))
  ([overlay-config ::vt/map] ((*tmpdir {:id (tmpdir-naming-fn)
                                        :dir (atom nil)
                                        :files (atom {})}) overlay-config)))

(defn-spec clean-tmp ::vt/any []
  (fs/delete-tree "bin/resources/tmp")
  (fs/create-dirs "bin/resources/tmp"))

;; ;;(create pat)
;; ;; (archive pat)
;; pat
;; (archive-file pat )
;; fat
;; ;; (def pat (tmpdir->))
;;  (def fat (tmpdir->))
;; ;; (create pat)
;; ;; (create-file pat)
;; ;; (destroy pat)

#_(archive-file pat
              (-> pat :files deref vals )
              )

;;  (fs/relativize "bin/resources/tmp" (-> pat :files deref vals first :file))
