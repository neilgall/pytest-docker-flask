from flask import Flask
from framework import RestClient, ContentType, Service
import pytest
import requests

@pytest.fixture(scope='module')
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
    compiled = compiler.post('/compile', content_type=ContentType.TEXT, data='''
    result = GET "%s"
    if result = "ok" always permit else always deny
    ''' % simple_service.url('/hello')).json()
    
    load = engine.post('/load', json=compiled).text
    assert load == 'ok'

    with simple_service:
        result = engine.post('/query', json={}).json()
        assert result[0]['value'] == 'Permit'
    
