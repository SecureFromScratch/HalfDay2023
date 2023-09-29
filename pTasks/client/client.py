import socket
import threading
import sys
import time
import username_provider

class TasksClient:
    def __init__(self, server_host, server_port, username):
        self.server_host = server_host
        self.server_port = server_port
        self.username = username

    def connect_to_server(self):
        try:
            self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.socket.connect((self.server_host, self.server_port))

            username = self.username if self.username else username_provider.get_username()
            print(f"Connected to the server. Sending username {username}")
            self.socket.sendall((username + '\n').encode())

            # Start a thread to receive messages from the server
            receive_thread = threading.Thread(target=self.receive_messages)
            receive_thread.start()

            # Send user input to the server
            while self.socket:
                time.sleep(0.3) # solve a problem that on windows input() blocks the program completely, even other threads
                user_input = input() if self.socket else ""
                if self.socket:
                    self.socket.sendall((user_input + '\n').encode())
        except Exception as e:
            print(f"Send Error: {e}")
        finally:
            if self.socket:
                self.socket.close()
                self.socket = None

    def receive_messages(self):
        try:
            while True:
                data = self.socket.recv(1024).decode()
                if not data:
                    break
                print(data.rstrip())
        except Exception as e:
            print(f"Recv error: {e}")
        finally:
            self.socket.close()
        self.socket = None
        print("Connection closed")

if __name__ == "__main__":
    USERNAME_FLAG = "-user="

    print(f"Usage: python {sys.argv[0]} [host] [port] [{USERNAME_FLAG}=<pwd>]")
    if len(sys.argv) > 4:
        print("Arguments error (too many)")
        sys.exit(-1)

    server_host = "localhost"
    server_port = 8000
    username = None
    if len(sys.argv) > 1:
        if sys.argv[1].startswith(USERNAME_FLAG):
            username = sys.argv[1][len(USERNAME_FLAG):]
        else:
            server_host = sys.argv[1]

    if len(sys.argv) > 2:
        if not username and sys.argv[2].startswith(USERNAME_FLAG):
            username = sys.argv[2][len(USERNAME_FLAG):]
        elif username:
            server_host = sys.argv[2]
        else:
            server_port = int(sys.argv[2])

    if len(sys.argv) > 3:
        if not username and sys.argv[3].startswith(USERNAME_FLAG):
            username = sys.argv[3][len(USERNAME_FLAG):]
        elif username:
            server_port = sys.argv[3]
        else:
            print("Arguments error (too many)")
            sys.exit(-2)            

    client = TasksClient(server_host, server_port, username)
    client.connect_to_server()
