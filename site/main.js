// Lince Browser landing: download links, scene video, entrance and the five effects.
const reduceMotion = matchMedia('(prefers-reduced-motion: reduce)').matches;
const wait = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const whenVisible = (element, callback, threshold = .35) => {
  if (!('IntersectionObserver' in window)) return callback();
  const observer = new IntersectionObserver(([entry]) => {
    if (!entry.isIntersecting) return;
    observer.disconnect();
    callback();
  }, { threshold });
  observer.observe(element);
};

// Download and GitHub links come from config.js.
const linceLinks = window.LINCE_LINKS || {};
const repository = (linceLinks.repository || '').replace(/\/+$/, '');
if (repository) {
  const releases = `${repository}/releases/latest`;
  const obtainiumApp = {
    id: 'com.lince.browser',
    url: repository,
    author: 'Lince Browser Team',
    name: 'Lince Browser',
    preferredApkIndex: 0,
    additionalSettings: JSON.stringify({ apkFilterRegEx: linceLinks.apkPattern || '' }),
  };
  const destinations = {
    releases,
    gecko: linceLinks.geckoApk || releases,
    webview: linceLinks.webviewApk || releases,
    obtainium: `https://apps.obtainium.imranr.dev/redirect?r=${encodeURIComponent(`obtainium://app/${JSON.stringify(obtainiumApp)}`)}`,
    source: repository,
    issues: `${repository}/issues`,
  };
  document.querySelectorAll('[data-link]').forEach((link) => {
    if (destinations[link.dataset.link]) link.href = destinations[link.dataset.link];
  });
  document.documentElement.classList.add('has-repository');

  // Direct APK downloads: ask GitHub for the latest release and link each edition's APK.
  // Without a release or a matching file, the buttons keep pointing at the Releases page.
  const editions = [
    { link: 'gecko', fixed: linceLinks.geckoApk, pattern: linceLinks.apkPattern },
    { link: 'webview', fixed: linceLinks.webviewApk, pattern: linceLinks.webviewApkPattern },
  ].filter(({ link, fixed, pattern }) => !fixed && pattern && document.querySelector(`[data-link="${link}"]`));
  const repositoryPath = repository.match(/github\.com\/([^/]+\/[^/]+)/)?.[1];
  if (editions.length && repositoryPath && 'fetch' in window) {
    fetch(`https://api.github.com/repos/${repositoryPath}/releases/latest`, { headers: { Accept: 'application/vnd.github+json' } })
      .then((response) => (response.ok ? response.json() : null))
      .then((release) => {
        const assets = release?.assets || [];
        editions.forEach(({ link, pattern }) => {
          const matcher = new RegExp(pattern);
          const asset = assets.find(({ name }) => matcher.test(name));
          if (!asset) return;
          document.querySelectorAll(`[data-link="${link}"]`).forEach((button) => {
            button.href = asset.browser_download_url;
            button.title = asset.name;
          });
        });
      })
      .catch(() => {});
  }
} else {
  document.querySelectorAll('[data-requires-repository]').forEach((element) => element.remove());
}

// Community invite: a dismissible card once the hero has settled, remembered per browser.
const telegram = linceLinks.telegram || '';
const community = document.querySelector('[data-community]');
document.querySelectorAll('[data-link="telegram"]').forEach((link) => {
  if (telegram) link.href = telegram;
  else link.remove();
});
if (community && telegram) {
  const dismissedKey = 'lince-community-dismissed';
  const isDismissed = () => {
    try { return localStorage.getItem(dismissedKey) === '1'; } catch { return false; }
  };
  const dismiss = () => {
    try { localStorage.setItem(dismissedKey, '1'); } catch {}
    community.classList.remove('is-open');
    setTimeout(() => { community.hidden = true; }, reduceMotion ? 0 : 300);
  };
  community.querySelector('[data-community-close]').addEventListener('click', dismiss);
  community.querySelector('[data-community-join]').addEventListener('click', dismiss);
  addEventListener('keydown', (event) => {
    if (event.key === 'Escape' && community.classList.contains('is-open')) dismiss();
  });
  if (!isDismissed()) {
    setTimeout(() => {
      community.hidden = false;
      void community.offsetWidth;
      community.classList.add('is-open');
    }, 3500);
  }
} else if (community) {
  community.remove();
}

// Scene video: only with motion allowed; it fades in over the identical still.
const video = document.querySelector('.scene video');
if (!video) {
  // Inner pages have no scene.
} else if (reduceMotion) {
  video.remove();
} else {
  video.style.opacity = '0';
  video.addEventListener('playing', () => video.animate([{ opacity: 0 }, { opacity: 1 }], { duration: 400, fill: 'forwards' }), { once: true });
  video.src = video.dataset.src;
  video.play().catch(() => {});
}

// Cap-top alignment for the display face.
document.fonts.load('900 100px Grandstander').then(() => {
  const metrics = document.createElement('canvas').getContext('2d');
  metrics.font = '900 100px Grandstander';
  const m = metrics.measureText('H');
  const capOffset = ((100 - m.fontBoundingBoxAscent - m.fontBoundingBoxDescent) / 2 + m.fontBoundingBoxAscent - m.actualBoundingBoxAscent) / 100;
  if (Number.isFinite(capOffset)) document.documentElement.style.setProperty('--cap-offset', capOffset.toFixed(4));
});

// 1. Floating navigation once the page scrolls.
const nav = document.querySelector('[data-floating-nav]');
const updateNav = () => nav.classList.toggle('is-floating', nav.dataset.floatingNav === 'always' || scrollY > 24);
addEventListener('scroll', updateNav, { passive: true });
updateNav();

