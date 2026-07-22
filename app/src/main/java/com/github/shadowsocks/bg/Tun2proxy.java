package com.github.shadowsocks.bg;

public final class Tun2proxy {
    public static final int DNS_VIRTUAL = 0;
    public static final int DNS_OVER_TCP = 1;
    public static final int DNS_DIRECT = 2;

    public static final int VERBOSITY_OFF = 0;
    public static final int VERBOSITY_ERROR = 1;
    public static final int VERBOSITY_WARN = 2;
    public static final int VERBOSITY_INFO = 3;
    public static final int VERBOSITY_DEBUG = 4;
    public static final int VERBOSITY_TRACE = 5;

    static {
        System.loadLibrary("tun2proxy");
    }

    private Tun2proxy() {
    }

    public static native int run(String cliArgs, char tunMtu);

    public static native int stop();
}
