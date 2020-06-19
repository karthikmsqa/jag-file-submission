module.exports = {
  env: {
    test: {
      presets: ["@babel/preset-env", "@babel/preset-react"],
      plugins: ["transform-export-extensions"],
      only: ["./**/*.js", "node_modules/jest-runtime"]
    }
  }
};
