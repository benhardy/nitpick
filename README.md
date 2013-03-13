nitpick
=======

nitpick code review tool# Nitpick #

## Build & Run in dev mode ##

### Configuration

Create a file called runtime.properties somewhere outside of the cloned
project. This is used to specify where you want Nitpick to store things.

Currently only one property is required, the review checkout directory.
This is where repos being cloned for reviews will be saved.
In production you'll want to do somewhere with a good bit of space but
for single user dev, /tmp might be OK for you. Put whatever you like, e.g.:

review.checkout.directory=/tmp


# Running continuously via sbt

Same as any other Scalatra app but tell is where the runtime properties file
you just made is located, e.g.:

```sh
$ cd $name__snake$
$ ./sbt  -Druntime.properties.file=/home/me/runtime.properties
> container:start
> ~ ;test;copy-resources;aux-compile
```

Now open the site's [root page](http://localhost:8080/) in your browser.

## Generate/view test coverage report

```sbt jacoco:cover
```

Then just open the following file (relative to project directory) in browser.

```target/scala-2.10/jacoco/html/index.html
```