# dotty-bot

[![Build Status](https://travis-ci.org/lampepfl/dotty-bot.svg?branch=master)](https://travis-ci.org/lampepfl/dotty-bot)

```shell
$ ssh dotty@scala-webapps.epfl.ch
```

Internal address: http://scala-webapps.epfl.ch:48080/api/rate

Architecture is based on http4s and circe for JSON serialization. Should be
pretty straight forward to make changes.


## Running the test suite
To run the test suite, you need to start sbt with these env vars:

```shell
$ GITHUB_USER=dotty-bot \
    GITHUB_TOKEN=<github token> \
    DRONE_TOKEN=<drone token> \
    GITHUB_CLIENT_ID=<github client id> \
    GITHUB_CLIENT_SECRET=<github client secret> \
    sbt
```

There are a bunch of tests that make sure that we're wired up correctly:

```shell
> test
```

Please note that you have to start sbt with the above args, otherwise you'll
get exceptions in the testing telling you to specify them.

## Deploying
```shell
$ git clone https://github.com/lampepfl/dotty-bot.git
$ cd dotty-bot
$ sbt assembly
$ HOST=128.178.154.101 \
    PORT=48080 \
    GITHUB_USER=dotty-bot \
    GITHUB_TOKEN=<github token> \
    DRONE_TOKEN=<drone token> \
    GITHUB_CLIENT_ID=<github client id> \
    GITHUB_CLIENT_SECRET=<github client secret> \
    java -jar target/scala-2.12/dotty-bot-assembly-0.1-SNAPSHOT.jar
```
