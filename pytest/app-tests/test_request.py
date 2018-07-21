import pytest

@pytest.fixture(scope='module')
def engine_with_rules(compiler, engine):
    compiled = compiler.compile('''
    myValue = request "myValue"

    exclusive {
	    permit when myValue = "foo",
	    deny when myValue = "bar"
    }
    ''')
    assert engine.load(compiled)
    return engine

def test_foo_permits(engine_with_rules):
    result = engine_with_rules.query({'myValue': 'foo'})
    assert result[0].value == 'Permit'

def test_bar_denies(engine_with_rules):
    result = engine_with_rules.query({'myValue': 'bar'})
    assert result[0].value == 'Deny'

def test_no_rule_for_other_values(engine_with_rules):
    result = engine_with_rules.query({'myValue': 'qq'})
    assert result[0].value == 'Undecided'
