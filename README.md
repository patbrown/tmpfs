# tmpfs

[![bb compatible](https://raw.githubusercontent.com/babashka/babashka/master/logo/badge.svg)](https://babashka.org)
[![Clojars Project](https://img.shields.io/clojars/v/baby.pat/tmpfs.svg)](https://clojars.org/baby.pat/tmpfs)

Configure temporary filesystems with maps in CLJ/BB

___
[<img src="resources/tmp.png" alt="fw" width="400px">](https://tmp.pat.baby)

I create temporary dirs and files using a TmpDir record using the ITmpDir protocol. There are five methods, `create`, `destroy`, and `archive` for dirs. As well as `create-file` and `destroy-file` for files.   
The magic comes for a map that defines how the tmpdir behaves. Your map goes on top of `default-temp-dir-config` which has the keys (root, dir-prefix, file-prefix, archive-dir, extension, and archive-with). This is all subject to change. Use at peril, because I'm not fully comfortable with what I do and don't want as my methods.
## Installation

```clojure
baby.pat/tpl {:mvn/version "0.0.2"}
```
## Usage

```clojure
(def love (tmpdir->))
(create love)
(create-file love)
(archive love)
(destroy love)

```
