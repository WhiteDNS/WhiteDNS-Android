package udpframe

import (
	"bytes"
	"testing"
)

func TestFrameRoundTripAndPartialPop(t *testing.T) {
	want := []byte("quic datagram")
	frame, err := Encode(AddressTypeIPv6, "2001:4860:4860::8888", 443, want)
	if err != nil {
		t.Fatal(err)
	}
	if _, _, ready, err := Pop(frame[:4]); err != nil || ready {
		t.Fatalf("partial frame ready=%v err=%v", ready, err)
	}
	body, rest, ready, err := Pop(frame)
	if err != nil || !ready || len(rest) != 0 {
		t.Fatalf("complete frame ready=%v rest=%d err=%v", ready, len(rest), err)
	}
	atyp, host, port, got, err := DecodeBody(body)
	if err != nil || atyp != AddressTypeIPv6 || host != "2001:4860:4860::8888" || port != 443 || !bytes.Equal(got, want) {
		t.Fatalf("decoded atyp=%d host=%q port=%d payload=%q err=%v", atyp, host, port, got, err)
	}
}

func TestFrameRejectsInvalidTargets(t *testing.T) {
	if _, err := Encode(AddressTypeIPv4, "not-an-ip", 53, nil); err == nil {
		t.Fatal("expected invalid IPv4 error")
	}
	if _, _, _, _, err := DecodeBody([]byte{AddressTypeIPv4, 1, 2, 3}); err == nil {
		t.Fatal("expected truncated body error")
	}
}
