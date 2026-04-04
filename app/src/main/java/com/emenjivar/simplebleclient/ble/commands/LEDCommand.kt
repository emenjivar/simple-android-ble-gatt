package com.emenjivar.simplebleclient.ble.commands

/**
 * Available command values for manipulate the peripheral's LED
 */
enum class LEDCommand(val bytes: Byte) {
    OFF(0x00),
    ON(0x01);
}
