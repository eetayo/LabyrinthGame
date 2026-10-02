#!/usr/bin/env python3
"""Servidor de desarrollo: como `python3 -m http.server`, pero sin cache.

Con el servidor estandar el navegador puede reutilizar modulos JS antiguos y mezclar
versiones. Aqui cada peticion se revalida (Cache-Control: no-cache).

    python3 version2/tools/serve.py [puerto]
"""
import os
import sys
from functools import partial
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..')


class Handler(SimpleHTTPRequestHandler):
    def end_headers(self):
        self.send_header('Cache-Control', 'no-cache')
        super().end_headers()


if __name__ == '__main__':
    port = int(sys.argv[1]) if len(sys.argv) > 1 else 8642
    print(f'Logic Labyrinth en http://localhost:{port}')
    ThreadingHTTPServer(('', port), partial(Handler, directory=ROOT)).serve_forever()
