var newman = require("newman");
const global = require("./globals.js").globals;
const globalJson = JSON.parse(JSON.stringify(global));
newman.run(
    {
        collection: require("./SwaggerApiRequest.postman_collection.json"),
        reporters: ["cli", "html"],
        globals: globalJson,
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