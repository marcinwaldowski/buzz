# buzz

Buzz is a library which helps building Clojurescript [SPA](https://en.wikipedia.org/wiki/Single-page_application) with approach similar to the one described in [Elm Architecture](https://guide.elm-lang.org/architecture/). It focuses only on state management - rendering of view has to be implemented with another library e.g. [Rum](https://github.com/tonsky/rum) or [Reagent](https://github.com/reagent-project/reagent).

It is inspired by:
- [Elm Architecture](https://guide.elm-lang.org/architecture/),
- [Petrol](https://github.com/krisajenkins/petrol) and
- [Re-frame](https://github.com/Day8/re-frame).

[![CircleCI](https://circleci.com/gh/marcinwaldowski/buzz/tree/master.svg?style=shield)](https://circleci.com/gh/marcinwaldowski/buzz/tree/master)

## Documentation

### API

See [API Docs](https://marcinwaldowski.github.io/buzz/)

### Guide

#### 1

To use buzz create Clojurescript or Clojure project and add the following dependency to it:

```clojure
[buzz 0.1.0]
```

To start working with buzz, require the `buzz.core` at the REPL:

```clojure
(require '[buzz.core :as buzz])
```

or include buzz in your namespace:

```clojure
(ns my.ns
  (:require [buzz.core :as buzz]))
```

### 2

Buzz manages state represented as single atom. In order to create buzz you have to provide:
- state (as atom),
- handle function and
- execute function.

```clojure
(def state (atom 0))  ; state, here our state is just a number
(declare handle-msg)  ; handle function
(declare execute-cmd) ; execute function

(def b (buzz/buzz state #'handle-msg #'execute-cmd))
```

Lets ignore `execute-cmd` (execute function) for now and provide `handle-msg` (handle function) only.

*Handle function* must accept two arguments:
- current state value (it is the current value of the atom, not atom itself) and
- message (which is what you `put!` to buzz - it can be any anything except `nil`)

and must produce vector of one or two elements:

- next value of your state (obligatory) and
- command to execute (optional, `nil` command is treated as no command).

Because command is optional we will skip it for now. Lets assume that our message is just a number. Following *handle function* produces next state value by just adding message to its previous value:

```clojure
(defn handle-msg
  [state msg]
  [(+ state msg)])
```

Check it by putting some messages (numbers) to buzz:

```clojure
(buzz/put! b 1)
(buzz/put! b 2)
(buzz/put! b 4)
```

and inspect the value of state:

```clojure
@state  ; => 7
```

### 3

To explain command execution more complex example is needed. Lets have two types of messages:

```clojure
[::inc 5]               ; increase state by 5
[::inc-delayed 6 10000] ; increase state by 6 after 10000 milliseconds
```

Every message here is a vector and we identify its type by first element. To easily dispatch handling of different messages we will declare *handle function* as multimethod. Please note that this is not enforced by buzz, you can use any dispatch technique provided by ClojureScript. Lets create buzz with handle and execute functions as multimethod.

```clojure
(def state (atom 0))

;; Handle function as multimethod.
(defmulti handle-msg
  (fn [curr-state-val msg]
    (first msg)))

;; Execute function as multimethod, we will use it later.
(defmulti execute-cmd
  (fn [cmd]
    (first cmd)))

(def b (buzz/buzz state handle-msg execute-cmd))
```

We will handle `::inc` message same way as in previous example:

```clojure
(defmethod handle-msg ::inc
  [state [msg-type inc-val]] ; message destructuring in function arguments
  [(+ state inc-val)])
```

In `::inc-delayed` we return same state (we don't want to change it) and command, which is just data vector similar to our messages.

```clojure
(defmethod handle-msg ::inc-delayed
  [state [_ inc-val delay]]
  [state [::inc-after-delay inc-val delay]])
```
Commands execution is responsibility of *execute function*. *Execute function* is called right after *handle function* (if the later returned a command) and its only job is to return new core.async channel based on command. This channel must later produce new message (or messages) for handle function. Following implementation for `::inc-after-delay` command returns channel which provides `::inc` message after some delay.

```clojure
(defmethod execute-cmd ::inc-after-delay
  [[_ inc-val delay]]
  (async/go                           ; go block returns channel
    (async/<!! (async/timeout delay))
    [::inc inc-val]))
```

Lets check it:

```clojure
(buzz/put! b [::inc 5])
@state ; => 5

(buzz/put! b [::inc-delayed 6 10000])
@state ; => 5

;; after 10 seconds
@state ; => 11
```

### 4

The general idea of buzz is to:
- put messages to buzz from listeners of view elements,
- change application state in *handle function* - this function is pure therefore you should maximize the amount of message types,
- perform all actions which require time or initialize reading messages from external systems in *execute function* (eg. calling REST API or opening websockets for reading messages) - this function is not pure therefore you should minimize the amount of command types,
- render view based on state using chosen library.

## Development

### Installation

1. Install [Leiningen](https://leiningen.org/#install).
2. Install [Node.js](https://nodejs.org/en/download/package-manager/).
3. [Fix npm permissions](https://docs.npmjs.com/getting-started/fixing-npm-permissions).
3. Clone this git repository and `cd` to it.
4. Execute commands:

   ```sh
   lein npm install
   npm install -g karma-cli
   ```

### Running tests

1. Run tests in Clojure.

   ```sh
   lein test
   ```

2. Run tests in Chrome, Firefox or both browsers.

   ```sh
   lein doo chrome test once
   lein doo firefox test once
   lein doo browsers test once
   ```

## License

Copyright © 2017 Marcin Waldowski

Distributed under the [MIT License](http://opensource.org/licenses/MIT).
