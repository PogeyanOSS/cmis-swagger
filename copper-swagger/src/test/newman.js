var newman = require("newman");
newman.run({
	collection : require("./SwaggerApiRequest.postman_collection.json"),
	reporters : "cli",
	globals : require("./NewWorkspace.postman_globals.json"),
	iterationCount : 1
}, function(err) {
	if (err) {
		throw err;
	}
	console.log("collection run completed!");
});