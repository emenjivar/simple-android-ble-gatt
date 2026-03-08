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

### I'm not able to connect to the GATT server anymore

Execute and copy the output
```bash
charlie@charlie-asus:~/Documents/repositories/simple-android-ble-gatt$ which bluetoothd
/usr/sbin/bluetoothd
```

Open the bluetooth configuration file
```
sudo vim /etc/systemd/system/bluetooth.target.wants/bluetooth.service
```

Then add the flag ``--experimental``:
```
# ExecStart=/usr/lib/bluetooth/bluetoothd 
ExecStart=/usr/sbin/bluetoothd --experimental
``

Now reset the bluetooth daemon
```
sudo systemctl daemon-reload
sudo systemctl restart bluetooth
```