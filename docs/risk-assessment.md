# Recommendation and qualitative risks

**Recommendation: remain on the current firmware while identification and independent recovery are unresolved.** No verified Android TV upgrade candidate exists for this particular PCB/DDR/SSV6158 combination in the checked evidence. No flashing/reboot/backup authorization is implied by the investigation.

| Risk | Assessment | Why it affects the decision |
|---|---|---|
| Wrong DDR loader / PCB | High consequence, unresolved likelihood | Can prevent boot and accessible recovery; generic RK3229 identity is insufficient |
| Lost Wi-Fi / remote / HDMI / decode | Material compatibility risk | Different modules/GPIO/IR maps/vendor media stacks; no target tests |
| Unable to restore | Critical unresolved risk | No independent USB or external boot path demonstrated; enclosure cannot be opened |
| Misaddressed restore | High consequence | Command-line offsets differ from kernel absolute LBAs by 0x2000 sectors |
| Bad backup transport | Identified and mitigated in diagnostic collector | Root binary stream changed LF bytes; future backup must encode inside su and verify source/host hashes |
| Live userdata inconsistency | Material | Filesystems continue changing; checksum alone cannot establish application consistency |
| ROM provenance/version | Unresolved | Spoofed Android release already observed; most candidate binaries/API levels uninspected |
| New OS performance | Uncertain benefit | 1 GiB RAM, slow 32-bit cores and old GPU; heavier framework/GMS can increase pressure |

## Requested decision points

1. **Best verified replacement:** none found. Current Android remains the only known-running baseline. The SV6158 DCBOX listing is the closest hardware lead; its Android version, board match and authenticity remain unknown. TechLife ATV9/10 require different advertised Wi-Fi support and are unverified here.
2. **Realistic Android version:** 7.1.2/API 25 is strongly supported by current observations. Published RK322x 8.1/9/10 claims do not establish a safe upgrade path for this unit; there is no verified Android 11+ result. No recommendation is based on the displayed 11.1 value.
3. **Expected performance:** a compact system may suit a single lightweight playback task when acceleration works. Modern multitasking, browsers and heavier GMS are likely to strain 1 GiB; this is engineering expectation, not a benchmark. No guarantee a newer version is faster.
4. **Hardware limitations:** unknown physical PCB/DDR, SSV6158 driver integration, remote keymaps, HDMI audio and vendor decoding; current Bluetooth DT is disabled. DRM, certification and streaming-app eligibility are unverified. OS flashing does not establish any service certification.
5. **Backup/restore readiness:** storage capacity is sufficient on the Mac; plans/verifier exist; no firmware backup or usable restore route exists yet.
6. **Exact installation:** withheld from execution because there is no approved compatible artifact and no verified recovery. See the explicit readiness sequence in recovery-plan.md; naming a generic FactoryTool Restore action would not be a safe exact procedure for this box.
7. **Failure risk:** material and presently unacceptable for a no-disassembly device, especially if bootloader changes are involved. No invented percentages.
8. **Rollback:** requires independently proven recovery plus verified original full raw user-area backup including unnamed prefix. Not presently ready.
9. **Proceed/stay:** stay. First preserve recovery options; consider firmware only if the missing evidence is resolved.

## Alternatives

The user has explicitly confirmed that the required system is a complete Android TV OS with native Android apps and TV/remote navigation; a generic tablet build or launcher replacement is insufficient. Armbian and LibreELEC are excluded as final OS solutions. Retain their research only for recovery/hardware reference. No container/emulation route has been validated, and it is not an established alternative on this 1 GiB box.

For the required Android application support, keep the existing installation for now. A later, separately approved performance pass could measure app startup/memory and discuss reversible app/service or launcher changes; no packages/services have been disabled and a launcher is not an OS upgrade. A supported, certified external streaming device is the safer route to modern Android TV app support when no compatible ROM/recovery can be established; no specific hardware purchase is recommended without checking current availability, support and the user's apps/budget.
