# simple-android-ble-gatt
Minimal gatt server written in python for controlling a led using BLE.

# Installation
## Prerequisites
```bash
sudo apt install libcairo2-dev -y
sudo apt install libgirepository1.0-dev -y
sudo apt install libdbus-1-dev -y

```

## Setup virtual environment
```bash
python3 -m venv ~/venv_bluetooth --system-site-packages
source ~/venv_bluetooth/bin/activate
pip install bluezero
```

## Running the server
```bash
source ~/venv_bluetooth/bin/activate
python 3 gatt_server.py
```

> To exit the virtual environment, run `deactivate`