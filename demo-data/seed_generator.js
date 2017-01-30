// Script used to generate database input out of JSON files passed as arguments.

const util = require('util');
const path = require('path');
const fs = require('fs');

var dbIdSuffix = 'id';
var query = "INSERT INTO %s (SELECT * FROM json_populate_recordset(NULL::%s, '%s'));";

// This presents the order that must be kept for proper data insertion.
// Tables not included here are populated in random order
var filesOrdered = [
    "referencedata.geographic_levels",
    "referencedata.geographic_zones",
    "referencedata.facility_operators",
    "referencedata.facility_types",
    "referencedata.facilities",
    "referencedata.supervisory_nodes",
    "referencedata.processing_schedules",
    "referencedata.processing_periods",
    "referencedata.orderable_display_categories",
    "referencedata.orderables",
    "referencedata.programs",
    "referencedata.program_orderables",
    "referencedata.stock_adjustment_reasons",
    "referencedata.supply_lines",
    "referencedata.users",
    "referencedata.roles",
    "referencedata.rights",
    "referencedata.role_assignments",
    "referencedata.requisition_groups",
    "referencedata.requisition_group_program_schedules",
    "referencedata.supported_programs",
    "referencedata.requisition_group_members",
    "referencedata.role_rights",
]

// Detects if the given key/value pair represents a foreign key.
var isForeign = function(obj, key) {
    var val = obj[key];
    return val !== null && typeof val === 'object' && val.hasOwnProperty("id");
}

// Refractors the key/value pair to match database format.
var adjustForeign = function(obj, key) {
    var newKey = key.endsWith(dbIdSuffix) ? key : key + dbIdSuffix;
    var newVal = obj[key]["id"];

    obj[newKey] = newVal;
    if (newKey !== key) {
        delete obj[key];
    }
}

// Transforms object's key to lowercase
var keyToLower = function(obj, key) {
    if (key.toLowerCase() !== key) {
        var val = obj[key];
        delete obj[key];

        key = key.toLowerCase();
        obj[key] = val;
    }

    return key;
}

// Takes the input file and transforms it into sql query, inserted into output file
var parseInput = function(input, output) {
    // Insert into pending files and start processing
    var filename = path.parse(input).name;
    var array = JSON.parse(fs.readFileSync(input));

    // Transform JSON to match database format
    array.forEach(function(obj) {
        Object.keys(obj).forEach(function(key) {
            key = keyToLower(obj, key);

            if (isForeign(obj, key)) {
                adjustForeign(obj, key);
            }
        });
    });

    // Filename should match schema and table name
    var insert = util.format(query, filename, filename, JSON.stringify(array));
    fs.writeSync(output, insert + "\n");
}

// Start processing
var fd = fs.openSync('input.sql', 'w');
var filesPending = {};
var filesQueue = [];

// Group the files into dictionary
process.argv.slice(2).forEach(function(file) {
    var filename = path.parse(file).name;
    filesPending[filename] = file;
});

// First enqueue ordered files
filesOrdered.forEach(function(key) {
    if (filesPending[key]) {
        filesQueue.push(filesPending[key]);
        delete filesPending[key];
    }
});

// Enqueue remaining files
for (key in filesPending) {
    filesQueue.push(filesPending[key]);
};

// Process the files
filesQueue.forEach(function(file) {
    console.log("Processing: " + path.parse(file).name);
    parseInput(file, fd);
});

fs.closeSync(fd);
