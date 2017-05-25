# buzz

Asynchronous state management based on messages with pure functions.

Alpha state and undocumented.

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

2. Run tests in Chrome browser.

  ```sh
  lein doo chrome test once
  ```

2. Run tests in Firefox browser.

  ```sh
  lein doo firefox test once
  ```

## License

Copyright © 2017 Marcin Waldowski

Licensed under the Apache License Version 2.0
