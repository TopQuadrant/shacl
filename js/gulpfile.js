var gulp = require('gulp');
var nodeunit = require('gulp-nodeunit');
var browserify = require('gulp-browserify');
var fs = require('fs');

gulp.task('test', function () {
    gulp.src('./test/**/*.js')
        .pipe(nodeunit({}));
});

gulp.task('browserify', function () {
    if (fs.existsSync('public/index.js')) {
        fs.unlinkSync('public/index.js');
    }
    if (fs.existsSync('public/shacl.js')) {
        fs.unlinkSync('public/shacl.js');
    }
    gulp.src('index.js')
        .pipe(browserify({
            standalone: 'SHACLValidator'
        }))
        .pipe(gulp.dest('public'))
        .on('end', function () {
            fs.renameSync('public/index.js', 'public/shacl.js');
        });
});

gulp.task('generate-vocabularies', function() {
    var vocabularies = fs.readdirSync("./vocabularies");
    var acc = {};
    for (var i=0; i<vocabularies.length; i++) {
        console.log("Generating " + vocabularies[i]);
        acc[vocabularies[i].split(".ttl")[0]] = fs.readFileSync("./vocabularies/" + vocabularies[i]).toString();
        fs.writeFileSync("./src/vocabularies.js", "module.exports = " + JSON.stringify(acc));
    }
});

gulp.task('default', ['test', 'browserify']);
