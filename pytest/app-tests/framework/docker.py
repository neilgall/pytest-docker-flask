import atexit
import docker as dockerlib
import json
import os
import re
import requests
from .networking import get_host_ip

_export_logs_on_exit=True
_export_filesystem_on_exit=False

_docker = dockerlib.from_env()

# Create a global bridge network which all docker containers connect to,
# so by default they can talk to each other by hostname
_network = _docker.networks.create('test-network', driver='bridge')
atexit.register(_docker.networks.prune)

class DockerContainer(object):
    """
    A context manager which runs a docker container in its context
    """
    def __init__(self, tmpdir, name, tag = None, run_id='', **kwargs):
        """
        tmpdir - a temporary directory for configuration and output files
        name - the name of the container to run
        tag - the tag for the container to run
        run_id - a unique ID per run; useful if the same container is invoked multiple times per test
        kwargs - further arguments to docker.containers.run()
        """
        self._tmpdir = tmpdir
        self._name = name
        self._tag = ':' + tag if tag is not None else ''
        self._run_id = str(run_id)
        self._args = kwargs
        self._container = None

    def __enter__(self):
        self.start()

    def __exit__(self, exc_type, exc_value, traceback):
        self.stop()

    def _image_name(self):
        return '%s%s' % (self._name, self._tag)

    def _container_name(self):
        return '%s%s' % (re.sub(r'[^0-9a-zA-Z]+', '_', self._name), self._run_id)

    def _rerun_docker_command(self):
        "Generate an equivalent docker command to rerun this container for debugging"
        cmd = ['docker', 'run', '--network', 'host']
        for (k,v) in self._args.get('environment', {}).items():
            cmd.extend(['-e', '"%s=%s"' % (k,v)])
        for (k,v) in self._args.get('volumes', {}).items():
            cmd.extend(['-v', '"%s:%s"' % (k, v['bind'])])
        for (k,v) in self._args.get('ports', {}).items():
            cmd.extend(['-p', '%s:%s"' % (k.split('/')[0], str(v))]) 
        cmd.append(self._image_name())
        return " ".join(cmd)

    def _container_info(self):
        "Generate the docker container info contents"
        return {
            'image': self._image_name(),
            **self._args
        }

    def _write_tmpfile(self, base, ext, text):
        os.makedirs(self._tmpdir, exist_ok=True)
        f = self._tmpdir / ('%s%s.%s' % (base, self._run_id, ext))
        f.write_text(text, encoding='utf8')

    def start(self):
        assert self._container is None

        container_info = json.dumps(self._container_info(), indent=2)
        self._write_tmpfile('container', 'json', container_info)
        self._write_tmpfile('docker', 'cmd', self._rerun_docker_command())

        for container in _docker.containers.list():
            if container.name == self._container_name():
                container.remove()
                break

        try:
            _docker.images.get(self._image_name())
        except dockerlib.errors.ImageNotFound:
            _docker.images.pull(self._image_name())

        self._container = _docker.containers.run(
            image=self._image_name(),
            name=self._container_name(),
            detach=True,
            network=_network.name,
            **self._args)
        
    def stop(self):
        root = 'container%s' % self._run_id
        (self._tmpdir / root + '.status').write(self._container.status)
        self._container.stop(timeout=1)
        if _export_logs_on_exit:
            (self._tmpdir / root + '.stdout').write(self._container.logs(stdout=True, stderr=False))
            (self._tmpdir / root + '.stderr').write(self._container.logs(stderr=True, stdout=False))
        if _export_filesystem_on_exit:
            with (self._tmpdir / root + '.tar').open('wb') as f:
                for chunk in self._container.export(): f.write(chunk)
        self._container.remove()
        self._container = None

    def wait(self):
        self._container.wait()

    def get_stdout(self):
        return self._container.logs(stdout=True, stderr=False)