// Hero entrance: one timeline, run once.
const entranceDone = (() => {
  if (reduceMotion || !('animate' in Element.prototype)) return Promise.resolve();
  const ease = 'cubic-bezier(.2, .9, .25, 1)';
  const steps = {
    drop: { keyframes: [{ opacity: 0, transform: 'translateY(-24px)' }, { opacity: 1, transform: 'none' }], duration: 700, easing: ease },
    unblur: { keyframes: [{ opacity: 0, filter: 'blur(12px)' }, { opacity: 1, filter: 'blur(0)' }], duration: 800, easing: ease },
    spring: { keyframes: [{ opacity: 0, filter: 'blur(14px)', transform: 'translateY(60px) scale(.96)' }, { opacity: 1, filter: 'blur(0)', transform: 'none' }], duration: 900, easing: 'cubic-bezier(.3, 1.45, .45, 1)' },
    rise: { keyframes: [{ opacity: 0, transform: 'translateY(28px)' }, { opacity: 1, transform: 'none' }], duration: 750, easing: ease },
  };
  const delays = { drop: [0, 60, 120], unblur: [320], spring: [520, 680] };
  let riseAt = 1000;
  const animations = [...document.querySelectorAll('[data-enter]')].map((element) => {
    const kind = element.dataset.enter;
    const delay = kind === 'rise' ? (riseAt += 110) - 110 : delays[kind].shift();
    const { keyframes, duration, easing } = steps[kind];
    return element.animate(keyframes, { duration, easing, delay, fill: 'backwards' }).finished;
  });
  return Promise.all(animations).catch(() => {});
})();

// 2. Rotating word: letters un-blur one by one in amber tones, then settle to white.
const word = document.querySelector('[data-rotating-word]');
if (word && !reduceMotion) {
  const words = word.dataset.rotatingWord.split(',');
  const amber = ['#ffe08a', '#ffc04d', '#ff9a3d', '#ff7a2e', '#ffe08a'];
  const toRgb = (hex) => [1, 3, 5].map((i) => parseInt(hex.slice(i, i + 2), 16));
  const colorAt = (position) => {
    const scaled = position * (amber.length - 1);
    const lower = Math.floor(scaled);
    const upper = Math.min(lower + 1, amber.length - 1);
    const t = scaled - lower;
    const [a, b] = [toRgb(amber[lower]), toRgb(amber[upper])];
    return `rgb(${a.map((value, i) => Math.round(value + (b[i] - value) * t)).join(',')})`;
  };
  const show = (text) => {
    word.replaceChildren(...[...text].map((char, i, letters) => {
      const letter = document.createElement('span');
      letter.textContent = char;
      letter.style.color = colorAt(i / Math.max(letters.length - 1, 1));
      letter.animate(
        [{ opacity: 0, filter: 'blur(20px)' }, { opacity: 1, filter: 'blur(0)' }],
        { duration: 500, delay: i * 45, easing: 'cubic-bezier(.33, 1, .68, 1)', fill: 'backwards' },
      );
      return letter;
    }));
    setTimeout(() => word.querySelectorAll('span').forEach((letter) => { letter.style.color = ''; }), text.length * 45 + 700);
  };
  entranceDone.then(async () => {
    let index = 0;
    while (true) {
      await wait(2500);
      if (document.hidden) continue;
      index = (index + 1) % words.length;
      show(words[index]);
    }
  });
}

// 3. Titles that build letter by letter when they scroll into view.
document.querySelectorAll('[data-letters]').forEach((title) => {
  const text = title.textContent.trim();
  title.setAttribute('aria-label', text);
  let i = 0;
  title.replaceChildren(...text.split(' ').flatMap((part, wordIndex, parts) => {
    const wordSpan = document.createElement('span');
    wordSpan.className = 'w';
    wordSpan.setAttribute('aria-hidden', 'true');
    [...part].forEach((char) => {
      const letter = document.createElement('span');
      letter.className = 'ch';
      letter.style.setProperty('--i', i++);
      letter.textContent = char;
      wordSpan.append(letter);
    });
    return wordIndex < parts.length - 1 ? [wordSpan, ' '] : [wordSpan];
  }));
  title.classList.add('split');
  whenVisible(title, () => title.classList.add('is-in'), .4);
});

// 4. Figures that count up.
document.querySelectorAll('[data-count]').forEach((figure) => {
  const target = Number(figure.dataset.count);
  if (reduceMotion) return;
  figure.textContent = '0';
  whenVisible(figure, () => {
    const start = performance.now();
    const duration = 1600;
    const tick = (now) => {
      const progress = Math.min((now - start) / duration, 1);
      figure.textContent = Math.round(target * (1 - Math.pow(1 - progress, 3)));
      if (progress < 1) requestAnimationFrame(tick);
    };
    requestAnimationFrame(tick);
  }, .6);
});

// 5. Install steps that advance on their own while visible.
const stepList = document.querySelector('[data-steps]');
if (stepList) {
  const steps = [...stepList.querySelectorAll('.step')];
  const activate = (next) => {
    steps.forEach((step, index) => {
      const active = index === next;
      step.classList.toggle('is-active', active);
      step.querySelector('button').setAttribute('aria-pressed', String(active));
      if (active) {
        const bar = step.querySelector('.step-bar i');
        bar.style.animation = 'none';
        void bar.offsetWidth;
        bar.style.animation = '';
      }
    });
  };
  steps.forEach((step, index) => {
    step.querySelector('button').addEventListener('click', () => activate(index));
    step.querySelector('.step-bar i').addEventListener('animationend', () => activate((index + 1) % steps.length));
  });
  if ('IntersectionObserver' in window) {
    stepList.classList.add('is-paused');
    new IntersectionObserver(([entry]) => stepList.classList.toggle('is-paused', !entry.isIntersecting), { threshold: .5 }).observe(stepList);
  }
}
