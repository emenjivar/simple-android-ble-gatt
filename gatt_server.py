from bluezero import adapter, peripheral

# ── UUIDs ──────────────────────────────────────────────────────────────────
SERVICE_UUID        = '12345678-1234-5678-1234-56789abcdef0'
CHARACTERISTIC_UUID = '12345678-1234-5678-1234-56789abcdef1'

# ── Hardcoded value to expose ───────────────────────────────────────────────
HARDCODED_VALUE = b'Hello BLE!'

def read_value():
    print("  [READ] Characteristic was read")
    return HARDCODED_VALUE

def main():
    # Auto-detect the first available Bluetooth adapter
    dongle = list(adapter.Adapter.available())[0]
    adapter_address = dongle.address
    print(f"  Using adapter: {adapter_address}")

    app = peripheral.Peripheral(adapter_address=adapter_address,
                                local_name='MyGattServer')

    app.add_service(srv_id=1, uuid=SERVICE_UUID, primary=True)

    app.add_characteristic(
        srv_id=1,
        chr_id=1,
        uuid=CHARACTERISTIC_UUID,
        value=[],
        notifying=False,
        flags=['read'],
        read_callback=read_value,
        write_callback=None,
        notify_callback=None,
    )

    print("GATT server starting...")
    print(f"  Local name  : MyGattServer")
    print(f"  Service UUID: {SERVICE_UUID}")
    print(f"  Char UUID   : {CHARACTERISTIC_UUID}")
    print(f"  Value       : {HARDCODED_VALUE}")
    print("Advertising — waiting for connections (Ctrl+C to stop)")
    app.publish()

if __name__ == '__main__':
    main()