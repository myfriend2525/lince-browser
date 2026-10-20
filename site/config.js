// Enlaces de descarga de Lince Browser.
// Es el único archivo que hay que tocar cuando exista el repositorio de GitHub.
//
// - repository: URL del repositorio, p. ej. 'https://github.com/tu-usuario/lince-browser'.
//   Mientras esté vacío, los botones de descarga llevan a la sección #descargar y muestran
//   «Próximamente», y se ocultan los enlaces a GitHub (código fuente y soporte).
// - geckoApk / webviewApk: enlace directo opcional a cada APK. Si se dejan vacíos, los botones
//   abren la página de la última versión en GitHub Releases.
// - apkPattern: expresión regular con la que Obtainium elige el APK de la edición GeckoView.
window.LINCE_LINKS = {
  repository: 'https://github.com/myfriend2525/lince-browser',
  geckoApk: '',
  webviewApk: '',
  apkPattern: '^LinceBrowser-v[0-9]+(?:\\.[0-9]+){1,2}-release\\.apk$',
};
