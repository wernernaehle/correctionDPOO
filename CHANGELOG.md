# Changelog

Los cambios relevantes del proyecto se documentan en este archivo.

## [Unreleased] - 2026-11-04

### Añadido

- Penalización opcional por calidad del código, configurable por ejercicio, con análisis estático del código fuente usando la Compiler Tree API del JDK.
- Informe de calidad en HTML con nota obtenida y final, penalización, resumen por categoría y detalle de incumplimientos agrupado por categoría.
- Validación de configuración y referencias de calidad antes de iniciar la corrección; las reglas incumplidas por una solución de referencia se desactivan automáticamente para ese ejercicio.
- Requisitos mínimos de tests por ejercicio.
- Evaluación de Javadoc configurable y presentada en una sección independiente del informe.
- Generación de archivos ZIP cifrados con contraseña y fichero `PASSWORD.txt` personalizado.

### Modificado

- El mensaje de `PASSWORD.txt` indica que el informe incluye los resultados de tests, calidad y la nota.
- Se retiró del inicio del informe HTML el aviso rojo sobre la nota no final y una posible penalización de calidad.
- El informe de calidad explica qué revisa cada categoría y agrupa bajo ella los incumplimientos correspondientes.

