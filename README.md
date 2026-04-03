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
python3 gatt_server.py
```

> To exit the virtual environment, run `deactivate`
> 
## Start the server during startup

Create a service file:
```bash
sudo vim /etc/systemd/system/gatt_server.service
```

```
[Unit]
Description=GATT server BLE
# Wait for bluetooth and network before starting
After=network.target bluetooth.target

[Service]
Type=simple
User=charlie
WorkingDirectory=/home/charlie/Desktop
ExecStart=/home/charlie/venv_bluetooth/bin/python3 /home/charlie/Desktop/gatt_server.py
Restart=on-failure

[Install]
WantedBy=multi-user.target
```

Run the following commands
```bash
sudo systemctl daemon-reload
sudo systemctl enable gatt_server.service
sudo systemctl start gatt_server.service
```

Then verify:
```bash
sudo systemctl status gatt_server.service
```