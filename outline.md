# Outline

* Intro
    * Three unrelated things
    * Motivation
        * Describe decision engine
        * Describe testing problem

* Docker
    * Show apps running in Docker
        * `docker run --rm -p8080:8080 rulesapp-compiler`
        * `docker run --rm -p8090:8080 --add-host="external:<local-host-ip>" rulesapp-engine`
    * Use Insomnia to communicate

* Flask
    * Great for test web services
    * Demo minimal web app
    * Demo banking service with test rules

* Pytest
    * Lack of ceremony
    * Dependency injection via fixtures
    * Fixtures as temporary state
    * Can fixtures run docker containers?

* Docker SDK for Python
    * Fine control over docker container - like docker-compose
    * Great fit for Python context manager
    * Quick demo
