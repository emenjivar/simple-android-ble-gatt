from bluezero import adapter, peripheral
from gpiozero import LED

## UUIS
SERVICE_UUID = '290edf15-b540-4e83-83cf-ba647bf4df20'
CHARACTERISTIC_UUID = '290edf15-b540-4e83-83cf-ba647bf4df21'

# Value to expose
LED_OFF = 0x00
LED_ON = 0x01
LED_STATE = LED_OFF

# IO
led = LED(17)

app = None # Glo0bal reference to call update_value

def read_value():
    print(f"[READ] Characteristic was read: {LED_STATE}")
    return LED_STATE

def notify_callback(notifying, characteristic):
    if notifying:
        print("[NOTIFY] Client subscribed to notifications")
        return list(LED_STATE)
    else:
        print("[NOTIFY] Client unsubscribed from notifications")

def write_value(value, options):
    global LED_STATE
    command = bytes(value)[0]
    if command not in (LED_OFF, LED_ON):
        raise ValueError(f"[ERROR] Invalid value: {hex(command)}.")

    LED_STATE = bytes([command])  # fix here
    print(f"[WRITE] Characteristic was written: {LED_STATE}")

    if command == LED_ON:
        led.on()
    elif command == LED_OFF:
        led.off()

    app.update_value(srv_id=1, chr_id=1)

def main():
    global app

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
        notifying = True,
        flags = ['read', 'write', 'notify'],
        read_callback = read_value,
        write_callback = write_value,
        notify_callback = notify_callback
    )

    print("GATT server starting")
    print(f"Local name: Charlie gatt server")
    print(f"Service UUID: {SERVICE_UUID}")
    print(f"Char UUID: {CHARACTERISTIC_UUID}")
    print(f"Value: {LED_STATE}")
    print("Advertising - waiting for connections (Ctrl+c to stop)")
    app.publish()

if __name__ == '__main__':
    main()
