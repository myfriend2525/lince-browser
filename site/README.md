# Lince Browser website

Static, dependency-free GitHub Pages site for Lince Browser: landing page, privacy policy and legal
notice in Spanish (root) and English (`en/`).

| Path | Content |
| --- | --- |
| `index.html`, `en/index.html` | Landing page (hero, features, editions, downloads, install steps) |
| `privacy/`, `en/privacy/` | Privacy policy |
| `legal/`, `en/legal/` | Legal notice, licenses and credits |
| `config.js` | Download and GitHub links — the only file to edit once the repository exists |
| `main.js` | Shared behavior: links, floating nav, hero entrance, rotating word, letter-by-letter titles, count-up figures, install steps |
| `styles.css` | All styles; the hero is a 1440×1280 frame scaled with `--u = 100vw / 1440` |
| `assets/fonts/` | Self-hosted Grandstander and Inter (SIL OFL 1.1) |

## Download links

While `repository` in `config.js` is empty, download buttons show "Próximamente / Coming soon" and
GitHub links (source, support) are hidden. Set it to the repository URL, for example
`https://github.com/your-user/lince-browser`, and the buttons point to the latest GitHub release.
`geckoApk` and `webviewApk` can hold direct APK links; `apkPattern` must match the GeckoView APK
name published by the release workflow so Obtainium picks the right file.

## Local preview

From the repository root:

```sh
python3 -m http.server 4173 --directory site
```

Open `http://127.0.0.1:4173/`. Opening `site/index.html` directly from disk also works.

## External media

The hero illustration and its looping video are loaded from an external CDN (see `index.html`).
Replace both URLs with files under `assets/` before relying on them long term.

## Deployment

`.github/workflows/pages.yml` deploys this directory after a push to `main`. Configure the
repository's Pages source as **GitHub Actions** before the first deployment.
