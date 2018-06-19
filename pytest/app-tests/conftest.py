from framework import DockerContainer, RestClient
import pytest
import random

def _dockerised_rest_app(tmpdir, name):
    port = random.randint(40000, 50000)
    with DockerContainer(tmpdir, name, ports={'8080/tcp':port}) as app:
        client = RestClient('localhost', port)
        client.wait_for_ready()
        yield client

@pytest.fixture
def compiler(tmpdir):
    yield from _dockerised_rest_app(tmpdir, 'rulesapp-compiler')


