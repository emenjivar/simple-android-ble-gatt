from bluezero import adapter, peripheral
from gpiozero import LED
import subprocess

## UUIDS
SERVICE_UUID            = '290edf15-b540-4e83-83cf-ba647bf4df20'
CHARACTERISTIC_UUID     = '290edf15-b540-4e83-83cf-ba647bf4df21'
GET_IP_UUID             = '290edf15-b540-4e83-83cf-ba647bf4df22'
GET_SSID_UUID           = '290edf15-b540-4e83-83cf-ba647bf4df23'
WIFI_SSID_UUID          = '290edf15-b540-4e83-83cf-ba647bf4df24'
WIFI_PASSWORD_UUID      = '290edf15-b540-4e83-83cf-ba647bf4df25'
WIFI_CONNECT_UUID       = '290edf15-b540-4e83-83cf-ba647bf4df26'
WIFI_STATUS_UUID        = '290edf15-b540-4e83-83cf-ba647bf4df27'

# Value to expose
LED_OFF = 0x00
LED_ON  = 0x01
LED_STATE = [LED_OFF]

# IO
led = LED(17)

char_obj = None  # Direct reference to the characteristic

pending_ssid = ""
pending_password = ""
wifi_status = list("idle".encode('utf-8'))

def read_value():
    print(f"[READ] Characteristic was read: {LED_STATE}")
    return LED_STATE

def get_ip_address():
    try:
        result = subprocess.check_output(['ip', 'addr', 'show', 'wlan0'])
        for line in result.decode().splitlines():
            if 'inet ' in line:
                ip = line.strip().split()[1].split('/')[0]
                return list(ip.encode('utf8'))
    except Exception:
        pass
    return list('0.0.0.0'.encode('utf8'))

def get_ssid():
    try:
        result = subprocess.check_output(['iwgetid', '-r'])
        ssid = result.decode().strip()
        return list(ssid.encode('utf8'))
    except Exception:
        pass
    return list('N/A'.encode('utf8'))

def read_wifi_status():
    print(f"[WIFI] Status read: {bytes(wifi_status).decode('utf-8')}")
    return wifi_status

WIFI_STATUS_IDLE        = "idle"
WIFI_STATUS_CONNECTED   = "connected"
WIFI_STATUS_NOT_FOUND   = "error: ssid_not_found"
WIFI_STATUS_AUTH_FAILED = "error: auth_failed"
WIFI_STATUS_TIMEOUT     = "error: timeout"
WIFI_STATUS_UNKNOWN     = "error: unknown"

# nmcli exit codes: 0=success, 4=activation failed (wrong password), 10=ssid not found
NMCLI_EXIT_CODES = {
    0:  WIFI_STATUS_CONNECTED,
    4:  WIFI_STATUS_AUTH_FAILED,
    10: WIFI_STATUS_NOT_FOUND,
}

def connect_wifi(ssid, password):
    global wifi_status
    try:
        print("[WIFI] Scanning for networks...")
        subprocess.run(
            ['nmcli', 'dev', 'wifi', 'rescan'],
            capture_output=True, timeout=10
        )
        result = subprocess.run(
            ['nmcli', 'dev', 'wifi', 'connect', ssid, 'password', password],
            capture_output=True, text=True, timeout=30
        )
        status = NMCLI_EXIT_CODES.get(result.returncode, WIFI_STATUS_UNKNOWN)
        print(f"[WIFI] nmcli exited {result.returncode} → {status}")
        wifi_status = list(status.encode('utf-8'))
        return result.returncode == 0
    except subprocess.TimeoutExpired:
        wifi_status = list(WIFI_STATUS_TIMEOUT.encode('utf-8'))
        print("[WIFI] nmcli timed out")
        return False

def write_wifi_ssid(value, options):
    global pending_ssid
    pending_ssid = bytes(value).decode('utf-8')
    print(f"[WIFI] SSID set: {pending_ssid}")

def write_wifi_password(value, options):
    global pending_password
    pending_password = bytes(value).decode('utf-8')
    print(f"[WIFI] Password set: {'*' * len(pending_password)}")

def write_wifi_connect(value, options):
    if bytes(value)[0] == 0x01:
        print(f"[WIFI] Connecting to: {pending_ssid}")
        success = connect_wifi(pending_ssid, pending_password)
        print(f"[WIFI] Connect {'OK' if success else 'FAILED'}: {pending_ssid}")

def notify_callback(notifying, characteristic):
    if notifying:
        print("[NOTIFY] Client subscribed to notifications")
    else:
        print("[NOTIFY] Client unsubscribed from notifications")

def write_value(value, options):
    global LED_STATE
    byte_val = bytes(value)
    command = byte_val[0]

    if command not in (LED_OFF, LED_ON):
        raise ValueError(f"[ERROR] Invalid value: {hex(command)}.")

    LED_STATE = [command]
    print(f"[WRITE] Characteristic was written: {LED_STATE}")

    if command == LED_ON:
        led.on()
    elif command == LED_OFF:
        led.off()

    # Emit notification
    if char_obj and char_obj.is_notifying:
        char_obj.set_value(LED_STATE)

def main():
    global char_obj

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
        value = [LED_OFF],
        notifying = False,
        flags = ['read', 'write', 'notify'],  # all three together
        read_callback = read_value,
        write_callback = write_value,
        notify_callback = notify_callback
    )

    app.add_characteristic(
        srv_id = 1,
        chr_id = 2,
        uuid = GET_IP_UUID,
        value = get_ip_address(),
        notifying = False,
        flags = ['read'],
        read_callback = get_ip_address,
        write_callback = None,
        notify_callback = None
    )

    app.add_characteristic(
        srv_id = 1,
        chr_id = 3,
        uuid = GET_SSID_UUID,
        value = get_ssid(),
        notifying = False,
        flags = ['read'],
        read_callback = get_ssid,
        write_callback = None,
        notify_callback = None
    )

    app.add_characteristic(
        srv_id = 1,
        chr_id = 4,
        uuid = WIFI_SSID_UUID,
        value = [],
        notifying = False,
        flags = ['write'],
        read_callback = None,
        write_callback = write_wifi_ssid,
        notify_callback = None
    )

    app.add_characteristic(
        srv_id = 1,
        chr_id = 5,
        uuid = WIFI_PASSWORD_UUID,
        value = [],
        notifying = False,
        flags = ['write'],
        read_callback = None,
        write_callback = write_wifi_password,
        notify_callback = None
    )

    app.add_characteristic(
        srv_id = 1,
        chr_id = 6,
        uuid = WIFI_CONNECT_UUID,
        value = [0x00],
        notifying = False,
        flags = ['write'],
        read_callback = None,
        write_callback = write_wifi_connect,
        notify_callback = None
    )

    app.add_characteristic(
        srv_id = 1,
        chr_id = 7,
        uuid = WIFI_STATUS_UUID,
        value = wifi_status,
        notifying = False,
        flags = ['read'],
        read_callback = read_wifi_status,
        write_callback = None,
        notify_callback = None
    )

    char_obj = app.characteristics[0]

    print("GATT server starting")
    print(f"Local name: Charlie gatt server")
    print(f"Service UUID: {SERVICE_UUID}")
    print(f"Char UUID:    {CHARACTERISTIC_UUID}")
    print(f"Value: {LED_STATE}")
    print("Advertising - waiting for connections (Ctrl+c to stop)")
    app.publish()

if __name__ == '__main__':
    main()