from framework import RestClient, ContentType
import requests

def test_compiler(compiler):
    result = compiler.post('/compile', content_type=ContentType.TEXT, data='always permit').json()

    assert result == {
        'attributes': [],
        'rules': [{
            'type': 'always',
            'decision': 'Permit'
        }]
    }
