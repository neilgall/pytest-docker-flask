from framework import RestClient, ContentType
import requests

def test_end_to_end(compiler, engine):
    """
    Simple end-to-end test invoking the compiler and running the resulting code in the engine
    """
    compiled = compiler.post('/compile', content_type=ContentType.TEXT, data='always permit').json()
    
    load = engine.post('/load', json=compiled).text
    assert load == 'ok'

    result = engine.post('/query', json={}).json()
    assert result[0]['value'] == 'Permit'
    