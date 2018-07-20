def test_end_to_end(compiler, engine):
    """
    Simple end-to-end test invoking the compiler and running the resulting code in the engine
    """
    compiled = compiler.compile('always permit')    
    assert engine.load(compiled)
    
    result = engine.query({})
    assert result[0].value == 'Permit'
