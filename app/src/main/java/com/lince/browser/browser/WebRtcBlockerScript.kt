package com.lince.browser.browser

internal object WebRtcBlockerScript {
    val installScript = """
        (() => {
          "use strict";
          const blocked = function RTCPeerConnection() {
            throw new DOMException("WebRTC is disabled by Lince.", "NotAllowedError");
          };
          for (const name of ["RTCPeerConnection", "webkitRTCPeerConnection", "mozRTCPeerConnection"]) {
            try {
              Object.defineProperty(globalThis, name, {
                configurable: false,
                enumerable: false,
                get: () => blocked,
                set: () => {},
              });
            } catch (_) {
              try { globalThis[name] = blocked; } catch (_) {}
            }
          }
        })();
    """.trimIndent()
}
