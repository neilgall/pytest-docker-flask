def test_can_write_files(tmpdir):
    with (tmpdir / 'foo.txt').open("wt") as f:
        f.write("hello world")
