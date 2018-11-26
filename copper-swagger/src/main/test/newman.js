var newman = require("newman");
newman.run(
    {
        collection: require("./SwaggerTests.json"),
        reporters: "cli",
        globals: require("./globals.json"),
        iterationCount: 1
    },
    function (err) {
        if (err) {
            throw err;
        }
        console.log("collection run completed!");
    }
);