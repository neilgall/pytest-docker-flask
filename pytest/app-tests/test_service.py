from flask import Flask
from framework import RestClient, ContentType, Service
import pytest
import requests

@pytest.fixture
def simple_service():
    """
    Provides a simple service running in the pytest process
    """
    app = Flask('simple-service')

    @app.route('/hello')
    def main():
        return 'ok'

    return Service(app)

def test_service(compiler, engine, simple_service):
    """
    End-to-end test including a call to a local service
    """
    compiled = compiler.compile('''
    result = GET "%s"
    if result = "ok" always permit else always deny
    ''' % simple_service.url('/hello'))
    
    assert engine.load(compiled)

    with simple_service as service:
        result = engine.query({})
        assert result[0].value == 'Permit'
        assert len(service.invocations) == 1
    
