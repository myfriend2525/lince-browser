// Enlaces de descarga de Lince Browser.
//
// - repository: URL del repositorio, p. ej. 'https://github.com/tu-usuario/lince-browser'.
//   Mientras esté vacío, los botones de descarga llevan a la sección #descargar y muestran
//   «Próximamente», y se ocultan los enlaces a GitHub (código fuente y soporte).
// - geckoApk / webviewApk: enlace directo fijo a cada APK. Si se dejan vacíos, la web pregunta a
//   GitHub por la última Release y enlaza directamente el APK cuyo nombre coincide con
//   apkPattern / webviewApkPattern. Si no lo encuentra, el botón abre la página de Releases.
// - apkPattern: APK de GeckoView (arm64, el más ligero) que usan el botón de descarga y Obtainium.
// - webviewApkPattern: nombre del APK de la edición System WebView.
// - telegram: grupo de la comunidad. Muestra un aviso flotante y un enlace en el pie de página;
//   déjalo vacío para ocultar ambos.
window.LINCE_LINKS = {
  repository: 'https://github.com/myfriend2525/lince-browser',
  geckoApk: '',
  webviewApk: '',
  apkPattern: '^LinceBrowser-v[0-9]+(?:\\.[0-9]+){1,2}-arm64-v8a-release\\.apk$',
  webviewApkPattern: '^LinceBrowser-v[0-9]+(?:\\.[0-9]+){1,2}-systemwebview-release\\.apk$',
  telegram: 'https://t.me/lincebrowser',
};
