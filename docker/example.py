#!/usr/bin/env python3
import docker as dockerlib
import time

docker = dockerlib.from_env()

class DockerContainer:
    def __init__(self, image_name):
        self._image_name = image_name

    def __enter__(self):
        self._container = docker.containers.run(image=self._image_name, detach=True)
        return self._container

    def __exit__(self, *args):
        self._container.kill()
        self._container.remove()

if __name__ == '__main__':
    with DockerContainer("rulesapp-compiler") as compiler:
        try:
            for line in compiler.logs(stream=True):
                print(">> %s" % line)
        except InterruptedError as e:
            pass
