import pytest

@pytest.fixture
def hello_file(tmpdir):
    path = tmpdir / 'hello.txt'
    with path.open('wt') as f:
        f.write('hello world')
    yield path
    path.remove()

def test_hello(hello_file):
    print(hello_file)
    with hello_file.open('rt') as f:
        assert f.read() == 'hello world'
        