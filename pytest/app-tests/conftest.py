from framework import DockerContainer, RestClient, ContentType
from munch import munchify
import pytest
import random

def _dockerised_rest_app(tmpdir, name, client_class=RestClient):
    port = random.randint(40000, 50000)
    with DockerContainer(tmpdir, name, ports={'8080/tcp':port}) as app:
        client = client_class('localhost', port)
        client.wait_for_ready()
        yield client

@pytest.fixture(scope='module')
def compiler(tmpdir_factory):
    class CompilerClient(RestClient):
        def compile(self, text):
            return self.post('/compile', content_type=ContentType.TEXT, data=text).json()

    tmpdir = tmpdir_factory.mktemp('compiler')
    yield from _dockerised_rest_app(tmpdir, 'rulesapp-compiler', CompilerClient)

@pytest.fixture(scope='module')
def engine(tmpdir_factory):
    class EngineClient(RestClient):
        def load(self, json):
            return self.post('/load', content_type=ContentType.JSON, json=json).text == 'ok'
        def query(self, json):
            return munchify(self.post('/query', content_type=ContentType.JSON, json=json).json())

    tmpdir = tmpdir_factory.mktemp('engine')
    yield from _dockerised_rest_app(tmpdir, 'rulesapp-engine', EngineClient)
