package client

import (
	"testing"
	"time"

	Enums "cottendns-go/internal/enums"
)

func TestCarrierSelector_SingleTypeIsPassthrough(t *testing.T) {
	s := newCarrierSelector([]uint16{Enums.DNS_RECORD_TYPE_TXT}, nil)
	for i := 0; i < 10; i++ {
		if got := s.next(); got != Enums.DNS_RECORD_TYPE_TXT {
			t.Fatalf("single-type selector must always return TXT, got %d", got)
		}
	}
}

// A carrier whose responses stop coming back (blocked/poisoned) is dropped from
// rotation, the working carrier stays, and the dropped one is re-explored later.
func TestCarrierSelector_DropsBlockedCarrierAndReexplores(t *testing.T) {
	now := time.Now()
	s := newCarrierSelector(
		[]uint16{Enums.DNS_RECORD_TYPE_TXT, Enums.DNS_RECORD_TYPE_CNAME},
		func() time.Time { return now },
	)
	txt := uint16(Enums.DNS_RECORD_TYPE_TXT)
	cname := uint16(Enums.DNS_RECORD_TYPE_CNAME)

	// send n queries; TXT always answers, CNAME never does (blocked).
	send := func(n int) map[uint16]int {
		counts := map[uint16]int{}
		for i := 0; i < n; i++ {
			qt := s.next()
			counts[qt]++
			if qt == txt {
				s.recordSuccess(txt)
			}
		}
		return counts
	}

	send(200) // ~100 each, enough to exceed carrierMinSamples

	// Trigger an eval.
	now = now.Add(carrierEvalInterval + time.Second)
	counts := send(100)
	if counts[txt] == 0 {
		t.Fatal("working TXT carrier must stay in rotation")
	}
	if counts[cname] > counts[txt]/4 {
		t.Fatalf("blocked CNAME should be largely dropped: txt=%d cname=%d", counts[txt], counts[cname])
	}

	// Re-exploration: with ongoing TXT traffic, CNAME's send count decays until it
	// drops below carrierMinSamples and re-enters rotation for a retry.
	reappeared := false
	for k := 0; k < 10 && !reappeared; k++ {
		now = now.Add(carrierEvalInterval + time.Second)
		send(10)
		for _, ti := range *s.active.Load() {
			if s.types[ti] == cname {
				reappeared = true
			}
		}
	}
	if !reappeared {
		t.Fatal("dropped carrier should be re-explored after decay")
	}
}

func TestCarrierSelector_IsolatesBlockedCarrierByPath(t *testing.T) {
	now := time.Now()
	s := newCarrierSelector([]uint16{Enums.DNS_RECORD_TYPE_TXT, Enums.DNS_RECORD_TYPE_CNAME}, func() time.Time { return now })
	for i := 0; i < 200; i++ {
		if qt := s.nextForPath("blocked"); qt == Enums.DNS_RECORD_TYPE_TXT {
			s.recordSuccessForPath("blocked", qt)
		}
		qt := s.nextForPath("clean")
		s.recordSuccessForPath("clean", qt)
	}
	now = now.Add(carrierEvalInterval + time.Second)
	blockedCNAME, cleanCNAME := 0, 0
	for i := 0; i < 100; i++ {
		if s.nextForPath("blocked") == Enums.DNS_RECORD_TYPE_CNAME {
			blockedCNAME++
		}
		if s.nextForPath("clean") == Enums.DNS_RECORD_TYPE_CNAME {
			cleanCNAME++
		}
	}
	if blockedCNAME >= cleanCNAME/2 {
		t.Fatalf("blocked carrier leaked across path scores: blocked=%d clean=%d", blockedCNAME, cleanCNAME)
	}
}
