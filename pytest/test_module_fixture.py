import pytest

@pytest.fixture(scope='module')
def hello_file(tmpdir_factory):
    tmpdir = tmpdir_factory.mktemp('module-scope')
    path = tmpdir / 'hello.txt'
    with path.open('wt') as f:
        f.write('hello world')
    yield path
    path.remove()

def test_hello(hello_file):
    print(hello_file)
    with hello_file.open('rt') as f:
        assert 'hello' in f.read()
        
def test_world(hello_file):
    print(hello_file)
    with hello_file.open('rt') as f:
        assert 'world' in f.read()
