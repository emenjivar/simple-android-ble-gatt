from bluezero import adapter, peripheral

## UUIS
SERVICE_UUID = '290edf15-b540-4e83-83cf-ba647bf4df20'
CHARACTERISTIC_UUID = '290edf15-b540-4e83-83cf-ba647bf4df21'

# Value to expose
VALUE = b'Hello BLE!'

def read_value():
    print("[READ] Characteristic was read")
    return VALUE

def write_value(value, options):
    global VALUE
    VALUE = bytes(value)
    print(f"[WRITE] Characteristic was written: {VALUE}")

def main():
    dongle = list(adapter.Adapter.available())[0]
    adapter_address = dongle.address
    print(f"Using adapter: {adapter_address}")

    app = peripheral.Peripheral(
        adapter_address = adapter_address,
        local_name = 'Charlie gatt server'
    )
    app.add_service(
        srv_id = 1,
        uuid = SERVICE_UUID,
        primary = True
    )

    app.add_characteristic(
        srv_id = 1,
        chr_id = 1,
        uuid = CHARACTERISTIC_UUID,
        value = [],
        notifying = False,
        flags = ['read', 'write'],
        read_callback = read_value,
        write_callback = write_value,
        notify_callback = None
    )

    print("GATT server starting")
    print(f"Local name: Charlie gatt server")
    print(f"Service UUID: {SERVICE_UUID}")
    print(f"Char UUID: {CHARACTERISTIC_UUID}")
    print(f"Value: {VALUE}")
    print("Advertising - waiting for connections (Ctrl+c to stop)")
    app.publish()

if __name__ == '__main__':
    main()