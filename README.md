# buzz

Asynchronous state management based on messages with pure functions.

Alpha state and undocumented.

[![CircleCI](https://circleci.com/gh/marcinwaldowski/buzz/tree/master.svg?style=shield)](https://circleci.com/gh/marcinwaldowski/buzz/tree/master)

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
