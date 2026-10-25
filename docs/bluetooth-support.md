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

## Audio connection plan — 2026-10-25

The owner confirmed that the MBOX sends media and sound to the TV over HDMI;
the TV speakers currently play that audio. Only the MBOX, TV and Bluetooth
headphones/speaker are available. No transmitter or USB Bluetooth adapter is
available. A rear-label photo shows an LG label, but its model field is unreadable;
no model or physical output connector has been established from that photo.
The inspected HDMI sysfs interfaces did not yield a readable model name.

1. **Check the TV's own Bluetooth audio output first.** On supported LG models,
   look under Settings → Sound → Sound Out for Bluetooth or Use Wireless Speaker.
   Pair the headphones/speaker there. Menus and support depend on the actual TV
   model; an LG label alone does not establish this feature.
2. **If the TV lacks Bluetooth audio, identify its audio-output connector.** A
   Bluetooth transmitter in TX mode can receive audio from a compatible headphone,
   optical or RCA **output**, then pair directly with the headphones/speaker.
   Confirm the connector before selecting hardware. For optical transmitters
   requiring stereo PCM, configure the TV accordingly. USB may only supply power
   to such a transmitter; its audio cable remains necessary.
3. **A USB audio Bluetooth transmitter is another test candidate.** Read-only MBOX
   checks found `snd-usb-audio`, `/system/lib/hw/audio.usb.default.so`, and USB device
   output/input entries in `/system/etc/audio_policy.conf`. Android supports a
   subset of USB Audio Class 1 in host mode from Android 5.0. This is evidence of
   a potential USB sound-card path, not proof that any particular transmitter
   works on this firmware. A suitable device would handle Bluetooth pairing
   itself and accept compatible USB audio. A generic USB Bluetooth radio is a
   different device and remains unverified here.

The same checks still found no advertised Android Bluetooth feature, no exposed
HCI controller and only USB root hubs. The available ALSA devices were RK322x,
HDMI and SPDIF; these software interfaces do not establish physical output ports.
Private audio evidence: `20261025T083044+0100`. All checks were read-only. No
purchase, driver installation, radio enablement or firmware change was performed.

The immediate next step is to inspect the **TV's** Sound Out menu. If Bluetooth
is absent, obtain a clear model number or a photo of the port labels before
choosing a transmitter. No currently available hardware has been confirmed to
provide Bluetooth audio.

References:

- [LG: connecting a Bluetooth device on supported TVs](https://www.lg.com/levant_en/support/product-help/CT20158005-20155394279226)
- [Avantree: TV transmitter connections and output requirements](https://support.avantree.com/hc/en-us/articles/30656341975193-Set-Up-1-Connect-HT5006-Transmitter-to-your-TV-Audio-Source)
- [AOSP: USB digital audio support and audio policy](https://source.android.com/docs/core/audio/usb)
