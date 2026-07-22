// ==============================================================================
// CottenDNS
// Author: tajirax
// Github: https://github.com/TaJirax/CottenDns
// Year: 2026
// ==============================================================================
// Package client provides the core logic for the CottenDns client.
// This file (traffic_stats.go) implements the periodic traffic speed and total
// bytes reporter that prints to the console log window.
// ==============================================================================

package client

import (
	"context"
	"fmt"
	"time"
)

// formatBytes formats a raw byte count into a human-readable size string.
func formatBytes(n uint64) string {
	switch {
	case n >= 1<<30:
		return fmt.Sprintf("%.2f GB", float64(n)/float64(1<<30))
	case n >= 1<<20:
		return fmt.Sprintf("%.2f MB", float64(n)/float64(1<<20))
	case n >= 1<<10:
		return fmt.Sprintf("%.2f KB", float64(n)/float64(1<<10))
	default:
		return fmt.Sprintf("%d B", n)
	}
}

// formatSpeed formats a bytes-per-second value into a human-readable speed string.
func formatSpeed(bytesPerSec float64) string {
	switch {
	case bytesPerSec >= float64(1<<30):
		return fmt.Sprintf("%.2f GB/s", bytesPerSec/float64(1<<30))
	case bytesPerSec >= float64(1<<20):
		return fmt.Sprintf("%.2f MB/s", bytesPerSec/float64(1<<20))
	case bytesPerSec >= float64(1<<10):
		return fmt.Sprintf("%.2f KB/s", bytesPerSec/float64(1<<10))
	default:
		return fmt.Sprintf("%.0f B/s", bytesPerSec)
	}
}

// runTrafficStatsReporter periodically logs upload/download speed and session
// totals to the console. It runs as a goroutine inside the async runtime and
// exits when ctx is cancelled.
func (c *Client) runTrafficStatsReporter(ctx context.Context) {
	interval := c.cfg.StatsReportInterval()
	if interval <= 0 {
		return
	}

	ticker := time.NewTicker(interval)
	defer ticker.Stop()

	var lastTX, lastRX uint64
	lastTime := time.Now()

	for {
		select {
		case <-ctx.Done():
			return
		case now := <-ticker.C:
			currentTX := c.txTotalBytes.Load()
			currentRX := c.rxTotalBytes.Load()

			elapsed := now.Sub(lastTime).Seconds()
			var upSpeed, downSpeed float64
			if elapsed > 0 {
				upSpeed = float64(currentTX-lastTX) / elapsed
				downSpeed = float64(currentRX-lastRX) / elapsed
			}

			lastTX = currentTX
			lastRX = currentRX
			lastTime = now

			if c.log != nil {
				lossPM := c.tunnelLossPerMille()
				activeResolvers := c.balancer.ValidCount()
				c.log.Infof(
					"\U0001F4CA <cyan>↑</cyan> <yellow>%s</yellow> <gray>(Total: %s)</gray> <magenta>|</magenta> <cyan>↓</cyan> <yellow>%s</yellow> <gray>(Total: %s)</gray> <magenta>|</magenta> <cyan>loss</cyan> <yellow>%.1f%%</yellow> <magenta>|</magenta> <cyan>resolvers</cyan> <yellow>%d</yellow> <magenta>|</magenta> <cyan>transport</cyan> <yellow>%s</yellow> <magenta>|</magenta> <cyan>queues</cyan> <yellow>%d/%d/%d</yellow> <magenta>|</magenta> <cyan>drops</cyan> <yellow>rx=%d tx=%d</yellow> <magenta>|</magenta> <cyan>recoveries</cyan> <yellow>%d</yellow> <magenta>|</magenta> <cyan>stream-fail</cyan> <yellow>dial=%d write=%d</yellow>",
					formatSpeed(upSpeed),
					formatBytes(currentTX),
					formatSpeed(downSpeed),
					formatBytes(currentRX),
					float64(lossPM)/10.0,
					activeResolvers,
					c.activeTransport(),
					len(c.txChannel), len(c.encodedTXChannel), len(c.rxChannel),
					c.rxDroppedPackets.Load(), c.txAdmissionDrops.Load(),
					c.transportRecoveryCount.Load(),
					c.streamDialFailures.Load(), c.streamWriteFailures.Load(),
				)
			}
		}
	}
}
