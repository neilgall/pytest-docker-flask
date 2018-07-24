import flask
import random
import requests
import threading
import time
import wsgiref

class _Instrumented:
    "WSGI middleware for instrumenting an embedded service."
    def __init__(self, app):
        self._app = app
        self.invocations = []

    def __call__(self, environ, start_response):
        rsp = self._app(environ, start_response)
        self.invocations.append((environ, rsp))
        return rsp

class Service:
    """
    Provides a context manager server lifecycle around a flask-defined API.
    Create a Flask APP as normal then use as a context manager, e.g:

        with Service(flask_app) as svc:
            ...

    Inside the `with` block the `svc` object has one property `invocations`
    which is a list of `(environ, response)` pairs for each invocation of
    the service.
    """
    def __init__(self, flask_app, port=None):
        self._flask_app = flask_app
        self._port = port or random.randint(50000,60000)
        self._server = None

        # add a shutdown hook to the Flask app
        @flask_app.route('/service-control', methods=['GET','DELETE'])
        def pip_control():
            if flask.request.method == 'GET':
                return "ok"
            elif flask.request.method == 'DELETE':
                flask.request.environ.get('werkzeug.server.shutdown')()
                return 'bye!'
            else:
                return "err"

    def url(self, path, localhost=False):
        host = 'localhost' if localhost else 'services'
        sep = '' if path.startswith('/') else '/'
        return 'http://%s:%d%s%s' % (host, self._port, sep, path)

    def flask_thread(self):
        self._flask_app.run(host='0.0.0.0', port=self._port)

    def _start_server(self):
        server = threading.Thread(target=self.flask_thread)
        server.start()
        timeout = time.time() + 5
        while time.time() < timeout:
            if not server.is_alive():
                time.sleep(1)
            else:
                try:
                    rsp = requests.get('http://localhost:%d/service-control' % self._port, timeout=1)
                    assert rsp.text == "ok"
                    return server
                except:
                    pass

    def _stop_server(self, server):
        rsp = requests.delete('http://localhost:%d/service-control' % self._port)
        assert rsp.text == 'bye!'
        server.join()

    def __repr__(self):
        return "Service:%s" % self._flask_app.name
    
    def __enter__(self):
        assert self._server is None
        instrumented = _Instrumented(self._flask_app.wsgi_app)
        self._original_wsgi_app = self._flask_app.wsgi_app
        self._flask_app.wsgi_app = instrumented
        self._server = self._start_server()
        instrumented.invocations = [] # clear out the start request
        return self._flask_app.wsgi_app

    def __exit__(self, exc_type, exc_value, traceback):
        self._stop_server(self._server)
        self._flask_app.wsgi_app = self._original_wsgi_app
        self._server = None

if __name__ == "__main__":
    app = flask.Flask("test")

    @app.route("/foo/<bar>")
    def foo(bar):
        return "hello " + bar

    svc = Service(app)
    with svc:
        rsp = requests.get(svc.url('/foo/test', localhost=True))
        assert rsp.text == "hello test"
    


