import socket
import extralogging as logging
import sys

class SimpleServer:
    def __init__(self, port):
        self.port = port
        self.logger = logging.getLogger(__name__)
        self.active_request = None

        self.server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.server_socket.bind(("localhost", self.port))
        self.server_socket.listen(1)
        self.logger.info(f"Server is listening on port {self.port}")

    @classmethod
    def create(cls, port):
        return cls(port)

    class Connection:
        def __init__(self, server, socket):
            self.server = server
            self.cur_socket = socket
            self.reader = socket.makefile("r")
            self.writer = socket.makefile("w")

        def get_input(self):
            try:
                line = self.reader.readline()
                if not line:
                    self.close()
                    return ""
                return line.rstrip()
            except Exception as ex:
                self.close()
                return ""

        def write(self, text):
            self.server.logger.debug(f"Server is writing {text}")
            self.writer.write(text)
            self.writer.flush()

        def writeln(self, text):
            self.server.logger.debug(f"Server is writing {text}")
            self.writer.write(text + "\n")
            self.writer.flush()

        def __enter__(self):
            return self
        
        def __exit__(self, exc_type, exc_value, traceback):
            self.close()

        def close(self):
            if self.reader:
                self.reader.close()
            if self.writer:
                self.writer.close()
            if self.cur_socket:
                try:
                    self.cur_socket.shutdown(socket.SHUT_RDWR)
                except OSError:
                    pass
                finally:
                    self.cur_socket.close()
                    self.cur_socket = None
                    self.reader = None
                    self.writer = None

            if self.server.active_request == self:
                self.server.active_request = None

            return True
            
    def wait_for_connection(self):
        if self.active_request is not None:
            raise PreviousConnectionNotClosed()

        while True:
            try:
                self.logger.info("Server is waiting for a new client")
                client_socket, _ = self.server_socket.accept()
                self.logger.info("New client connected")
                self.active_request = self.Connection(self, client_socket)
                return self.active_request
            except Exception as ex:
                if client_socket:
                    client_socket.close()
                self.logger.warning(f"Socket error during connection init: {ex}")
                raise

    def close(self):
        self.server_socket.close()

class PreviousConnectionNotClosed(Exception):
    pass
