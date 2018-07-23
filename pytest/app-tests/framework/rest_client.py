import os
import requests
import time

import logging
import http.client
http.client.HTTPConnection.debuglevel = 1

class ContentType:
    TEXT = 'text/plain'
    JSON = 'application/json'
    BINARY = 'application/octet-stream'

class RestClient:
    """
    An API client
    """
    def __init__(self, host, port):
        self._base = "http://%s:%d" % (host, port)
        self._session = requests.Session()

    def __repr__(self):
        return "RestClient{base=%s}" % self._base

    def _url(self, path):
        sep = '' if len(path) > 0 and path[0] == '/' else '/'
        return self._base + sep + path

    def get_base_url(self):
        return self._base

    def wait_for_ready(self, timeout=30):
        """
        Poll the system status API until it succeeds.
        """
        expiry = time.time() + timeout
        while time.time() < expiry:
            try:
                self.get("/status")
                return
            except Exception as e:
                time.sleep(1)
        raise TimeoutError("timed out waiting for %s to be ready" % self._base)

    def get(self, path, allow_errors = False, **kwargs):
        "Perform a GET request."
        rsp = self._session.get(self._url(path), **kwargs)
        assert allow_errors or rsp.status_code < 300, "bad response from GET %s: %s" % (path, rsp)
        return rsp

    def post(self, path, content_type=ContentType.JSON, headers={}, allow_errors=False, **kwargs):
        "Perform a POST request"
        all_headers = { 'Content-Type': content_type }
        all_headers.update(headers)
        rsp = self._session.post(self._url(path), headers=all_headers, **kwargs)
        assert allow_errors or rsp.status_code < 300, "bad response from POST %s: %s" % (path, rsp)
        return rsp

    def put(self, path, content_type=ContentType.JSON, headers={}, allow_errors=False, **kwargs):
        "Perform a PUT request"
        all_headers = { 'Content-Type': content_type }
        all_headers.update(headers)
        rsp = self._session.put(self._url(path), headers=all_headers, **kwargs)
        assert allow_errors or rsp.status_code < 300, "bad response from PUT %s: %s" % (path, rsp)
        return rsp

    def delete(self, path, allow_errors=False, **kwargs):
        "Perform a DELETE request"
        rsp = self._session.delete(self._url(path), **kwargs)
        assert allow_errors or rsp.status_code < 300, "bad response from DELETE %s: %s" % (path, rsp)
        return rsp

    def download_get(self, path, file, **kwargs):
        "Perform a GET request, downloading the content to the given file-like object"
        rsp = self._session.get(self._url(path), **kwargs)
        assert rsp.status_code < 300, "bad response from GET %s: %s" % (path, rsp)
        file.write_binary(rsp.content)
