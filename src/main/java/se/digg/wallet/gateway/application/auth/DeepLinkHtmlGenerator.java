// SPDX-FileCopyrightText: 2025 Digg - Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.digg.wallet.gateway.application.auth;

import org.springframework.stereotype.Component;


@Component
public class DeepLinkHtmlGenerator {

  public String generate(String sessionId) {
    return """
        <!doctype html>
        <html lang="en">
          <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>Continue</title>
          </head>
          <body>
            <p>Opening the app…</p>
            <p>
              <a id="appLink" href="wallet-app://session?session_id={{sessionId}}">
                Open Wallet Session
              </a>
            </p>
            <script>
              window.addEventListener("load", () => {
                const url = document.getElementById("appLink").href;
                setTimeout(() => { window.location.href = url; }, 50);
              });
            </script>
          </body>
        </html>
        """.replace("{{sessionId}}", sessionId);
  }
}
