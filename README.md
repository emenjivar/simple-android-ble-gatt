# simple-android-ble-gatt

## Installacion

1. dependencies
```bash
sudo apt update
sudo apt install -y python3-pip bluez bluetooth libglib2.0-dev
sudo pip3 install bluezero
```

1. starting the server
make sure bluetooth is up
```bash
sudo systemctl start bluetooth
sudo hciconfig hci0 up        # bring up the adapter
```

run the server
```bash
sudo python3 gatt_server.py
```
