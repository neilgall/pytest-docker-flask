from socket import *

def get_host_ip():
    """Get the IP address of the host"""
    s = socket(AF_INET, SOCK_DGRAM)
    try:
        # doesn't need to be reachable
        s.connect(('1.1.1.1', 1))
        return s.getsockname()[0]
    except:
        return '127.0.0.1'
    finally:
        s.close()

if __name__ == "__main__":
    print(get_host_ip())
