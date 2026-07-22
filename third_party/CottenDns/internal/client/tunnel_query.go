// ==============================================================================
// CottenDNS
// Author: tajirax
// Github: https://github.com/TaJirax/CottenDns
// Year: 2026
// ==============================================================================
// Package client provides the core logic for the CottenDns client.
// This file (tunnel_query.go) handles the construction of DNS tunnel queries.
// ==============================================================================
package client

import (
	DnsParser "cottendns-go/internal/dnsparser"
	Enums "cottendns-go/internal/enums"
	VpnProto "cottendns-go/internal/vpnproto"
)

type preparedTunnelDomain struct {
	normalized string
	qname      []byte
}

// normalizeRuntimeQueryTypes is a defensive guard for the runtime query-type
// set: config finalization already guarantees a non-empty slice, but a Client
// constructed in a test without going through config validation might pass nil.
// In that case fall back to TXT-only (the historical behavior).
func normalizeRuntimeQueryTypes(codes []uint16) []uint16 {
	if len(codes) == 0 {
		return []uint16{Enums.DNS_RECORD_TYPE_TXT}
	}
	out := make([]uint16, len(codes))
	copy(out, codes)
	return out
}

// nextQueryType returns the DNS record type to use for the next tunnel query,
// rotating round-robin over the configured set (A1). The upstream server reads
// the tunnel payload from the QNAME labels regardless of qType, so rotation is
// purely a query-fingerprint measure and never affects decodability. A
// single-element set always returns that one type (e.g. the default TXT-only).
func (c *Client) nextQueryType() uint16 {
	if c == nil {
		return Enums.DNS_RECORD_TYPE_TXT
	}
	// Adaptive selection with automatic fallback (steers away from blocked/poisoned
	// carriers, re-explores recovered ones). See carrier_selector.go.
	if c.carrier != nil {
		return c.carrier.next()
	}
	// Fallback for directly-constructed test clients: plain round-robin.
	if len(c.queryTypes) == 0 {
		return Enums.DNS_RECORD_TYPE_TXT
	}
	if len(c.queryTypes) == 1 {
		return c.queryTypes[0]
	}
	idx := c.queryTypeCursor.Add(1) - 1
	return c.queryTypes[int(idx%uint32(len(c.queryTypes)))]
}

func (c *Client) nextQueryTypeForPath(path string) uint16 {
	if c != nil && c.carrier != nil && path != "" {
		return c.carrier.nextForPath(path)
	}
	return c.nextQueryType()
}

// queryShaping snapshots the client's per-query DNS-shaping settings for the
// wire builder. All fields are client-only and server-transparent.
func (c *Client) queryShaping() DnsParser.QueryShaping {
	return DnsParser.QueryShaping{
		EDNSUDPSize:   c.ednsUDPSize,
		RandomizeID:   c.dnsRandomizeID,
		EDNSCookie:    c.dnsEDNSCookie,
		CaseRandomize: c.dnsCaseRandomize,
	}
}

func (c *Client) buildTunnelTXTQuestionBytes(domain string, encoded []byte, qType uint16) ([]byte, error) {
	normalized, qname, err := DnsParser.PrepareTunnelDomainQname(domain)
	if err != nil {
		return nil, err
	}
	return DnsParser.BuildTunnelQuestionPacketShaped(normalized, qname, encoded, qType, c.queryShaping())
}

func prepareTunnelDomain(domain string) (preparedTunnelDomain, error) {
	normalized, qname, err := DnsParser.PrepareTunnelDomainQname(domain)
	if err != nil {
		return preparedTunnelDomain{}, err
	}
	return preparedTunnelDomain{normalized: normalized, qname: qname}, nil
}

func (c *Client) buildTunnelTXTQuestionBytesPrepared(domain preparedTunnelDomain, encoded []byte, qType uint16) ([]byte, error) {
	return DnsParser.BuildTunnelQuestionPacketShaped(domain.normalized, domain.qname, encoded, qType, c.queryShaping())
}

// buildTunnelTXTQueryRaw builds an encoded tunnel query using the provided options and codec.
func (c *Client) buildTunnelTXTQueryRaw(domain string, options VpnProto.BuildOptions) ([]byte, error) {
	raw, err := VpnProto.BuildRaw(options)
	if err != nil {
		return nil, err
	}
	encoded, err := c.codec.EncryptAndEncodeBytes(raw)
	if err != nil {
		return nil, err
	}
	return c.buildTunnelTXTQuestionBytes(domain, encoded, c.nextQueryType())
}

func (c *Client) buildEncodedAutoWithCompressionTrace(options VpnProto.BuildOptions) ([]byte, error) {
	raw, err := VpnProto.BuildRawAuto(options, c.effectiveCompressionMinSize())
	if err != nil {
		return nil, err
	}

	if c.codec == nil {
		return nil, VpnProto.ErrCodecUnavailable
	}
	return c.codec.EncryptAndEncodeBytes(raw)
}

// buildTunnelTXTQuery builds an encoded tunnel query with automatic option handling.
func (c *Client) buildTunnelTXTQuery(domain string, options VpnProto.BuildOptions) ([]byte, error) {
	encoded, err := c.buildEncodedAutoWithCompressionTrace(options)
	if err != nil {
		return nil, err
	}
	return c.buildTunnelTXTQuestionBytes(domain, encoded, c.nextQueryType())
}
