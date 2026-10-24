# Bluetooth audio feasibility

The owner wants speakers/headphones and has no USB Bluetooth adapter. No driver,
kernel module, firmware, device-tree setting or Bluetooth state was changed.

Read-only checks on the reference RK3229 MBOX on 2026-10-24 found:

| Layer | Observed state |
|---|---|
| Android features | USB host advertised; no Bluetooth feature advertised. |
| Framework/assets | `com.android.bluetooth`, Bluetooth HALs, UART/USB vendor libraries and multiple chipset firmware files exist. |
| Kernel | Bluetooth core exists, **Realtek `rtk_btusb` USB driver is already registered**, and `/dev/rtk_btusb` exists. Generic `btusb` was absent. Kernel config archive unavailable. |
| Controller | No HCI entry exposed in `/sys/class/bluetooth`; only USB root hubs enumerated, no USB Bluetooth adapter. |
| Device tree/rfkill | `wireless-bluetooth` is disabled; the only rfkill entry reports WLAN. |
| Current radio | SSV6158 Wi-Fi works; exact Bluetooth hardware and board wiring remain unconfirmed. |

The existing driver/assets do not prove a Bluetooth radio is installed. An empty
HCI class alone also does not prove physical absence: some Android hardware uses
userspace UART transport. There is no verified controller to enable safely here.
Installing an APK or a random driver cannot add the missing radio hardware.

IComm's product table distinguishes SV6158 (no Bluetooth listed) from SV6158M
(BLE 5.0). The observed Wi-Fi driver name/SDIO ID does not establish that precise
variant or board wiring. Speakers/headphones on this Android generation normally
need Classic Bluetooth A2DP; BLE-only capability would not supply that audio path.

A supported Realtek USB controller is a possible investigation route because a
vendor USB driver and matching software assets already exist. A generic dongle
is **not** guaranteed to work: exact chipset/USB IDs, kernel driver, Android vendor
HAL and audio-profile integration must match. No adapter is available to test.

For audio only, a Bluetooth **transmitter (TX)** connected to a compatible TV/box
audio output is another route that avoids relying on Android's Bluetooth stack.
The available audio output must be confirmed before choosing hardware; a Bluetooth
receiver (RX) alone does not serve this purpose. No specific purchase is proposed.

Evidence: private diagnostic sessions `20261024T200612+0100` and
`20261024T200815+0100`. No paired-device addresses or personal connection data were
collected. Further work requires identified hardware; no speculative module
loading or device-tree edits are warranted by these findings.

Sources: [IComm SV6158 series model table](https://www.icomm-semi.com/Product/info/id/53.html),
[Android Bluetooth profiles and A2DP](https://developer.android.com/develop/connectivity/bluetooth/profiles),
[Android 7 Bluetooth architecture](https://android.googlesource.com/platform/docs/source.android.com/+/4945711ac0fe9f19ae0d358ae00db5c03485aa53/en/devices/bluetooth/index.html).
