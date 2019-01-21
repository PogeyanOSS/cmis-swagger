var newman = require("newman");
newman.run(
    {
        collection: require("./SwaggerApiRequest.postman_collection.json"),
        reporters: ["cli", "html"],
        globals: require("./globals.json"),
        iterationCount: 1,
        reporter: {
            html: {
            export: "./testResult.html"
            }
            }
    },
    function (err) {
        if (err) {
            throw err;
        }
        console.log("Collection run completed!");
    }
);