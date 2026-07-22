package udpframe

import (
	"encoding/binary"
	"errors"
	"net"
)

const (
	AddressTypeIPv4   = 0x01
	AddressTypeDomain = 0x03
	AddressTypeIPv6   = 0x04
	MaxFramePayload   = 65535
)

var (
	AssociationMarker = []byte{0x00, 'C', 'D', 'U', 0x01}
	ErrFrameTooLarge  = errors.New("UDP frame exceeds 65535 bytes")
	ErrInvalidFrame   = errors.New("invalid UDP datagram frame")
)

// Encode wraps one SOCKS-addressed UDP datagram in a two-byte length prefix.
// The prefix is required because the reliable tunnel is a byte stream and may
// split a datagram across several DNS-sized STREAM_DATA packets.
func Encode(addressType byte, host string, port uint16, payload []byte) ([]byte, error) {
	body := make([]byte, 0, 1+16+2+len(payload))
	body = append(body, addressType)
	switch addressType {
	case AddressTypeIPv4:
		ip := net.ParseIP(host).To4()
		if ip == nil {
			return nil, ErrInvalidFrame
		}
		body = append(body, ip...)
	case AddressTypeIPv6:
		ip := net.ParseIP(host).To16()
		if ip == nil || ip.To4() != nil {
			return nil, ErrInvalidFrame
		}
		body = append(body, ip...)
	case AddressTypeDomain:
		if len(host) == 0 || len(host) > 255 {
			return nil, ErrInvalidFrame
		}
		body = append(body, byte(len(host)))
		body = append(body, host...)
	default:
		return nil, ErrInvalidFrame
	}
	body = binary.BigEndian.AppendUint16(body, port)
	body = append(body, payload...)
	if len(body) > MaxFramePayload {
		return nil, ErrFrameTooLarge
	}
	frame := make([]byte, 2, len(body)+2)
	binary.BigEndian.PutUint16(frame, uint16(len(body)))
	return append(frame, body...), nil
}

// DecodeBody parses the body following the two-byte frame length.
func DecodeBody(body []byte) (addressType byte, host string, port uint16, payload []byte, err error) {
	if len(body) < 1+2 {
		return 0, "", 0, nil, ErrInvalidFrame
	}
	addressType = body[0]
	offset := 1
	switch addressType {
	case AddressTypeIPv4:
		if len(body) < offset+4+2 {
			return 0, "", 0, nil, ErrInvalidFrame
		}
		host = net.IP(body[offset : offset+4]).String()
		offset += 4
	case AddressTypeIPv6:
		if len(body) < offset+16+2 {
			return 0, "", 0, nil, ErrInvalidFrame
		}
		host = net.IP(body[offset : offset+16]).String()
		offset += 16
	case AddressTypeDomain:
		if len(body) < offset+1 {
			return 0, "", 0, nil, ErrInvalidFrame
		}
		n := int(body[offset])
		offset++
		if n == 0 || len(body) < offset+n+2 {
			return 0, "", 0, nil, ErrInvalidFrame
		}
		host = string(body[offset : offset+n])
		offset += n
	default:
		return 0, "", 0, nil, ErrInvalidFrame
	}
	port = binary.BigEndian.Uint16(body[offset : offset+2])
	if port == 0 {
		return 0, "", 0, nil, ErrInvalidFrame
	}
	payload = body[offset+2:]
	return addressType, host, port, payload, nil
}

// Pop removes one complete frame from buffered data. Incomplete data is left
// untouched so callers can append the next stream chunk without copying it.
func Pop(buffer []byte) (body []byte, rest []byte, ready bool, err error) {
	if len(buffer) < 2 {
		return nil, buffer, false, nil
	}
	n := int(binary.BigEndian.Uint16(buffer[:2]))
	if n < 3 {
		return nil, nil, false, ErrInvalidFrame
	}
	if len(buffer) < n+2 {
		return nil, buffer, false, nil
	}
	return buffer[2 : n+2], buffer[n+2:], true, nil
}
