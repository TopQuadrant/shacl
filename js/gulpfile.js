var gulp = require('gulp');
var nodeunit = require('gulp-nodeunit');
var browserify = require('gulp-browserify');
var fs = require('fs');
var serve = require('gulp-serve');

gulp.task('test', function () {
    gulp.src('./test/**/*.js')
        .pipe(nodeunit({}));
});

gulp.task('browserify', function () {
    if (fs.existsSync('dist/index.js')) {
        fs.unlinkSync('dist/index.js');
    }
    if (fs.existsSync('dist/shacl.js')) {
        fs.unlinkSync('dist/shacl.js');
    }
    gulp.src('index.js')
        .pipe(browserify({
            standalone: 'SHACLValidator'
        }))
        .pipe(gulp.dest('dist'))
        .on('end', function () {
            fs.renameSync('dist/index.js', 'dist/shacl.js');
        });
});

gulp.task('generate-vocabularies', function () {
    var vocabularies = fs.readdirSync("./vocabularies");
    var acc = {};
    for (var i = 0; i < vocabularies.length; i++) {
        console.log("Generating " + vocabularies[i]);
        acc[vocabularies[i].split(".ttl")[0]] = fs.readFileSync("./vocabularies/" + vocabularies[i]).toString();
        fs.writeFileSync("./src/vocabularies.js", "module.exports = " + JSON.stringify(acc));
    }
});

gulp.task('browserify-public-tests', function () {
    if (fs.existsSync('public/test.js')) {
        fs.unlinkSync('public/test.js');
    }
    gulp.src('public/src/test.js')
        .pipe(browserify())
        .pipe(gulp.dest('public'));
});

gulp.task('generate-public-test-cases', function () {
    var testCases = [];

    if (!fs.existsSync(__dirname + "/public/data"))
        fs.mkdirSync(__dirname + "/public/data");

    fs.readdirSync(__dirname + "/test/data/core").forEach(function (dir) {
        fs.readdirSync(__dirname + "/test/data/core/" + dir).forEach(function (file) {
            var read = fs.readFileSync(__dirname + "/test/data/core/" + dir + "/" + file).toString();
            fs.writeFileSync(__dirname + "/public/data/" + dir + "_" + file, read);
            testCases.push("data/" + dir + "_" + file);
        });
    });

    fs.writeFileSync(__dirname + "/public/test_cases.json", JSON.stringify(testCases));
});


gulp.task('test-web', ['generate-public-test-cases', 'browserify-public-tests'], serve('public'));

gulp.task('default', ['test', 'browserify']);
