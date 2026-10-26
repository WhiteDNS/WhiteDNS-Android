# Manual DNS server entry and reachability checks

The Scan tab can scan three resolver sources: the built-in default list, a resolver
file, and — since this change — servers typed or pasted by hand. Manual servers are
also probed from the device's current network before the scan starts, so an
unreachable resolver is visible before it costs scan time.

## Entering servers by hand

1. Open **Scan** and choose **Enter servers manually**.
2. Type one server per line, or press **Paste from clipboard** to fill the box from
   the clipboard.
3. Confirming with **Add** prepares the scan exactly like the other two sources:
   the status card shows `Source: Manual entry`, and **Start** begins the scan.

The counter under the box reports how many valid servers the text contains and how
many entries were rejected, so a typo is visible before scanning.

### Accepted input

- One server per line, or comma/semicolon separated within a line.
- `#` starts a comment; blank lines are ignored.
- Default port is 53 and is dropped from the stored list; any other port is kept
  (`1.1.1.1:5353` stays `1.1.1.1:5353`).
- IPv6 addresses work bare (`2606:4700:4700::1111`) or bracketed with a port
  (`[2606:4700:4700::1111]:5353`).
- Duplicates, including duplicates that differ only by `:53`, collapse to one entry.
- Entries already present in the scanner result for the selected engine are counted
  as already valid and are not scanned again.

## Reachability from the current network

As soon as the box holds at least one valid server, each one is probed automatically
and its result is listed under **Reachability from this network**:

- `NN ms` — the server answered;
- `no response` — it did not answer over UDP and did not accept a TCP connection.

**Test again** repeats the run for the whole set, for example after switching between
Wi-Fi and mobile data.

### What the probe actually measures

- A real DNS query (`example.com`, type A) is sent over UDP to port 53, or to the
  explicit port in the entry, and the round trip is measured.
- If UDP does not answer within 1.5 seconds, a TCP connection to the same
  host and port is attempted; a successful handshake also counts as reachable.
- There is **no ICMP ping**. Android does not allow ordinary apps to send ICMP, so a
  DNS round trip is the honest equivalent of "is this resolver usable from here".
- At most 24 servers are probed per run, in batches of 8, to keep the dialog responsive
  and avoid hammering the network. When the list is longer, the dialog shows how many
  entries were tested.
- Closing the dialog cancels the run; a probe already in flight ends with its own
  1.5-second timeout. The text is re-parsed 600 ms after the last keystroke, so fast
  typing does not start a probe per character.

### Reading the result

`no response` does not always mean the server is down. The most common causes are:

- the current network blocks outbound port 53 to that address;
- the entry uses a port the server does not serve;
- the address is stale or is not a resolver at all.

A reachable result only says the resolver answers from this network. Whether it
returns correct answers for the tunnel remains the job of the scan itself.

## Privacy

The only network traffic this feature adds is the probe described above: one DNS
query (plus one TCP attempt when UDP fails) per listed server. No other host is
contacted, no result is uploaded anywhere, and nothing is stored beyond the existing
scan files.
